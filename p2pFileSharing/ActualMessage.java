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
        this.messageType = MessageType.values()[extractMessageType(message)];
        this.messagePayload = extractPayload(message, TYPE_SIZE);
    }

    public int extractIntFromByteArray(byte[] message, int start) {
        byte[] len = Arrays.copyOfRange(message, start, start + LENGTH_SIZE);
        return ByteBuffer.wrap(len).getInt();
    }

    public int extractMessageType(byte[] message) {
        return message[LENGTH_SIZE];
    }

    public byte[] extractPayload(byte[] message, int start) {
        return Arrays.copyOfRange(message, start, start + this.messageLength - LENGTH_SIZE - TYPE_SIZE);
    }

    public BitSet getBitFieldMessage() {
        return BitSet.valueOf(this.messagePayload);
    }

    public int getPieceIndexFromPayload() {
        return extractIntFromByteArray(this.messagePayload, 0);
    }

    public byte[] getPieceFromPayload() {
        return Arrays.copyOfRange(this.messagePayload, LENGTH_SIZE, this.messagePayload.length);
    }

    public MessageType getMessageType() {
        return this.messageType;
    }
}
