// To compile: Use javac peerProcess.java
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Remote;
import java.util.*;


public class PeerProcess {

	// Attribute variables regarding this peer process
	private final String peerID;
	private PeerConfigManager configs;
	private PeerInfo peerInfo;
	private int pieceCount;
	private Thread peerServerThread;
	private volatile RandomAccessFile file;
	private volatile ServerSocket serverSocket;
	private volatile boolean everythingDone = false;

	// Variables regarding the neighboring peers
	private ArrayList<String> availableNeighbors = new ArrayList<>();
	private volatile HashMap<String, PeerManager> connectedNeighbors = new HashMap<>();
	private volatile HashMap<String, Thread> connectedThreads = new HashMap<>();
	private volatile HashMap<String, BitSet> neighborsPieces = new HashMap<>();
	private volatile HashSet<String> interestedNeighbors = new HashSet<>();
	private volatile HashSet<String> unchokedNeighbors = new HashSet<>();
	private volatile String optimisticUnchokeNeighbor = new String();

	// Threaded Variables
	private volatile String[] requestedInfo;

	// Miscellaneous variables
	private PeerInfoManager peerInfoManager;
	private HashMap<String, PeerInfo> peerInfoMap;
	private PeerProcessLog peerLogger;
	private volatile OptimisticUnchokeManager optUnchokeManager;
	private volatile UnchokeManager unchokeManager;
	private volatile PeerTerminate terminateManager;

	public PeerProcess(String peerID) {
		this.peerID = peerID;

		this.configs = new PeerConfigManager();
		this.peerInfoManager = this.configs.getPeerInfoConfig();
		this.peerInfoMap = this.peerInfoManager.getPeerInfoMap();
		this.peerLogger = new PeerProcessLog(peerID);

		this.pieceCount = configs.getCommonConfig().calculatePieceCount();
		this.requestedInfo = new String[this.pieceCount];

		try {
			initialize();
		}
		catch (IOException ioException) {
			ioException.printStackTrace();
		}

		this.optUnchokeManager = new OptimisticUnchokeManager(this);
		this.unchokeManager = new UnchokeManager(this);
		this.terminateManager = new PeerTerminate(this);

		this.unchokeManager.startTask();
		this.optUnchokeManager.startTask();
	}

	private static void deleteDirectory(File dir) {
		File[] allContents = dir.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		dir.delete();
	}

	private void initialize() throws IOException {
		// Initialization logic here
		this.peerInfo = this.peerInfoMap.get(this.peerID);
		this.availableNeighbors = this.peerInfoManager.getPeerList();

		System.out.println("---------- Common.cfg ----------");
		System.out.println("NumberOfPreferredNeighbors: " + this.configs.getCommonConfig().getNumberOfPreferredNeighbors());
		System.out.println("UnchokingInterval: " + this.configs.getCommonConfig().getUnchokingInterval());
		System.out.println("OptimisticUnchokingInterval: " + this.configs.getCommonConfig().getOptimisticUnchokingInterval());
		System.out.println("FileName: " + this.configs.getCommonConfig().getFileName());
		System.out.println("FileSize: " + this.configs.getCommonConfig().getFileSize());
		System.out.println("PieceSize: " + this.configs.getCommonConfig().getPieceSize());
		System.out.println();
		System.out.println("---------- PeerInfo.cfg ----------");
		System.out.println("Peer ID: " + this.peerInfo.getPeerId());
		System.out.println("Peer Address: " + this.peerInfo.getPeerAddress());
		System.out.println("Peer Port: " + this.peerInfo.getPeerPort());
		System.out.println("Peer ID: " + this.peerInfo.containsFile());

		// Create directory for the peer
		// Directory path
		String directoryPath = "" + this.peerID;
		File directory = new File(directoryPath);

//                // Delete the directory if it exists
//                if (directory.exists()) {
//                        deleteDirectory(directory);
//                }

		// Create the directory
		if (directory.mkdir()) {
			String fileName = this.configs.getCommonConfig().getFileName();
			File temp = new File(directoryPath + "/" + fileName);

			try {
				// Assign a random access file for the peer
				this.file = new RandomAccessFile(temp, "rw");

				// Set the length of the file with the file size given by the configs
				if (!this.peerInfo.containsFile()) {
					this.file.setLength(this.configs.getCommonConfig().getFileSize());
				}
			} catch (FileNotFoundException e) {
				System.err.println("File not found: " + e.getMessage());

			} catch (IOException e) {
				System.err.println("Error accessing the file: " + e.getMessage());
			}
		} else {
			// Handle the case when directory creation fails
			// This might involve logging the error, alerting the user, or taking corrective action
		}

		// For each peer, initialize each of their bitsets
		this.peerInfoMap.forEach((key, value) -> {
			BitSet availablePieces = new BitSet(this.pieceCount);
			// If the peer contains the file, set the entire bitset to true
			if (this.peerInfoMap.get(key).containsFile()) {
				availablePieces.set(0, this.pieceCount);
				this.neighborsPieces.put(key, availablePieces);
			}
			// Otherwise set it to false
			else {
				availablePieces.set(0, this.pieceCount, false);
				this.neighborsPieces.put(key, availablePieces);
			}
		});

		try {
			this.runServer();
			this.connectToNeighbors();
		} catch (Exception e) {
			System.out.println("Error starting server and establishing neighbor connections" + e.toString());
		}
	}

