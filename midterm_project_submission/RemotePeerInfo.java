/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */
public class RemotePeerInfo {
        private String peerId;
        private String peerAddress;
        private int peerPort;
        private boolean containsFile;

        public RemotePeerInfo(String pId, String pAddress, String pPort) {
                this.peerId = pId;
                this.peerAddress = pAddress;
                this.peerPort = parsePort(pPort);
        }

        public RemotePeerInfo(String pId, String pAddress, String pPort, String cFile) {
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

