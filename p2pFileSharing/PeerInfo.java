public class PeerInfo {
        private final String peerId;
        private final String peerAddress;
        private final int peerPort;
        private boolean containsFile;

        public PeerInfo(String pId, String pAddress, String pPort) {
                this.peerId = pId;
                this.peerAddress = pAddress;
                this.peerPort = parsePort(pPort);
        }

        public PeerInfo(String pId, String pAddress, String pPort, String cFile) {
                this.peerId = pId;
                this.peerAddress = pAddress;
                this.peerPort = parsePort(pPort);
                this.containsFile = "1".equals(cFile);
        }

        public String getPeerId() {
                return peerId;
        }

        public String getPeerAddress() {
                return peerAddress;
        }

        public int getPeerPort() {
                return peerPort;
        }

        public boolean containsFile() {
                return containsFile;
        }

        private int parsePort(String pPort) {
                try {
                        return Integer.parseInt(pPort);
                } catch (NumberFormatException e) {
                        System.err.println("Invalid port number: " + pPort);
                        return -1;
                }
        }
}

