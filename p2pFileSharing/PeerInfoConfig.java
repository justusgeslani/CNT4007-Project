import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PeerInfoConfig {

    private final HashMap<String, RemotePeerInfo> peerInfoMap;
    private final ArrayList<String> peerList;

    public PeerInfoConfig() {
        this.peerInfoMap = new HashMap<>();
        this.peerList = new ArrayList<>();
    }

    public void loadConfigFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("PeerInfo.cfg"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.trim().split("\\s+");
                if (details.length == 4) {
                    RemotePeerInfo peerInfo = new RemotePeerInfo(details[0], details[1], details[2], details[3]);
                    peerInfoMap.put(details[0], peerInfo);
                    peerList.add(details[0]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading PeerInfo.cfg: " + e.getMessage());
        }
    }

    public RemotePeerInfo getPeerConfig(String peerID) {
        return peerInfoMap.get(peerID);
    }

    public HashMap<String, RemotePeerInfo> getPeerInfoMap() {
        return new HashMap<>(peerInfoMap);
    }

    public ArrayList<String> getPeerList() {
        return new ArrayList<>(peerList);
    }
}
