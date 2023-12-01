// To compile: Use javac peerProcess.java
import java.io.*;
import java.util.*;


public class PeerProcess {


        private final String peerID;
        private PeerConfigManager configs;
        private int pieceCount;

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

        public static void main(String[] args) {
                //might need to make another driver function
        }
}

