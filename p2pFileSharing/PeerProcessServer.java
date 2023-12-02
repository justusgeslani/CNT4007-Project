import java.net.*;
import java.io.*;



public class PeerProcessServer implements Runnable {
	private ServerSocket socketListener;
	private PeerProcess process;

	public PeerProcessServer(ServerSocket listener, PeerProcess process) {
		socketListener = listener;
		this.process = process;
	}

	public void run() {
        try {
            Socket neighborPeer = socketListener.accept();
			PeerManager neighborManager = new PeerManager(neighborPeer, process.getPeerID());
			Thread processServerThread = new Thread(neighborManager);
			processServerThread.start();
        }
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}
