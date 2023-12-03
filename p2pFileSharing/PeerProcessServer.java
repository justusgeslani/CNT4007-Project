import java.net.*;
import java.io.*;



public class PeerProcessServer implements Runnable {
	private final ServerSocket socketListener;
	private final PeerProcess process;

	public PeerProcessServer(ServerSocket listener, PeerProcess process) {
		socketListener = listener;
		this.process = process;
	}

	public void run() {
		while (true) {
			try {
				Socket neighborPeer = socketListener.accept();
				PeerManager neighborManager = new PeerManager(neighborPeer, process.getPeerID(), this.process);
				Thread processServerThread = new Thread(neighborManager);
				processServerThread.start();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
				break;
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
}