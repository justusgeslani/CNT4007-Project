import java.util.*;
import java.io.*;
import java.nio.*;

public class ActualMessage {
    private static final int LENGTH_SIZE = 4;
    private static final int TYPE_SIZE = 1;

    /**
     * Represents the length of the message in bytes.
     * Note: This does not include the length of the message length field itself.
     */
    private int messageLength;
    /**
     * Represents the type of the message.
     */
    private MessageType messageType;
    /**
     * The payload of the message.
     */
    private byte[] messagePayload;

    public enum MessageType {
        CHOKE, UNCHOKE, INTERESTED, NOT_INTERESTED, HAVE, BITFIELD, REQUEST, PIECE;

        public static MessageType fromByte(byte b) {
            return MessageType.values()[b];
        }

        public byte toByte() {
            return (byte) this.ordinal();
        }
    }

    public ActualMessage(MessageType messageType) {
        this.messageType = messageType;
        this.messageLength = TYPE_SIZE;
        this.messagePayload = new byte[0];
    }

    public ActualMessage(MessageType messageType, byte[] messagePayload) {
        this.messageType = messageType;
        this.messagePayload = messagePayload;
        this.messageLength = TYPE_SIZE + this.messagePayload.length;
    }

    public byte[] buildActualMessage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            byte[] bytes = ByteBuffer.allocate(LENGTH_SIZE).putInt(this.messageLength).array();
            stream.write(bytes);
            stream.write(this.messageType.toByte());
            stream.write(this.messagePayload);
        } catch (Exception e) {
            System.err.println("Error while processing message payload: " + e.getMessage());
        }
        return stream.toByteArray();
    }

    public void readActualMessage(int len, byte[] message) {
        try {
            if (message == null || message.length < TYPE_SIZE) {
                throw new IllegalArgumentException("Invalid message array.");
            }
            this.messageLength = len;
            this.messageType = MessageType.fromByte(message[0]);
            if (this.messageLength < TYPE_SIZE) {
                throw new IllegalArgumentException("Message length is too short.");
            }
            this.messagePayload = new byte[this.messageLength - TYPE_SIZE];
            System.arraycopy(message, TYPE_SIZE, this.messagePayload, 0, this.messageLength - TYPE_SIZE);
        } catch (Exception e) {
            System.out.println("Error reading actual message");
            e.printStackTrace();
        }
    }

    public int extractIntFromByteArray(byte[] message, int start) {
        try {
            if (message == null || message.length < start + LENGTH_SIZE) {
                throw new IllegalArgumentException("Invalid message array or start index.");
            }
            byte[] len = Arrays.copyOfRange(message, start, start + LENGTH_SIZE);
            return ByteBuffer.wrap(len).getInt();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public BitSet getBitFieldMessage() {
        try {
            if (this.messagePayload == null) {
                throw new IllegalStateException("Payload is not set.");
            }
            return BitSet.valueOf(this.messagePayload);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getPieceIndexFromPayload() {
        try {
            return extractIntFromByteArray(this.messagePayload, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public byte[] getPieceFromPayload() {
        try {
            // Adjust the size calculation to consider the new messageLength calculation
            int headerSize = LENGTH_SIZE + TYPE_SIZE; // Total size of the length and type headers
            int size = this.messageLength - headerSize;
            if (size < 0 || size > this.messagePayload.length) {
                throw new IllegalArgumentException("Invalid size calculation.");
            }
            byte[] piece = new byte[size];
            System.arraycopy(this.messagePayload, 0, piece, 0, size);
            return piece;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Or handle the error as appropriate for your application
        }
    }

}