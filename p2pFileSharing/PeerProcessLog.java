// Reference: https://www.geeksforgeeks.org/logging-in-java/
// Reference: https://www.javatpoint.com/java-get-current-date
// Reference: https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html
// Reference: https://www.digitalocean.com/community/tutorials/logger-in-java-logging-example
// Reference: https://docs.oracle.com/javase/8/docs/api/java/util/logging/FileHandler.html
// Reference: https://docs.oracle.com/javase/8/docs/api/java/util/logging/SimpleFormatter.html
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.time.*;

public class PeerProcessLog {

    public String peerID;
    public String thePieceIndex;
    public String theNumberOfPieces;
    public String theNeighborList;
    public LogManager manager = LogManager.getLogManager();
    public Logger logMessage = manager.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public ZoneId timeZone = ZoneId.of("EST", ZoneId.SHORT_IDS);
    public LocalDateTime now = LocalDateTime.now(timeZone);
    public DateTimeFormatter time = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    public String currentTime = time.format(now);
    public String message;
    public FileHandler peerFileHandler;
    public String peerLogFile;
    public String fileName;

    public PeerProcessLog(String pID) {

        this.peerID = pID;

        this.peerLogFile = peerLogFile(this.peerID);

        try {
            this.peerFileHandler = new FileHandler(this.peerLogFile);
            logMessage.setLevel(Level.INFO);
            this.peerFileHandler.setFormatter(new SimpleFormatter());
            logMessage.addHandler(this.peerFileHandler);

        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public String peerLogFile(String pID) {

        fileName = "log_peer_" + pID + ".log";
        return fileName;
    }

    public String logMessage(int caseNumber, String pID) {

        String tempMessage = "[" + currentTime + "]: ";

        switch (caseNumber) {
            // Log message for when peer makes a TCP connection to another peer
            case 1:
                tempMessage += "Peer [" + this.peerID + "] makes a connection to Peer [" + pID + "].";
                break;
            // Log message for when peer makes a TCP connection from another peer
            case 2:
                tempMessage += "Peer [" + this.peerID + "] is connected from Peer [" + pID + "].";
                break;
            // Log message for when peer changes its preferred neighbors
            case 3:
                tempMessage += "Peer [" + this.peerID + "] has the preferred neighbors ";
                break;
            // Log message for when peer changes its optimistically unchoked neighbor
            case 4:
                tempMessage += "Peer [" + this.peerID + "] has the optimistically unchoked neighbor [" + pID + "].";
                break;
            // Log message for when peer receives an unchoked message from a neighbor
            case 5:
                tempMessage += "Peer [" + this.peerID + "] is unchoked by [" + pID + "].";
                break;
            // Log message for when peer receives a choke message from a neighbor
            case 6:
                tempMessage += "Peer [" + this.peerID + "] is choked by [" + pID + "].";
                break;
            // Log message for when peer receives a 'have' message
            case 7:
                tempMessage += "Peer [" + this.peerID + "] received the 'have' message from [" + pID + "] for the piece ";
                break;
            // Log message for when peer receives an 'interested' message
            case 8:
                tempMessage += "Peer [" + this.peerID + "] received the 'interested' message from [" + pID + "].";
                break;
            // Log message for when peer receives a 'not interested' message
            case 9:
                tempMessage += "Peer [" + this.peerID + "] received the 'not interested' message from [" + pID + "].";
                break;
            // Log message for when peer finishes downloading a piece
            case 10:
                tempMessage += "Peer [" + this.peerID + "] has downloaded the piece ";
                break;
            // Log message for when peer downloads the complete file
            case 11:
                tempMessage += "Peer [" + this.peerID + "] has downloaded the complete file.";
                break;

        }

        return tempMessage;


    }

    public void peerToPeerTCPLog(String pID) {

        message = logMessage(1, pID);
        logMessage.log(Level.INFO, message);
    }

    public void peerFromPeerTCPLog(String pID) {

        message = logMessage(2, pID);
        logMessage.log(Level.INFO, message);
    }

    public void changePreferNeighborLog(ArrayList<String> list) {

        message = logMessage(3, this.peerID);
        theNeighborList = "";
        for(int i = 0; i < list.size(); i++) {

            theNeighborList += list.get(i) + ",";
        }
        theNeighborList = theNeighborList.substring(0, theNeighborList.lastIndexOf(","));
        message += "[" + theNeighborList + "].";
        logMessage.log(Level.INFO, message);
    }

    public void changeOptimisticUnchokedNeighborLog(String pID) {

        message = logMessage(4, pID);
        logMessage.log(Level.INFO, message);
    }

    public void unchokingLog(String pID) {

        message = logMessage(5, pID);
        logMessage.log(Level.INFO, message);
    }

    public void chokingLog(String pID) {

        message = logMessage(6, pID);
        logMessage.log(Level.INFO, message);
    }

    public void receiveHaveLog(int pieceIndex, String pID) {

        thePieceIndex = Integer.toString(pieceIndex);
        message = logMessage(7, pID);
        message += "[" + thePieceIndex + "].";
        logMessage.log(Level.INFO, message);
    }

    public void receiveInterestLog(String pID) {

        message = logMessage(8, pID);
        logMessage.log(Level.INFO, message);
    }

    public void receiveNotInterestLog(String pID) {

        message = logMessage(9, pID);
        logMessage.log(Level.INFO, message);
    }

    public void downloadPieceLog(int pieceIndex, int numberOfPieces, String pID) {

        thePieceIndex = Integer.toString(pieceIndex);
        theNumberOfPieces = Integer.toString(numberOfPieces);
        message = logMessage(10, this.peerID);
        message += "[" + thePieceIndex + "] from [" + pID + "]. Now the number of pieces it has is [" + theNumberOfPieces + "].";
        logMessage.log(Level.INFO, message);
    }

    public void downloadCompleteLog() {

        message = logMessage(11, this.peerID);
        logMessage.log(Level.INFO, message);
    }


}
