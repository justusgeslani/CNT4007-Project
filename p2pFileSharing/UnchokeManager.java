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
            // get a list of the unchoked neighbors
            // create a new list for the unchoked neighbors


        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
