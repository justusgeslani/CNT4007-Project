import java.nio.charset.StandardCharsets;

public class HandShakeMessage {
    /**
     * The handshake consists of three parts; [ Header  |  Zero Bits  | Peer ID ]
     * The length of the handshake is always 32 bytes:
     * - 18-bytes string header
     * - 10 byte zero bits
     * - 4 byte PeerId, which is the integer representation of the peer ID.
     */
    private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    private static final int HEADER_LENGTH = 18;    // Length of HANDSHAKE_HEADER
    private static final int ZERO_BITS_LENGTH = 10; // Length of zero bits
    private static final int PEER_ID_LENGTH = 4;    // Length of peer ID

    private final String peerID;

    public HandShakeMessage(String peerID) {
        // Error Checking as per project specifications.
        if (peerID.length() != PEER_ID_LENGTH) {
            throw new IllegalArgumentException("PeerID must be 4 bytes in length.");
        }
        this.peerID = peerID;
    }

    public String getPeerID() {
        return this.peerID;
    }

    public byte[] buildHandShakeMessage() {
        byte[] handshakeBytes = new byte[HEADER_LENGTH + ZERO_BITS_LENGTH + PEER_ID_LENGTH];

        // Copy the header bytes
        System.arraycopy(HANDSHAKE_HEADER.getBytes(), 0, handshakeBytes, 0, HEADER_LENGTH);

        // Copy the peerID bytes
        byte[] peerIdBytes = peerID.getBytes();
        System.arraycopy(peerIdBytes, 0, handshakeBytes, HEADER_LENGTH + ZERO_BITS_LENGTH, PEER_ID_LENGTH);

        return handshakeBytes;
    }

    public static HandShakeMessage readHandShakeMessage(byte[] messageBytes) {
        if (messageBytes.length != HEADER_LENGTH + ZERO_BITS_LENGTH + PEER_ID_LENGTH) {
            throw new IllegalArgumentException("Invalid handshake message length");
        }

        // Extract and validate the header
        byte[] headerBytes = new byte[HEADER_LENGTH];
        System.arraycopy(messageBytes, 0, headerBytes, 0, HEADER_LENGTH);
        String header = new String(headerBytes, StandardCharsets.UTF_8);
        if (!HANDSHAKE_HEADER.equals(header)) {
            throw new IllegalArgumentException("Invalid handshake header");
        }

        // Validate the zero bits
        for (int i = HEADER_LENGTH; i < HEADER_LENGTH + ZERO_BITS_LENGTH; i++) {
            if (messageBytes[i] != 0) {
                throw new IllegalArgumentException("Handshake zero bits are not all zeros");
            }
        }

        // Extract the peer ID
        byte[] peerIdBytes = new byte[PEER_ID_LENGTH];
        System.arraycopy(messageBytes, HEADER_LENGTH + ZERO_BITS_LENGTH, peerIdBytes, 0, PEER_ID_LENGTH);
        String peerID = new String(peerIdBytes, StandardCharsets.UTF_8);

        System.out.println();
        System.out.println("---------- Handshake Message ----------");
        System.out.println("Handshake header: " + header);
        String zerobits = "";
        for(int i = HEADER_LENGTH; i < HEADER_LENGTH + ZERO_BITS_LENGTH; i++) {
            zerobits += messageBytes[i];
        }
        System.out.println("Zero bits: " + zerobits);
        System.out.println("Peer ID: " + peerID);

        return new HandShakeMessage(peerID);
    }
}
