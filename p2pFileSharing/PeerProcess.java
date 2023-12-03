// To compile: Use javac peerProcess.java
import java.io.*;
import java.net.ServerSocket;
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

        }

        private void initialize() throws IOException {
                // Initialization logic here
                this.peerInfo = this.peerInfoMap.get(this.peerID);
                this.availableNeighbors = this.peerInfoManager.getPeerList();

                // Create directory for the peer
                if ((new File("peer_" + this.peerID)).mkdir()) {
                        String fileName = this.configs.getCommonConfig().getFileName();
                        File temp = new File("peer_" + this.peerID + "/" + fileName);
                        // if this peer doesn't have the file
                        if (!this.peerInfo.containsFile())
                                // create a new file for the peer
                                temp.createNewFile();
                        // assign a random access file for the peer as one of its attribute variables
                        this.file = new RandomAccessFile(temp, "rw");
                        if (!this.peerInfo.containsFile())
                                // set the length of the file with the file size given by the configs
                                this.file.setLength(this.configs.getCommonConfig().getFileSize());
                }
                else {
                        System.out.println("Couldn't create " + "peer_" + this.peerID + " file!");
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
        }

        public void runServer() {
                try {
                        ServerSocket serverSocket = new ServerSocket(this.peerInfo.getPeerPort());
                        this.peerServerThread = new Thread(new PeerProcessServer(serverSocket, this));
                        this.peerServerThread.start();
                }
                catch (IOException e) {
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

        public boolean allFinished() {
                for (Map.Entry<String, BitSet> entry : neighborsPieces.entrySet()) {
                        if (entry.getValue().cardinality() != this.pieceCount)
                                return false;
                }
                return true;
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

        public void clearUnchokedNeighbors() {
                this.unchokedNeighbors.clear();
        }

        public static void main(String[] args) {
                //might need to make another driver function
        }
}

