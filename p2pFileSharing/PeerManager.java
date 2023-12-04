import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

public class PeerManager implements Runnable {
    private Socket socketListener;
    private final PeerProcess process;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private HandShakeMessage hsmsg;
    private String correspondentPeerID;
    private String peerID;
    private int downloadRate = 0;
    private boolean madeConnection = false;
    private boolean prompter = false; // If this peer is the one prompting the handshake

    public PeerManager(Socket listener, String pID, PeerProcess process) {
        socketListener = listener; // Assign listener
        this.process = process; // Assign the particular process
        this.peerID = pID;

        // Create input and output streams
        try {
            out = new ObjectOutputStream(socketListener.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socketListener.getInputStream());
        }
        catch(IOException ioException) {
            ioException.printStackTrace();
            System.out.println("ERROR CREATING PEERMANAGER");
        }

        hsmsg = new HandShakeMessage(pID);
    }
    public void run() {
        try {
            // Create the handshake message byte array and send it out
            byte[] msg = hsmsg.buildHandShakeMessage();
            // Log the sent handshake message
            this.process.getPeerLogger().logMessage(4, this.peerID + " has sent the following handshake message to " + this.correspondentPeerID);
            this.process.getPeerLogger().logMessage(4, Arrays.toString(msg));
            out.write(msg);
            out.flush();

            while (true) {
                if (!madeConnection) {
                    establishConnection();

                    if (this.process.getPeerInfo().containsFile() || this.process.getNeighborsPieces().get(this.peerID).cardinality() > 0) {
                        try {
                            BitSet myPieces = this.process.getNeighborsPieces().get(this.peerID);
                            ActualMessage am = new ActualMessage(ActualMessage.MessageType.BITFIELD, myPieces.toByteArray());
                            out.write(am.buildActualMessage());
                            out.flush();
                        }
                        catch (Exception e) {
                            System.out.println("ERROR BUILDING BITFIELD MESSAGE");

                        }
                    }
                }
                else {
                    while (this.in.available() < 4) {
                    }
                    int msgLength = this.in.readInt();
                    byte[] actualMsg = new byte[msgLength];
                    this.in.readFully(actualMsg);

                    // Gather the message type with the written method
                    ActualMessage.MessageType messageType = getMessageType(actualMsg);
                    // TODO CHECK THE FOLLOWING TWO STATEMENTS, seems there are discrepancies between this and ActualMessage.java
                    ActualMessage acmsg = new ActualMessage(messageType);
                    acmsg.readActualMessage(msgLength, actualMsg);

                    // Method to handle the message based on the type
                    handleMessage(acmsg, messageType);
                }
            }
        }
        catch (IOException ioException){
            ioException.printStackTrace();
            System.out.println("ERROR RUNNING PEER MANAGER");
        }
    }

    private ActualMessage.MessageType getMessageType(byte[] actualMsg) {
        byte type = actualMsg[0];

        ActualMessage.MessageType messageType = switch (type) {
            case 0 -> ActualMessage.MessageType.CHOKE;
            case 1 -> ActualMessage.MessageType.UNCHOKE;
            case 2 -> ActualMessage.MessageType.INTERESTED;
            case 3 -> ActualMessage.MessageType.NOT_INTERESTED;
            case 4 -> ActualMessage.MessageType.HAVE;
            case 5 -> ActualMessage.MessageType.BITFIELD;
            case 6 -> ActualMessage.MessageType.REQUEST;
            case 7 -> ActualMessage.MessageType.PIECE;
            default -> null;
        };
        return messageType;
    }

    private void establishConnection() {
        try {
            // Create a 32-byte array to encapsulate the returned handshake message
            byte[] returnhs = new byte[32];
            in.readFully(returnhs); // Read in the returned handshake message

            // Log the received handshake message
            this.process.getPeerLogger().logMessage(4, this.peerID + " has received the following handshake message from " + this.correspondentPeerID);
            this.process.getPeerLogger().logMessage(4, Arrays.toString(returnhs));

            hsmsg = hsmsg.readHandShakeMessage(returnhs);
            // Get the peer ID of the corresponding peer from the handshake message
            this.correspondentPeerID = hsmsg.getPeerID();
            // Add the connected neighbor into the hash map with the corresponding peer ID and the current peer manager
            this.process.addConnectedNeighbor(this.correspondentPeerID, this);
            // Add the current thread into the hash map with the corresponding peer ID
            this.process.addConnectedThread(this.correspondentPeerID, Thread.currentThread());

            madeConnection = true;

            if (this.prompter)
                this.process.getPeerLogger().peerToPeerTCPLog(this.correspondentPeerID);
            else
                this.process.getPeerLogger().peerFromPeerTCPLog(this.correspondentPeerID);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            System.out.println("ERROR ESTABLISHING CONNECTION");
        }
    }

