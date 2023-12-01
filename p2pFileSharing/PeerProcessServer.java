import java.net.*;
import java.io.*;



public class PeerProcessServer implements Runnable {
	private ServerSocket socketListener;

	public peerProcessServer(ServerSocket listener) {
		socketListener = listener;
	}

	public void run() {
        try {
            Socket neighborPeer = socketListener.accept();
			peerManager neighborManager = new peerManaer()
        }
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}
