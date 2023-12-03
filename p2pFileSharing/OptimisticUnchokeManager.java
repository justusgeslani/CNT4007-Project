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


    // Default ctor
    OptimisticUnchokeManager(PeerProcess pProcess) {
        this.peerProcess = pProcess;
        this.optInterval = pProcess.getOptmisticUnchokeInterval; //TODO: implement getter
     }
    public void startTask() {
        // TODO: idk what the initial delay should be
        this.task = this.scheduler.scheduleAtFixedRate(this, 10, this.optInterval, TimeUnit.SECONDS);
    }

    public void stopTask() {
        this.scheduler.shutdownNow();
    }

    @Override
    public void run() {
        try {
            // Get current connections
            String currentOptimisticUnchoked = this.peerProcess.getOptimisticUnchokeNeighbor();
            Set<String> interestedNeighbors = new HashSet<>(this.peerProcess.getInterestedNeighbors());

            interestedNeighbors.remove(currentOptimisticUnchoked);
            interestedNeighbors.removeAll(this.peerProcess.getUnchokedNeighbors());

            String nextOptimisticUnchoked = selectRandomNeighbor(interestedNeighbors);

            // If the length of the interested neighbors is none or no optimistic neighbor can be found
            if (nextOptimisticUnchoked == null) {
                String currentOptimisticUnchoked = this.peerProcess.getOptimisticUnchokeNeighbor();
                this.peerProcess.setOptimisticUnchokeNeighbor(null);

                if (currentOptimisticUnchoked != null && !this.peerProcess.getUnchokedNeighbors().contains(currentOptimisticUnchoked)) {
                    PeerManager pm = this.peerProcess.getConnectedNeighbors().get(currentOptimisticUnchoked);
                    pm.sendMsg(ActualMessage.MessageType.CHOKE);
                }

                if (this.peerProcess.allFinished()) {
                    // TODO HAVE IMPLEMENTATION TO KILL CHOKES
                }


            }

            //this.peerProcess.setOptimisticUnhokedNeighbor(nextOptimisticUnchoked); // TODO: implement setter

            // TODO: send unchoked messages


        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

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

    private void sendUnchokeMessages(String curr, String next) {
        if (next != null) {
            this.peerProcess.getConnectedNeighbors().get(next).sendMsg(ActualMessage.MessageType.UNCHOKE);
            this.peerProcess.getPeerLogger().changeOptimisticUnchokedNeighborLog(next);
        }
        if (curr != null && this.peerProcess.getUnchokedNeighbors().contains(curr)) {
            this.peerProcess.getConnectedNeighbors().get(curr).sendMsg(ActualMessage.MessageType.CHOKE);
        }
    }

}