    private void handleMessage(ActualMessage acmsg, ActualMessage.MessageType type) {
        switch (type) {
            case CHOKE:
                // reset the requested info due to the choke message
                String[] processRequestedInfo = this.process.getRequestedInfo();
                for (int i = 0; i < processRequestedInfo.length; i++) {
                    if (processRequestedInfo[i].equals(this.correspondentPeerID) && processRequestedInfo[i] != null)
                        processRequestedInfo[i] = null;
                }
                // log the choking
                this.process.getPeerLogger().chokingLog(this.correspondentPeerID);
                break;
            case UNCHOKE:
                // Check if this peer is interested in one of the corresponding peer's pieces
                // and send a request or not interested message based on the result
                checkRequestsAndSendMsg();

                // log the unchoking
                this.process.getPeerLogger().unchokingLog(this.correspondentPeerID);
                break;
            case INTERESTED:
                this.process.getInterestedNeighbors().add(this.correspondentPeerID);
                // log that this peer received an interest message
                this.process.getPeerLogger().receiveInterestLog(this.correspondentPeerID);
                break;
            case NOT_INTERESTED:
                this.process.getNeighborsPieces().remove(this.correspondentPeerID);
                // log that this peer received a not interested message
                this.process.getPeerLogger().receiveNotInterestLog(this.correspondentPeerID);
                break;
            case HAVE:
                int pieceIndex = acmsg.getPieceIndexFromPayload();
                this.process.getNeighborsPieces().get(this.correspondentPeerID).set(pieceIndex);
                // Cancel chokes if all peers are done
                if(process.allFinished())
                    this.process.stopChokes();

                // Check if this peer is interested in the corresponding peer's pieces
                // and send an interested or not message based on the result
                checkInterestAndSendMsg();

                // log that we received the "have" message
                this.process.getPeerLogger().receiveHaveLog(pieceIndex, this.correspondentPeerID);
                break;
            case BITFIELD:
                BitSet bitfield = acmsg.getBitFieldMessage();
                // replace the bitfield of the correspondent in the neighborsPieces map
                this.process.getNeighborsPieces().remove(this.correspondentPeerID);
                this.process.getNeighborsPieces().put(this.correspondentPeerID, bitfield);

                boolean haveFile = this.process.getPeerInfo().containsFile();
                if (!haveFile) {
                    // Check if this peer is interested in the corresponding peer's pieces
                    // and send an interested or not message based on the result
                    checkInterestAndSendMsg();
                }
                break;
            case REQUEST:
                if ((this.process.getOptimisticUnchokeNeighbor() != null && this.process.getOptimisticUnchokeNeighbor().equals(this.correspondentPeerID))
                        || this.process.getUnchokedNeighbors().contains(this.correspondentPeerID) ) {

                    pieceIndex = acmsg.getPieceIndexFromPayload();
                    try {
                        int myPosition = this.process.getCommonConfig().getPieceSize() * pieceIndex;
                        int size = this.process.getCommonConfig().getPieceSize();
                        if (pieceIndex == this.process.getPieceCount() - 1) {
                            size = this.process.getCommonConfig().getFileSize() % size;
                        }
                        this.process.getFile().seek(myPosition);
                        byte[] data = new byte[size];
                        this.process.getFile().read(data);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        byte[] requestPayload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
                        stream.write(requestPayload);
                        stream.write(data);

                        try {
                            ActualMessage am = new ActualMessage(ActualMessage.MessageType.REQUEST, stream.toByteArray());
                            out.write(am.buildActualMessage());
                            out.flush();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("ERROR SENDING REQUEST MESSAGE");
                        }

                    }
                    catch (IOException e) {
                        System.out.println("ERROR RECEIVING REQUEST MESSAGE");
                        throw new RuntimeException(e);
                    }

                }
                break;
            case PIECE:
                pieceIndex = acmsg.getPieceIndexFromPayload();
                byte[] receivedPiece = acmsg.getPieceFromPayload();

                // Write the receivedPiece to the file
                try {
                    int pos = this.process.getCommonConfig().getPieceSize() * pieceIndex;
                    this.process.getFile().seek(pos);
                    this.process.getFile().write(receivedPiece);
                }
                catch (Exception e) {
                    System.out.println("ERROR RECEIVING PIECE MESSAGE");
                    e.printStackTrace();
                }

                // Update the neighborsPieces map, now with this peer having received the additional piece
                this.process.getNeighborsPieces().get(this.peerID).set(pieceIndex);

                // Increase the download rate for this thread/peer
                this.downloadRate += 1;

                // Get number of finished pieces
                int numFinishedPieces = this.process.getNeighborsPieces().get(this.peerID).cardinality();
                // Log that this peer has downloaded the piece
                this.process.getPeerLogger().downloadPieceLog(pieceIndex, numFinishedPieces, this.peerID);
                // Update the requested info array so that we no longer require the piece in the pieceIndex
                this.process.getRequestedInfo()[pieceIndex] = null;
                // Have the peer broadcast "have" messages to all connected neighbors
                broadcastHaveMsgs(pieceIndex);

                if (this.process.getNeighborsPieces().get(this.peerID).cardinality() != this.process.getPieceCount()) {
                    // Check if this peer is interested in one of the corresponding peer's pieces
                    // and send a request or not interested message based on the result
                    checkRequestsAndSendMsg();
                }
                else {
                    this.process.getPeerLogger().downloadCompleteLog();
                    if(process.allFinished())
                        this.process.stopChokes();

                    // Send a not interested message
                    try {
                        ActualMessage am = new ActualMessage(ActualMessage.MessageType.NOT_INTERESTED);
                        out.write(am.buildActualMessage());
                        out.flush();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                System.out.println("Message received is not any of the types!");
        }
    }

    private void checkInterestAndSendMsg() {
        BitSet correspondentPieces = this.process.getNeighborsPieces().get(this.correspondentPeerID);
        BitSet myPieces = this.process.getNeighborsPieces().get(this.peerID);

        // See if we desire a piece from the corresponding peer
        // If we do, set the boolean for desire to true
        boolean desirePiece = false;
        for (int i = 0; i < this.process.getPieceCount() && i < correspondentPieces.size(); i++) {
            if (correspondentPieces.get(i) && !myPieces.get(i)) {
                this.process.getRequestedInfo()[i] = this.correspondentPeerID;
                desirePiece = true;
                break;
            }
        }

        if (desirePiece) {
            try {
                ActualMessage am = new ActualMessage(ActualMessage.MessageType.INTERESTED);
                out.write(am.buildActualMessage());
                out.flush();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                ActualMessage am = new ActualMessage(ActualMessage.MessageType.NOT_INTERESTED);
                out.write(am.buildActualMessage());
                out.flush();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkRequestsAndSendMsg() {
        BitSet correspondentPieces = this.process.getNeighborsPieces().get(this.correspondentPeerID);
        BitSet myPieces = this.process.getNeighborsPieces().get(this.peerID);

        // See if we desire a piece from the corresponding peer
        // If we do, set the index of the requestedInfo array to the peer ID of the corresponding peer
        int desiredPiece = -1;
        for (int i = 0; i < this.process.getPieceCount() && i < correspondentPieces.size(); i++) {
            if (correspondentPieces.get(i) && !myPieces.get(i) && this.process.getRequestedInfo()[i] == null) {
                this.process.getRequestedInfo()[i] = this.correspondentPeerID;
                desiredPiece = i;
                break;
            }
        }

        // If we have no desired piece, send a message that we're not interested
        if (desiredPiece < 0) {
            try {
                ActualMessage am = new ActualMessage(ActualMessage.MessageType.NOT_INTERESTED);
                out.write(am.buildActualMessage());
                out.flush();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Else, send a message that as a request for the desired piece
        else {
            try {
                byte[] requestPayload = ByteBuffer.allocate(4).putInt(desiredPiece).array();
                ActualMessage am = new ActualMessage(ActualMessage.MessageType.REQUEST, requestPayload);
                out.write(am.buildActualMessage());
                out.flush();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void broadcastHaveMsgs (int pieceIndex) {
        this.process.getConnectedNeighbors().forEach((key, value) -> {
            value.sendHaveMsgs(pieceIndex);
        });
    }

    private void sendHaveMsgs(int pieceIndex) {
        try {
            byte[] requestPayload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            ActualMessage am = new ActualMessage(ActualMessage.MessageType.HAVE, requestPayload);
            out.write(am.buildActualMessage());
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActualMessage.MessageType type) {
        try {
            ActualMessage am = new ActualMessage(type);

            this.out.write(am.buildActualMessage());
            this.out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCorrespondentPeerID(String x) { this.correspondentPeerID = x;}
    public void resetDownloadRate() {
        this.downloadRate = 0;
    }

    public int getDownloadRate() {
        return this.downloadRate;
    }

}