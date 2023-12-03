import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.io.ByteArrayOutputStream;

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
        }

        hsmsg = new HandShakeMessage(pID);
    }
    public void run() {
        try {
            // Create the handshake message byte array and send it out
            byte[] msg = hsmsg.buildHandShakeMessage();
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

                        }
                    }
                }
                else {
                    // Create a 4-byte array to encapsulate the message length header from the actual message
                    byte[] msgLength = new byte[4];
                    in.readFully(msgLength);
                    // Create a len variable to store the message length gathered from the header
                    int len = 0;
                    // Convert the byte array to an int
                    for (byte b : msgLength) { len = (len << 8) + (b & 0xFF); }
                    // Read the actual message, as we now know the message length
                    byte[] actualMsg = new byte[len];
                    in.readFully(actualMsg);
                    // Gather the message type with the written method
                    ActualMessage.MessageType messageType = getMessageType(actualMsg);
                    // TODO CHECK THE FOLLOWING TWO STATEMENTS, seems there are discrepancies between this and ActualMessage.java
                    ActualMessage acmsg = new ActualMessage(messageType);
                    acmsg.readActualMessage(len, actualMsg);

                    // Method to handle the message based on the type
                    handleMessage(acmsg, messageType);
                }
            }
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    private ActualMessage.MessageType getMessageType(byte[] actualMsg) {
        char type = (char) actualMsg[0];
        ActualMessage.MessageType messageType = switch (type) {
            case '0' -> ActualMessage.MessageType.CHOKE;
            case '1' -> ActualMessage.MessageType.UNCHOKE;
            case '2' -> ActualMessage.MessageType.INTERESTED;
            case '3' -> ActualMessage.MessageType.NOT_INTERESTED;
            case '4' -> ActualMessage.MessageType.HAVE;
            case '5' -> ActualMessage.MessageType.BITFIELD;
            case '6' -> ActualMessage.MessageType.REQUEST;
            case '7' -> ActualMessage.MessageType.PIECE;
            default -> null;
        };
        return messageType;
    }

    private void establishConnection() {
        try {
            // Create a 32-byte array to encapsulate the returned handshake message
            byte[] returnhs = new byte[32];
            in.readFully(returnhs); // Read in the returned handshake message
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
                        }

                    }
                    catch (IOException e) {
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
        try {
            BitSet correspondentPieces = this.process.getNeighborsPieces().get(this.correspondentPeerID);
            BitSet myPieces = this.process.getNeighborsPieces().get(this.peerID);

            boolean desirePiece = false;
            for (int i = 0; i < this.process.getPieceCount() && i < correspondentPieces.size(); i++) {
                if (correspondentPieces.get(i) && !myPieces.get(i)) {
                    this.process.getRequestedInfo()[i] = this.correspondentPeerID;
                    desirePiece = true;
                    break;
                }
            }

            ActualMessage am;
            if (desirePiece) {
                am = new ActualMessage(ActualMessage.MessageType.INTERESTED);
                System.out.println("Sending INTERESTED message to peer: " + this.correspondentPeerID);
            } else {
                am = new ActualMessage(ActualMessage.MessageType.NOT_INTERESTED);
                System.out.println("Sending NOT INTERESTED message to peer: " + this.correspondentPeerID);
            }

            out.write(am.buildActualMessage());
            out.flush();
        } catch (IOException e) {
            System.err.println("IOException occurred while sending message to peer: " + this.correspondentPeerID);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected exception occurred while sending message to peer: " + this.correspondentPeerID);
            e.printStackTrace();
        }
    }


    private void checkRequestsAndSendMsg() {
        BitSet correspondentPieces = this.process.getNeighborsPieces().get(this.correspondentPeerID);
        BitSet myPieces = this.process.getNeighborsPieces().get(this.peerID);

        int desiredPiece = -1;
        for (int i = 0; i < this.process.getPieceCount() && i < correspondentPieces.size(); i++) {
            if (correspondentPieces.get(i) && !myPieces.get(i) && this.process.getRequestedInfo()[i] == null) {
                this.process.getRequestedInfo()[i] = this.correspondentPeerID;
                desiredPiece = i;
                break;
            }
        }

        if (desiredPiece < 0) {
            try {
                ActualMessage am = new ActualMessage(ActualMessage.MessageType.NOT_INTERESTED);
                out.write(am.buildActualMessage());
                out.flush();
                System.out.println("Sent NOT_INTERESTED message to peer: " + correspondentPeerID);
            } catch (IOException e) {
                System.err.println("IOException occurred while sending NOT_INTERESTED message to peer: " + correspondentPeerID);
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected exception occurred while sending NOT_INTERESTED message to peer: " + correspondentPeerID);
                e.printStackTrace();
            }
        } else {
            try {
                byte[] requestPayload = ByteBuffer.allocate(4).putInt(desiredPiece).array();
                ActualMessage am = new ActualMessage(ActualMessage.MessageType.REQUEST, requestPayload);
                out.write(am.buildActualMessage());
                out.flush();
                System.out.println("Sent REQUEST message for piece " + desiredPiece + " to peer: " + correspondentPeerID);
            } catch (IOException e) {
                System.err.println("IOException occurred while sending REQUEST message for piece " + desiredPiece + " to peer: " + correspondentPeerID);
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected exception occurred while sending REQUEST message for piece " + desiredPiece + " to peer: " + correspondentPeerID);
                e.printStackTrace();
            }
        }
    }


    private void broadcastHaveMsgs(int pieceIndex) {
        this.process.getConnectedNeighbors().forEach((key, value) -> {
            try {
                value.sendHaveMsgs(pieceIndex);
                System.out.println("Broadcasted HAVE message for piece index: " + pieceIndex + " to peer: " + key);
            } catch (Exception e) {
                System.err.println("Error broadcasting HAVE message for piece index: " + pieceIndex + " to peer: " + key);
                e.printStackTrace();
            }
        });
    }

    private void sendHaveMsgs(int pieceIndex) {
        try {
            byte[] requestPayload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            ActualMessage am = new ActualMessage(ActualMessage.MessageType.HAVE, requestPayload);
            out.write(am.buildActualMessage());
            out.flush();
            System.out.println("Sent HAVE message for piece index: " + pieceIndex);
        } catch (IOException e) {
            System.err.println("IOException occurred while sending HAVE message for piece index: " + pieceIndex);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected exception occurred while sending HAVE message for piece index: " + pieceIndex);
            e.printStackTrace();
        }
    }


    public void sendMsg(ActualMessage.MessageType type) {
        try {
            ActualMessage am = new ActualMessage(type);

            this.out.write(am.buildActualMessage());
            this.out.flush();
        } catch (IOException e) {
            System.err.println("IOException occurred while sending a message of type " + type);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected exception occurred while sending a message of type " + type);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void setCorrespondentPeerID(String x) {
        try {
            this.correspondentPeerID = x;
            System.out.println("Correspondent peer ID set to: " + x);
        } catch (Exception e) {
            System.err.println("Error setting correspondent peer ID to: " + x);
            e.printStackTrace();
        }
    }

    public void resetDownloadRate() {
        try {
            this.downloadRate = 0;
            System.out.println("Download rate reset to 0.");
        } catch (Exception e) {
            System.err.println("Error resetting download rate.");
            e.printStackTrace();
        }
    }

    public int getDownloadRate() {
        try {
            System.out.println("Current download rate: " + this.downloadRate);
            return this.downloadRate;
        } catch (Exception e) {
            System.err.println("Error retrieving download rate.");
            e.printStackTrace();
            return -1; // Return a default or error value
        }
    }
}