	public void runServer() {
		try {
			this.serverSocket = new ServerSocket(this.peerInfo.getPeerPort());
			this.peerServerThread = new Thread(new PeerProcessServer(serverSocket, this));
			this.peerServerThread.start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connectToNeighbors() {
		try {
			Thread.sleep(5000);
			for (String pid : this.availableNeighbors) {

				if (pid.equals(this.peerID)) break;
				else {
					PeerInfo peer = this.peerInfoMap.get(pid);
					Socket peerSocket = new Socket(peer.getPeerAddress(), peer.getPeerPort());

					try {
						PeerManager handler = new PeerManager(peerSocket, pid, this);
						handler.setCorrespondentPeerID(pid);
						this.getConnectedNeighbors().put(pid, handler);

						// Start new thread for a neighbor
						Thread peerThread = new Thread(handler);
						this.addConnectedThread(pid, peerThread);
						peerThread.start();
					} catch (Exception e) {
						System.out.println("Error connecting to neighbors " + e.toString());
						peerSocket.close(); // Prevent resource leaks
					}
				}
			}
		} catch (InterruptedException e){
			Thread.currentThread().interrupt(); // Proper way to handle interrupted exceptions
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		// logic to stop server socket and other network related stuff
	}

	public void closeResources() {
		// logic to close resources: file handlers, etc.
	}

	public String getPeerID() { return this.peerID; }
	public PeerProcessLog getPeerLogger() { return this.peerLogger; }
	public String[] getRequestedInfo() { return this.requestedInfo; }
	public HashMap<String, BitSet> getNeighborsPieces() { return this.neighborsPieces; }
	public HashSet<String> getInterestedNeighbors() { return this.interestedNeighbors; }
	public int getPieceCount() { return this.pieceCount; }
	public PeerInfo getPeerInfo() { return this.peerInfo; }
	public CommonConfig getCommonConfig() { return this.configs.getCommonConfig(); }
	public RandomAccessFile getFile() { return this.file; }
	public HashMap<String, PeerManager> getConnectedNeighbors() { return this.connectedNeighbors; }
	public HashSet<String> getUnchokedNeighbors() { return this.unchokedNeighbors; }
	public String getOptimisticUnchokeNeighbor() { return this.optimisticUnchokeNeighbor; }
	public int getOptmisticUnchokeInterval() { return this.configs.getCommonConfig().getOptimisticUnchokingInterval(); }

	public boolean allFinished() {
		for (Map.Entry<String, BitSet> entry : neighborsPieces.entrySet()) {
			if (entry.getValue().cardinality() != this.pieceCount)
				return false;
		}
		return true;
	}

	public synchronized void stopThreads() {
		this.connectedThreads.forEach((key, value) -> {
			if (value != null) {
				value.interrupt();
			}
		});
	}

	public synchronized void stopChokes() {
		try {
			this.optUnchokeManager.stopTask();
			this.unchokeManager.stopTask();
			this.clearUnchokedNeighbors();
			this.optimisticUnchokeNeighbor = null;
			this.interestedNeighbors.clear();
			this.file.close();
			this.getPeerLogger().stopLogger();
			this.serverSocket.close();
			if (this.peerServerThread != null) {
				this.peerServerThread.interrupt();
			}
			this.everythingDone = true;
			this.terminateManager.startTask(10);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addConnectedNeighbor(String connectedPeerID, PeerManager pm) {
		this.connectedNeighbors.put(connectedPeerID, pm);
	}

	public void addConnectedThread(String connectedPeerID, Thread t) {
		this.connectedThreads.put(connectedPeerID, t);
	}

	public void setUnchokedNeighbors(HashSet<String> newUnchokedNeighbors) {
		this.unchokedNeighbors = newUnchokedNeighbors;
	}

	public void setOptimisticUnchokeNeighbor (String newOptimisticUnchokeNeighbor) {
		this.optimisticUnchokeNeighbor = newOptimisticUnchokeNeighbor;
	}

	public void clearUnchokedNeighbors() {
		this.unchokedNeighbors.clear();
	}

	public boolean stopEverything() {
		return this.everythingDone;
	}
}
