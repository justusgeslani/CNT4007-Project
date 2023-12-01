// To compile: Use javac peerProcess.java
import java.io.*;
import java.util.*;


public class PeerProcess {


        private String peerId;

        // Data structures to hold peers
        private HashMap<String, RemotePeerInfo> peerInfoHashMap;
        private ArrayList<String> peerList;

        // Encapsulate Configurations
        private CommonConfig commonConfig;
        private PeerInfoConfig peerInfoConfig;


        private void initialize() {

                try {
                        // Load common files
                        this.commonConfig.loadCommonFile();
                        this.peerInfoConfig.loadConfigFile();

                } catch (Exception ex) {
                        System.out.println(ex.toString());
                }
        }

        private int getPieceCount() {
                int fileSize = this.commonConfig.getFileSize();
                int pieceSize = this.commonConfig.getPieceSize();
                return (fileSize + pieceSize - 1) / pieceSize;
        }

























//        public static String numPreferNeighbors;       // NumberOfPreferredNeighbors
//        public static String uInterval;                // UnchokingInterval
//        public static String optimistUInterval;        // OptimisticUnchokingInterval
//        public static String fileName;                 // FileName
//        public static String fileSize;                 // FileSize
//        public static String pieceSize;                // PieceSize
//        public String numberOfPieces;           // Number of pieces in this file
//        public String sizeOfLastPiece;          // Size of last piece in this file
//
//        public String hostName;                 // Host name
//        public String listenPort;               // Listening port
//        public String hasFileOrNoFile;          // Has file or not
//        public ArrayList<Integer> bitfield = new ArrayList<Integer>();     // Bitfield
//        public static ArrayList<Map.Entry<String, Integer>> availablePeerServers = new ArrayList<>();       // Keep track of peer process servers that are up and available

        // Default constructor for peerProcess
        public PeerProcess(String pID) {
                this.peerId = pID;
        }


        // Checks the hasFileOrNoFile variable and sets the bitfield appropriately
        public void setBitfield() {
                // If hasFileOrNoFile equals 1, we set all the bits in the bitfield to 1
                if (Objects.equals(hasFileOrNoFile, "1")) {
                        for (int i =0; i < Integer.parseInt(numberOfPieces); i++) {
                                bitfield.add(1);
                        }
                }
                // Otherwise, we set all the bits in the bitfield to 0
                else {
                        for (int i =0; i < Integer.parseInt(numberOfPieces); i++) {
                                bitfield.add(0);
                        }
                }
        }

        public static void main(String[] args) {

        }
}

