import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class UnchokeManager implements Runnable {
    private PeerProcess process;
    private int unchokingInterval;
    private int numberOfPreferredNeighbors;
    private Random rnd = new Random();
    private ScheduledFuture<?> task = null;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    UnchokeManager(PeerProcess process) {
        this.process = process;
        // unchokingInterval
        // numberOfPreferredNeighbors
    }

    public void run() {
        try {
            ArrayList<String> interestedNeighbors = new ArrayList<>(this.process.getInterestedNeighbors());
            HashSet<String> oldUnchokedList = new HashSet<>(this.process.getUnchokedNeighbors());
            HashSet<String> newUnchokedList = new HashSet<>();
            // If at least one neighbor is interested
            if (interestedNeighbors.size() > 0) {
                // If this peer has not completed all its pieces
                if (this.process.getNeighborsPieces().get(this.process.getPeerID()).cardinality != this.process.getPieceCount()) {
                    // The max number of neighbors is the minimum of the preferred and the number of interested
                    int maxNeighbors = Math.min(this.numberOfPreferredNeighbors, interestedNeighbors.size());
                    for (int i = 0; i < maxNeighbors; i++) {
                        // Pick a random peerID that doesn't exist in the new unchoked list
                        String randomPeerID = interestedNeighbors.get(this.rnd.nextInt(interestedNeighbors.size()));
                        while (newUnchokedList.contains(randomPeerID)) {
                            randomPeerID = interestedNeighbors.get(this.rnd.nextInt(interestedNeighbors.size()));
                        }
                        // Get the PeerManager associated with the random peerID
                        PeerManager randomPeerManager = this.process.getConnectedNeighbors().get(randomPeerID);
                        if (oldUnchokedList.contains(randomPeerID)) {
                            oldUnchokedList.remove(randomPeerID);
                        }
                        else {
                            //TODO
                        }
                    }
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
