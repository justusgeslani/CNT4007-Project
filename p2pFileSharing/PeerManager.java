import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerManager implements Runnable {
    private Socket socketListener;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private HandShakeMessage hsmsg;
    private boolean madeConnection = false;

    public PeerManager(Socket listener, String pID) {
        socketListener = listener; // Assign listener

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
            byte[] msg = hsmsg.buildHandShakeMessage();
            out.write(msg);
            out.flush();

            while (true) {
                if (!madeConnection) {
                    byte[] returnhs = new byte[32];
                    in.readFully(returnhs);
                    hsmsg = hsmsg.readHandShakeMessage(returnhs);
                    madeConnection = true;
                }
                else {
                    byte[] msgLength = new byte[4];
                    in.readFully(msgLength);
                    int len = 0;
                    for (byte b : msgLength) { len = (len << 8) + (b & 0xFF); }
                    byte[] actualMsg = new byte[len];
                    in.readFully(actualMsg);
                    ActualMessage.MessageType messageType = getMessageType(actualMsg);
                    ActualMessage acmsg = new ActualMessage(messageType);



                }
            }
        }
        catch(IOException ioException){
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
}
