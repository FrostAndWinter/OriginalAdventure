/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 package swen.adventure.engine.network;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by David Barnett, Student ID 3003123764, on 19/09/15.
 */
class Packet {

    public enum Operation {
        // FIXME: awful names
        // packets coming from the server
        /**
         * Represents a client has connected to the server
         */
        CLIENT_CONNECT,
        /**
         * Represents a client been removed by the server
         */
        CLIENT_KICK,
        /**
         * Represents data from a client
         */
        CLIENT_DATA,

        // Server packets going out
        /**
         * Represents data from the server
         */
        SERVER_DATA,
        SERVER_KILL,
        SNAPSHOT;

        public byte toByte() {
            return (byte)this.ordinal();
        }

        public static Operation fromByte(byte b) {
            return Operation.values()[b]; // should be ffiinnee
        }
    }

    private final Operation op;
    private final byte[] payload;

    /**
     * Create a simple network packet with only an operation
     *
     * @param op Operation the the packet represents
     */
    public Packet(Operation op) {
        this.op = op;
        payload = new byte[0];
    }

    /**
     * Create a packet to be sent over the network
     *
     * The maximum length of the pay load is 2^31-1 bytes due to the implementation of the header
     * It is advised to use have the payload smaller than the maximum to keep in bounds of TCP send/recieve
     * buffers
     *
     * Note: Ownership of the payload data belongs to the packet
     *
     * @param op Operation the the packet represents
     * @param payload raw data to be sent
     */
    public Packet(Operation op, byte[] payload) {
        this.op = op;
        this.payload = payload;
    }

    /**
     * Convert the a given Packet to a byte array ready to be sent
     * over the network or saved.
     *
     * @return a byte array that Packet.fromBytes can covert to a Packet
     */
    public byte[] toBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            bytes.write(new byte[]{op.toByte()});

            // Ensure we use 4 bytes to represent an int & in correct order that BigInt will read it in
            byte[] lengthBytes = new byte[4];
            byte[] b = BigInteger.valueOf(payload.length).toByteArray();
            for (int i = 1; i <= b.length; i++ ) {
                lengthBytes[lengthBytes.length - i] = b[b.length - i];
            }
            bytes.write(lengthBytes);
            bytes.write(payload);
        } catch (IOException ex) {
            // muffu muffu~
        }
        return bytes.toByteArray();
    }

    public byte[] getPayload() {
        return payload;
    }

    public Operation getOperation() {
        return op;
    }

    static class Builder {
        int lengthCounter = 0;
        byte[] lengthBytes = new byte[4];
        byte[] payload = null;
        final List<Byte> overflow = new ArrayList<>();

        Optional<Operation> op = Optional.empty();
        int length = 0;
        int processed = 0;

        Builder append(List<Byte> data) {
            data.forEach(this::append);
            return this;
        }

        Builder append(byte[] data) {
            for (byte b : data) {
                append(b);
            }
            return this;
        }

        void append(byte b) {
            if (!op.isPresent()) {
                // Get OP code
                op = Optional.of(Operation.fromByte(b));
            } else if (lengthCounter < 4) {

                // Get the length of the buffer
                lengthBytes[lengthCounter] = b;

                lengthCounter++;
                if (lengthCounter == 4) {
                    // got all the bytes of the integer
                    // BigInt for the rescue
                    length = new BigInteger(lengthBytes).intValue();
                    payload = new byte[length];
                }
            } else if (processed < length) {
                // build payload
                payload[processed++] = b;
            } else {
                overflow.add(b);
            }
        }

        /**
         * The data in the builder that was not used to build the packet
         * @return List of bytes that was added to the builder in order of arrival
         */
        List<Byte> overflow() {
           return overflow;
        }

        boolean isReady() {
            return lengthCounter == 4 && processed == length && op.isPresent();
        }

        /**
         * Build the packet from the data
         *
         * @throws IllegalStateException if the builder is not ready to build the packet
         * @return A built packet from the data given
         */
        Packet build() {
            if (!isReady()) {
                throw new IllegalStateException("Cannot build a packet when it is not ready");
            }
            return new Packet(op.get(), payload);
        }

    }
}