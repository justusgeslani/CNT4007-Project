// To compile: Use javac peerProcess.java
import java.io.*;
import java.util.*;

public class peerProcess {
        public static String numPreferNeighbors;       // NumberOfPreferredNeighbors
        public static String uInterval;                // UnchokingInterval
        public static String optimistUInterval;        // OptimisticUnchokingInterval
        public static String fileName;                 // FileName
        public static String fileSize;                 // FileSize
        public static String pieceSize;                // PieceSize
        public String numberOfPieces;           // Number of pieces in this file
        public String sizeOfLastPiece;          // Size of last piece in this file
        public String peerID;                   // Peer ID
        public String hostName;                 // Host name
        public String listenPort;               // Listening port
        public String hasFileOrNoFile;          // Has file or not
        public ArrayList<Integer> bitfield = new ArrayList<Integer>();     // Bitfield
        public static ArrayList<Map.Entry<String, Integer>> availablePeerServers = new ArrayList<>();       // Keep track of peer process servers that are up and available

        // Default constructor for peerProcess
        public peerProcess(String pID) {

                peerID = pID;
        }

        // Reads the contents of Common.cfg and assigns the values to variables for this peer process
        public void getCommonInfo() {
                
                Vector<String> token = new Vector<String>();

                try{

                        File common = new File("Common.cfg");
                        try (Scanner myScanner = new Scanner(common).useDelimiter("\\s+")) {
                                while(myScanner.hasNext()) {


                                        token.add(myScanner.next());

                                }
                        }


                        for(int i = 0; i< token.size(); i++) {

                                System.out.println("Common Info Token: " + token.get(i));

                        }


                        numPreferNeighbors = token.get(1);
                        uInterval= token.get(3);
                        optimistUInterval = token.get(5);
                        fileName = token.get(7);
                        fileSize = token.get(9);
                        pieceSize = token.get(11);

                        int sizeOfFile = Integer.parseInt(fileSize); // e.g. sizeOfFile = fileSize = 10,000,232 bytes
                        int sizeOfPiece = Integer.parseInt(pieceSize); // e.g. sizeOfPiece = pieceSize = 32768 bytes each piece

                        int numPieces = (sizeOfFile / sizeOfPiece); // (10,000,232 / 32768) = 305 pieces
                        int lastPieceSize = sizeOfFile - (sizeOfPiece * numPieces); // 10,000,232 - (32768 * 305) = 5,992 bytes for last piece
                        numPieces += 1; // 305 + 1 = 306 pieces total

                        numberOfPieces = Integer.toString(numPieces);
                        sizeOfLastPiece = Integer.toString(lastPieceSize);

                        System.out.println("Number of Pieces: " + numberOfPieces);
                        System.out.println("Size of last piece: " + sizeOfLastPiece);
                        System.out.println();


                }
                catch (Exception ex) {

                        System.out.println(ex.toString());

                }
        }


        // Reads the contents of PeerInfo.cfg and assigns the correct values to variables for this peer process
        public void getPeerInfo() {
                try{

                        File common = new File("PeerInfo.cfg");
                        try (Scanner myScanner = new Scanner(common)) {
                                // Iterating through PeerInfo.cfg and matching peer ID to current peer process
                                // to determine the properties to assign to this peer process
                                while(myScanner.hasNext()) {

                                        String pID = myScanner.next();
                                        myScanner.useDelimiter("\\s+");


                                        if(pID.equals(this.peerID)) {

                                                System.out.println("Peer ID: " + pID);
                                                System.out.println("----- This peer ID matches current peer process -----");
                                                
                                                this.hostName = myScanner.next();
                                                System.out.println("Host name: " + hostName);
                                                
                                                myScanner.useDelimiter("\\s+");
                                                
                                                this.listenPort = myScanner.next();
                                                System.out.println("Listening port: " + listenPort);
                                                
                                                myScanner.useDelimiter("\\s+");
                                                
                                                this.hasFileOrNoFile = myScanner.next();
                                                System.out.println("Has file or not: " + hasFileOrNoFile);
                                                
                                                myScanner.useDelimiter("\\s+");
                                                System.out.println();
                                                
                                                break;
                                        }
                                        else {
                                                
                                                System.out.println("Peer ID: " + pID);
                                                System.out.println("----- This peer ID does not match current peer process -----");

                                                String hostName = myScanner.next();

                                                System.out.println("Host name: " + hostName);
                                                
                                                myScanner.useDelimiter("\\s+");

                                                String listenPort = myScanner.next();

                                                System.out.println("Listening port: " + listenPort);
                                                
                                                myScanner.useDelimiter("\\s+");

                                                System.out.println("Has file or not: " + myScanner.next());
                                                
                                                myScanner.useDelimiter("\\s+");

                                                // Add the server to the static array list to keep track of what servers are available!
                                                // So new peer processes know which servers to contact to connect to!
                                                peerProcess.availablePeerServers.add(Map.entry(hostName, Integer.parseInt(listenPort)));
                                        }
                                }
                        }
                }
                catch (Exception ex) {

                        System.out.println(ex.toString());

                }

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

                try {

                        peerProcess peer = new peerProcess(args[0]);
                        peer.getCommonInfo();
                        peer.getPeerInfo();
                        peer.setBitfield();


                        if (!availablePeerServers.isEmpty()) {
//                                for (Map.Entry<String, Integer> peerServer : availablePeerServers) {
//                                        peerProcessClient client = new peerProcessClient(peerServer.getKey(), peerServer.getValue());
//                                        client.run();
//                                }
                                peerProcessClient client = new peerProcessClient();
                                client.run();
                        }
                        else {
                                peerProcessServer server = new peerProcessServer(Integer.parseInt(start.listenPort));
                                server.run();
                        }



                }
                catch (Exception ex) {

                        System.out.println(ex);

                }

        }

}

