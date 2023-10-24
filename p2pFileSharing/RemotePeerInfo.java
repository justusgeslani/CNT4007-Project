/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

public class RemotePeerInfo {
        public String peerId;
        public String peerAddress;
        public int peerPort;
        public int containsFile;

        public RemotePeerInfo(String pId, String pAddress, String pPort) {
                this.peerId = pId;
                this.peerAddress = pAddress;
                this.peerPort = Integer.parseInt(pPort);
        }
        public RemotePeerInfo(String pId, String pAddress, String pPort, String cFile) {
                this.peerId = pId;
                this.peerAddress = pAddress;
                this.peerPort = Integer.parseInt(pPort);
                this.containsFile = Integer.parseInt(cFile);
        }
}
