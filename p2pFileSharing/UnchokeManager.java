import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UnchokeManager implements Runnable {
    private PeerProcess process;
    private int unchokingInterval;
    private int numberOfPreferredNeighbors;
    private Random rnd = new Random();
    private ScheduledFuture<?> task = null;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    UnchokeManager(PeerProcess process) {
        this.process = process;
        this.unchokingInterval = process.getCommonConfig().getUnchokingInterval();
        this.numberOfPreferredNeighbors = process.getCommonConfig().getNumberOfPreferredNeighbors();
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
                if (this.process.getNeighborsPieces().get(this.process.getPeerID()).cardinality() != this.process.getPieceCount()) {
                    HashMap<String, Integer> downloadRates = new HashMap<>();
                    this.process.getConnectedNeighbors().forEach((key, value) -> {
                        downloadRates.put(key, value.getDownloadRate());
                    });

                    LinkedHashMap<String, Integer> sortedDownloadRates = sortMapByValuesDescending(downloadRates);
                    int counter = 0;
                    Iterator<Map.Entry<String, Integer>> iter = sortedDownloadRates.entrySet().iterator();

                    while (iter.hasNext() && counter < maxNeighbors) {
                        Map.Entry<String, Integer> entry = iter.next();
                        String peerID = entry.getKey();

                        if (interestedNeighbors.contains(peerID)) {
                            // Get the PeerManager associated with the interested peerID
                            PeerManager pm = this.process.getConnectedNeighbors().get(peerID);

                            if (oldUnchokedList.contains(peerID)) {
                                oldUnchokedList.remove(peerID);
                            }
                            else {
                                String optimisticUnchokeNeighbor = this.process.getOptimisticUnchokeNeighbor();
                                if (!optimisticUnchokeNeighbor.equals(peerID) || optimisticUnchokeNeighbor == null) {
                                    pm.sendMsg(ActualMessage.MessageType.UNCHOKE);
                                }
                            }

                            newUnchokedList.add(peerID);
                            pm.resetDownloadRate();
                            counter += 1;
                        }
                    }

                    // Update the unchoked neighbors in the process with the new list
                    this.process.setUnchokedNeighbors(newUnchokedList);

                    // If the new unchoked list is not empty
                    if (!newUnchokedList.isEmpty()) {
                        // Log the changed preferred neighbors
                        this.process.getPeerLogger().changePreferNeighborLog(new ArrayList<>(newUnchokedList));
                    }

                    // For each peerID in the old unchoked list, send a choke message
                    oldUnchokedList.forEach((peerID) -> {
                        PeerManager pm = this.process.getConnectedNeighbors().get(peerID);
                        pm.sendMsg(ActualMessage.MessageType.CHOKE);
                    });
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
                                    randomPeerManager.sendMsg(ActualMessage.MessageType.UNCHOKE);
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
                this.process.clearUnchokedNeighbors();

                // For each peerID in the old unchoked list, send a choke message
                oldUnchokedList.forEach((peerID) -> {
                    PeerManager pm = this.process.getConnectedNeighbors().get(peerID);
                    pm.sendMsg(ActualMessage.MessageType.CHOKE);
                });

                if (this.process.allFinished()) {
                    // TODO stop all the choking
                }
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

    public void startTask() {
        this.task = this.scheduler.scheduleAtFixedRate(this, 10, this.unchokingInterval, TimeUnit.SECONDS);
    }
    public void stopTask() {
        this.scheduler.shutdownNow();
    }
}
