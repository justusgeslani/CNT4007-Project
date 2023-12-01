import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class peerManager implements Runnable {
    private Socket socketListener;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private HandShakeMessage hsmsg;
    private boolean madeConnection = false;

    public peerManager(Socket listener, String pID) {
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
            }
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
    }
}
