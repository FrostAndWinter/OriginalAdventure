package swen.adventure.engine.network;


import java.io.*;
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
        CLIENT_CONNECT,
        CLIENT_KICK,
        CLIENT_DATA,

        // Mutual packet types
        PING,
        PONG,

        // Server packets going out
        SERVER_DATA,
        SERVER_KILL,
        SNAPSHOT;

        public byte toByte() {
            byte b = 0;
            for (Operation op :Operation.values()) {
                if (this == op) {
                    break;
                }
                b++;

            }
            return b;
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
     * The maximum length of the pay load is 65535 bytes due to the implementation of the header
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
            ex.printStackTrace();
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
                // Got OP code
                op = Optional.of(Operation.fromByte(b));
            } else if (lengthCounter < 4) {
                lengthBytes[lengthCounter] = b;

                lengthCounter++;
                if (lengthCounter == 4) {
                    // BigInt for the rescue
                    length = new BigInteger(lengthBytes).intValue();
                    payload = new byte[length];
                }
            } else if (processed < length) {
                payload[processed++] = b;
            } else {
                overflow.add(b);
            }
        }

        List<Byte> overflow() {
           return overflow;
        }

        boolean isReady() {
            return lengthCounter == 4 && processed == length && op.isPresent();
        }

        Packet build() {
            return new Packet(op.get(), payload);
        }

    }
}
