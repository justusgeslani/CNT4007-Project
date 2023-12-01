import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class peerManager implements Runnable {
    private Socket socketListener;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public peerManager(Socket listener) {
        socketListener = listener;

        try {
            out = new ObjectOutputStream(socketListener.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socketListener.getInputStream());
        }
        catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }
    public void run() {

    }
}
