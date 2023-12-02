// To compile: Use javac peerProcess.java
import java.io.*;
import java.net.SocketException;
import java.rmi.Remote;
import java.util.*;


public class PeerProcess {

        // Attribute variables regarding this peer process
        private final String peerID;
        private PeerConfigManager configs;
        private RemotePeerInfo peerInfo;
        private int pieceCount;

        // Variables regarding the neighboring peers
        private ArrayList<String> availableNeighbors = new ArrayList<>();
        private volatile HashMap<String, PeerManager> connectedNeighbors = new HashMap<>();
        private volatile HashMap<String, Thread> connectedThreads = new HashMap<>();

        // Threaded Variables
        private volatile String[] requestedInfo;

        public PeerProcess(String peerID) {
                this.peerID = peerID;
                this.configs = new PeerConfigManager();
                this.pieceCount = configs.getCommonConfig().calculatePieceCount();
                this.requestedInfo = new String[this.pieceCount];


                initialize();
        }

        private void initialize() {

                // Initialization logic here
        }

        public void openServer() {
                try {

                }
                catch (SocketException e) {
                        e.printStackTrace();
                }
        }

        public String getPeerID() { return this.peerID; }

        public static void main(String[] args) {
                //might need to make another driver function
        }
}

