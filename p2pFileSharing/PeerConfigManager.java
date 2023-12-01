public class PeerConfigManager {
    private final CommonConfig commonConfig;
    private final PeerInfoConfig peerInfoConfig;

    public PeerConfigManager() {
        this.commonConfig = new CommonConfig();
        this.peerInfoConfig = new PeerInfoConfig();
        loadConfigurations();
    }

    private void loadConfigurations() {
        this.commonConfig.loadConfigFile();
        this.peerInfoConfig.loadConfigFile();
    }

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }

    public PeerInfoConfig getPeerInfoConfig() {
        return peerInfoConfig;
    }
}
