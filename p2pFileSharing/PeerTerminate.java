import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PeerTerminate implements Runnable {
    /**
     * A peer terminates when it founds out that all peers, **not just itself**,
     * have downloaded the complete file.
     */

    private final PeerProcess peerProcess;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task = null;


    // Default ctor
    PeerTerminate(PeerProcess pProcess) {
        this.peerProcess = pProcess;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void run() {
        try {
            // Check if this peer and all connected peers are done
            if (this.peerProcess.stopEverything()) {
                this.peerProcess.stopThreads();
                this.stopTask();;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void startTask(int interval) {
        // Arbitrary initial delay value
        this.task = scheduler.scheduleAtFixedRate(this, 30, interval, TimeUnit.SECONDS);
    }

    public void stopTask() {
        this.scheduler.shutdownNow();
    }
}
