import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OptimisticUnchokeManager implements Runnable{

    // Interval for optimistic unchoking
    private final int optInterval;

    // Peer process information
    private final PeerProcess peerProcess;

    // Random selection strategy
    private final Random rnd = new Random();

    // Scheduler
    private final ScheduledExecutorService scheduler = null;

    // Next preferred neighbor (?)
    private ScheduledFuture<?> task = null;


    /**
     * Default ctor
     * @param pProcess The current peer process instance managing the peers and their state.
     */
    OptimisticUnchokeManager(PeerProcess pProcess) {
        this.peerProcess = pProcess;
        this.optInterval = pProcess.getOptmisticUnchokeInterval();
     }

    /**
     * Start the scheduled task of optimistically unchoking peers.
     * Method schedules the run method to be called at fixed (arbitrary) intervals.
     */
    public void startTask() {
        // Arbitrary initial delay
        this.task = this.scheduler.scheduleAtFixedRate(this, 10, this.optInterval, TimeUnit.SECONDS);
    }

    /**
     * Cancels scheduledjob, effectively stopping the handler from running.
     */
    public void stopTask() {
        this.scheduler.shutdownNow();
    }

    /**
     * Run method is periodically invoked by the scheduler/
     * Selects a new optimistically unchoked peer and updates the choking status of peers accordingly.
     */
    @Override
    public void run() {
        try {
            // Get current connections
            String currentOptimisticUnchoked = this.peerProcess.getOptimisticUnchokeNeighbor();
            Set<String> interestedNeighbors = new HashSet<>(this.peerProcess.getInterestedNeighbors());

            interestedNeighbors.remove(currentOptimisticUnchoked);
            interestedNeighbors.removeAll(this.peerProcess.getUnchokedNeighbors());

            String nextOptimisticUnchoked = selectRandomNeighbor(interestedNeighbors);

            if (nextOptimisticUnchoked != null) {
                this.peerProcess.setOptimisticUnchokeNeighbor(nextOptimisticUnchoked);
                this.peerProcess.getConnectedNeighbors().get(nextOptimisticUnchoked).sendMsg(ActualMessage.MessageType.CHOKE);
                this.peerProcess.getPeerLogger().changeOptimisticUnchokedNeighborLog(nextOptimisticUnchoked);
            } else {
                this.peerProcess.setOptimisticUnchokeNeighbor(null);
            }

            if (currentOptimisticUnchoked != null && this.peerProcess.getUnchokedNeighbors().contains(currentOptimisticUnchoked)) {
                this.peerProcess.getConnectedNeighbors().get(currentOptimisticUnchoked).sendMsg(ActualMessage.MessageType.CHOKE);
            }

            if (interestedNeighbors.isEmpty() && this.peerProcess.allFinished()) {
                this.peerProcess.stopChokes();
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Selects a random peer from a set of peer
     * @param neighbors the set of peers to choose from
     * @return returns a randomly selected peer, or null if the set is empty.
     */
    private String selectRandomNeighbor(Set<String> neighbors) {
        int len = neighbors.size();
        if (len == 0)
            return null;
        int x = rnd.nextInt(len);
        int i = 0;

        for (String neighbor : neighbors) {
            if (i == x) return neighbor;
            i++;
        }
        return null;
    }
}
