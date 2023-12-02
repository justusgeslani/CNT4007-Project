public class PeerConfigManager {
    private final CommonConfig commonConfig;
    private final PeerInfoManager peerInfoConfig;

    public PeerConfigManager() {
        this.commonConfig = new CommonConfig();
        this.peerInfoConfig = new PeerInfoManager();
        loadConfigurations();
    }

    private void loadConfigurations() {
        this.commonConfig.loadConfigFile();
        this.peerInfoConfig.loadConfigFile();
    }

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }

    public PeerInfoManager getPeerInfoConfig() {
        return peerInfoConfig;
    }
}
