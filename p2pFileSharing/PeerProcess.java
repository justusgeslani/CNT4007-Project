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

        // Threaded Variables
        private volatile String[] requestedInfo;

        // Miscellaneous variables
        private PeerInfoManager peerInfoManager;
        private HashMap<String, PeerInfo> peerInfoMap;

        public PeerProcess(String peerID) {
                this.peerID = peerID;

                this.configs = new PeerConfigManager();
                this.peerInfoManager = this.configs.getPeerInfoConfig();
                this.peerInfoMap = this.peerInfoManager.getPeerInfoMap();

                this.pieceCount = configs.getCommonConfig().calculatePieceCount();
                this.requestedInfo = new String[this.pieceCount];


                initialize();
        }

        private void initialize() {
                // Initialization logic here
                this.peerInfo = this.peerInfoMap.get(this.peerID);
                this.availableNeighbors = this.peerInfoManager.getPeerList();

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

