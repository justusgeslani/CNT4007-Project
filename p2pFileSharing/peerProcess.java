// To compile: Use javac peerProcess.java
import java.io.*;
import java.util.*;

public class peerProcess {

        // In this project, the peer process should read two configuration files: Common.cfg and PeerInfo.cfg

        // The common properties used by all peers in Common.cfg are:

        public String numPreferNeighbors;       // NumberOfPreferredNeighbors
        public String uInterval;                // UnchokingInterval
        public String optimistUInterval;        // OptimisticUnchokingInterval
        public String fileName;                 // FileName
        public String fileSize;                 // FileSize
        public String pieceSize;                // PieceSize

        public Vector<peerProcess> commonInfoVector;

        // The peer info properties in PeerInfo.cfg are:

        public String peerID;                   // Peer ID
        public String hostName;                 // Host name
        public String listenPort;               // Listening port
        public String hasFileOrNoFile;          // Has file or not

        public Vector<peerProcess> peerInfoProperties;

        public String theCurrentPeerID;         // Current Peer ID

        // Default constructor for peerProcess
        public peerProcess(String pID){

                theCurrentPeerID = pID;
        }

        // Constructor for peerProcess to initialize properties of PeerInfo.cfg
        public peerProcess(String pID, String host, String lPort, String fileOrNoFile) {

                peerID = pID;
                hostName = host;
                listenPort = lPort;
                hasFileOrNoFile = fileOrNoFile;

        }

        // Constructor for peerProcess to initialize properties of Common.cfg
        public peerProcess(String nPNeighbors, String uI, String oUInterval, String fName, String fSize, String pSize) {

                numPreferNeighbors = nPNeighbors;
                uInterval = uI;
                optimistUInterval = oUInterval;
                fileName = fName;
                fileSize = fSize;
                pieceSize = pSize;
        }

        // Reads the contents of Common.cfg and assigns the output to peerProcess constructor
        public void getCommonInfo() {

                commonInfoVector = new Vector<peerProcess>();
                
                Vector<String> token = new Vector<String>();

                try{

                        File common = new File("Common.cfg");
                        Scanner myScanner = new Scanner(common).useDelimiter("\\s+");


                        while(myScanner.hasNext()) {

                                //System.out.println("Begin token sequence ----");
                                //System.out.println(myScanner.next());

                                token.add(myScanner.next());

                                //System.out.println("End token sequence ----");

                        }

                        for(int i = 0; i< token.size(); i++) {

                                System.out.println("Common Info Token: " + token.get(i));

                        }

                        System.out.println();

                        commonInfoVector.addElement(new peerProcess(token.get(1), token.get(3), token.get(5), token.get(7>

                }
                catch (Exception ex) {

                        System.out.println(ex.toString());

                }
        }

        // Reads the contents of PeerInfo.cfg and assigns output to peerProcess constructor
        public void getPeerInfo() {

                peerInfoProperties = new Vector<peerProcess>();

                try{

                        File common = new File("PeerInfo.cfg");
                        Scanner myScanner = new Scanner(common);

                        while(myScanner.hasNext()) {

                                Vector<String> token = new Vector<String>();

                                //System.out.println("Begin token sequence ----");
                                //System.out.println(myScanner.next());

                                token.add(myScanner.next());
                                myScanner.useDelimiter("\\s+");

                                //System.out.println(myScanner.next());

                                token.add(myScanner.next());
                                myScanner.useDelimiter("\\s+");

                                //System.out.println(myScanner.next());

                                token.add(myScanner.next());
                                myScanner.useDelimiter("\\s+");

                                //System.out.println(myScanner.next());

                                token.add(myScanner.next());

                                //System.out.println("End token sequence ----");

                                for(int i = 0; i < token.size(); i++) {

                                        System.out.println("Peer Info Token: " + token.get(i));
                                }

                                peerInfoProperties.addElement(new peerProcess(token.get(0), token.get(1), token.get(2), t>

                        }
                }
                catch (Exception ex) {

                        System.out.println(ex.toString());

                }

        }

        // ---------- Getters for Common.cfg properties ----------

        // Returns the number of preferred neighbors, e.g. 3
        public String getPreferredNeighbors() {

                return numPreferNeighbors;

        }

        // Returns the unchoking interval, e.g. 5
        public String getUnchokingInterval() {

                return uInterval;

        }

        // Returns the optimistic unchoking interval, e.g. 10
        public String getOptimistUnchokingInterval() {

                return optimistUInterval;

        }

        // Returns the file name, e.g. thefile
        public String getFileName() {

                return fileName;

        }

        // Returns the file size, e.g. 2167705
        public String getFileSize() {

                return fileSize;

        }

        // Returns the piece size, e.g. 16384
        public String getPieceSize() {

                return pieceSize;

        }

        // ---------- Getters for PeerInfo.cfg properties ----------

        // Returns the current peer ID, e.g. java peerProcess 1001
        public String getCurrentPeerID() {

                return theCurrentPeerID;

        }

        // Returns the peer ID, e.g. 1002
        public String getPeerID() {

                return peerID;

        }

        // Returns the host name, e.g. lin114-01.cise.ufl.edu
        public String getHostName() {

                return hostName;

        }

        // Returns the listening port, e.g. 6001
        public String getListeningPort() {

                return listenPort;

        }

        // Returns has file or no file, e.g. 0 or 1
        public String getFileOrNoFile() {

                return hasFileOrNoFile;

        }

        public static void main(String[] args) {

                try {

                        peerProcess start = new peerProcess(args[0]);
                        start.getCommonInfo();
                        start.getPeerInfo();

                }
                catch (Exception ex) {

                        System.out.println(ex);

                }

        }

}

