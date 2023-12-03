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
        CHOKE, UNCHOKE, INTERESTED, NOT_INTERESTED, HAVE, BITFIELD, REQUEST, PIECE
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
            stream.write(this.messageType.ordinal());
            stream.write(this.messagePayload);
        } catch (Exception e) {
            System.err.println("Error while processing message payload: " + e.getMessage());
        }
        return stream.toByteArray();
    }

    public void readActualMessage(int len, byte[] message) {
        this.messageLength = len;
        //this.messageType = MessageType.values()[message[LENGTH_SIZE]]; // Correctly get MessageType
        //this.messageType = (char) message[0];
        this.messageType = switch ((char) message[0]) {
            case '0' -> ActualMessage.MessageType.CHOKE;
            case '1' -> ActualMessage.MessageType.UNCHOKE;
            case '2' -> ActualMessage.MessageType.INTERESTED;
            case '3' -> ActualMessage.MessageType.NOT_INTERESTED;
            case '4' -> ActualMessage.MessageType.HAVE;
            case '5' -> ActualMessage.MessageType.BITFIELD;
            case '6' -> ActualMessage.MessageType.REQUEST;
            case '7' -> ActualMessage.MessageType.PIECE;
            default -> null;
        };
        byte[] temp = new byte[this.messageLength - 1];
        System.arraycopy(message, 1, temp, 0, this.messageLength - 1);
        this.messagePayload = temp;
        // Adjust starting index for payload extraction
        //this.messagePayload = Arrays.copyOfRange(message, TYPE_SIZE, TYPE_SIZE + this.messageLength - LENGTH_SIZE - TYPE_SIZE);
    }

    public int extractIntFromByteArray(byte[] message, int start) {
        byte[] len = Arrays.copyOfRange(message, start, start + LENGTH_SIZE);
        return ByteBuffer.wrap(len).getInt();
    }

    public BitSet getBitFieldMessage() {
        return BitSet.valueOf(this.messagePayload);
    }

    public int getPieceIndexFromPayload() {
        return extractIntFromByteArray(this.messagePayload, 0);
    }

    public byte[] getPieceFromPayload() {
        // Adjust the size calculation to consider the new messageLength calculation
        int headerSize = LENGTH_SIZE + TYPE_SIZE; // Total size of the length and type headers
        int size = this.messageLength - headerSize;
        byte[] piece = new byte[size];
        System.arraycopy(this.messagePayload, 0, piece, 0, size);
        return piece;
    }

}