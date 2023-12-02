import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerManager implements Runnable {
    private Socket socketListener;
    private PeerProcess process;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private HandShakeMessage hsmsg;
    private String correspondentPeerID;
    private boolean madeConnection = false;

    public PeerManager(Socket listener, String pID, PeerProcess process) {
        socketListener = listener; // Assign listener
        this.process = process; // Assign the particular process

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
                    // Create a 32-byte array to encapsulate the returned handshake message
                    byte[] returnhs = new byte[32];
                    in.readFully(returnhs);
                    hsmsg = hsmsg.readHandShakeMessage(returnhs);
                    // Get the peer ID of the corresponding peer from the handshake message
                    this.correspondentPeerID = hsmsg.getPeerID();
                    // Add the connected neighbor into the hash map with the corresponding peer ID and the current peer manager
                    this.process.addConnectedNeighbor(this.correspondentPeerID, this);
                    // Add the current thread into the hash map with the corresponding peer ID
                    this.process.addConnectedThread(this.correspondentPeerID, Thread.currentThread());

                    madeConnection = true;
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
                    ActualMessage acmsg = new ActualMessage(messageType, actualMsg);
                    acmsg.readActualMessage(len, actualMsg);

                    // Method to handle the message based on the type
                    handleMessage(messageType);
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

    private void handleMessage(ActualMessage.MessageType type) {
        switch (type) {
            case CHOKE:
                break;
            case UNCHOKE:
                break;
            case INTERESTED:
                break;
            case NOT_INTERESTED:
                break;
            case HAVE:
                break;
            case BITFIELD:
                break;
            case REQUEST:
                break;
            case PIECE:
                break;
            default:
                System.out.println("Message received is not any of the types!");
        }
    }
}
