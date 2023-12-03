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

        // Variables regarding the neighboring peers
        private ArrayList<String> availableNeighbors = new ArrayList<>();
        private volatile HashMap<String, PeerManager> connectedNeighbors = new HashMap<>();
        private volatile HashMap<String, Thread> connectedThreads = new HashMap<>();
        private volatile HashMap<String, BitSet> neighborsPieces = new HashMap<>();
        private volatile HashSet<String> interestedNeighbors = new HashSet<>();

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


                initialize();
        }

        private void initialize() {
                // Initialization logic here
                this.peerInfo = this.peerInfoMap.get(this.peerID);
                this.availableNeighbors = this.peerInfoManager.getPeerList();

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

        public String getPeerID() { return this.peerID; }
        public PeerProcessLog getPeerLogger() { return this.peerLogger; }
        public String[] getRequestedInfo() { return this.requestedInfo; }
        public HashMap<String, BitSet> getNeighborsPieces() { return this.neighborsPieces; }
        public HashSet<String> getInterestedNeighbors() { return this.interestedNeighbors; }
        public int getPieceCount() { return this.pieceCount; }

        public void addConnectedNeighbor(String connectedPeerID, PeerManager pm) {
                this.connectedNeighbors.put(connectedPeerID, pm);
        }

        public void addConnectedThread(String connectedPeerID, Thread t) {
                this.connectedThreads.put(connectedPeerID, t);
        }

        public static void main(String[] args) {
                //might need to make another driver function
        }
}

