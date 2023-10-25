import java.net.*;
import java.io.*;


public class peerProcessServer implements Runnable {
	private ServerSocket socketListener;

	public peerProcessServer(ServerSocket listener) {
		socketListener = listener;
	}

	public void run() {
        try {
            Socket neighborPeer = socketListener.accept();

        }
		catch (IOException ioException) {
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
