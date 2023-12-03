import java.util.*;
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

            // The max number of neighbors is the minimum of the preferred and the number of interested
            int maxNeighbors = Math.min(this.numberOfPreferredNeighbors, interestedNeighbors.size());

            // If at least one neighbor is interested
            if (interestedNeighbors.size() > 0) {
                // If this peer has not completed all its pieces
                if (this.process.getNeighborsPieces().get(this.process.getPeerID()).cardinality != this.process.getPieceCount()) {
                    HashMap<String, Integer> downloadRates = new HashMap<>();
                    this.process.getConnectedNeighbors().forEach((key, value) -> {
                        downloadRates.put(key, value.getDownloadRate());
                    });

                    LinkedHashMap<String, Integer> sortedDownloadRates = sortMapByValuesDescending(downloadRates);
                    int counter = 0;
                    Iterator<Map.Entry<String, Integer>> iter = sortedDownloadRates.entrySet().iterator();
                    while (iter.hasNext() && counter < maxNeighbors) {
                        Map.Entry<String, Integer> entry = iter.next();

                    }


                }
                else {
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
                            if (this.process.getOptimisticUnchokeNeighbor() == null || !this.process.getOptimisticUnchokeNeighbor().equals(randomPeerID)) {
                                // Send an unchoke message
                                try {
                                    ActualMessage am = new ActualMessage(ActualMessage.MessageType.UNCHOKE);
                                    out.write(am.buildActualMessage());
                                    out.flush();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        // Reset the download rate of the PeerManager and then add the random peer ID to the new unchoked list
                        randomPeerManager.resetDownloadRate();
                        newUnchokedList.add(randomPeerID);
                    }
                }
            }
            else {

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LinkedHashMap<String, Integer> sortMapByValuesDescending(HashMap<String, Integer> hm) {
        // These lines take inspiration from the following source:
        // https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
        List<HashMap.Entry<String, Integer> > list =
                new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Collections.reverse(list);

        LinkedHashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }

        return temp;
    }
}
