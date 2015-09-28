package swen.adventure.network;


import java.io.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by David Barnett, Student ID 3003123764, on 19/09/15.
 */
public class Packet {

    public enum Operation {
        // FIXME: awful names
        // packets coming from the server
        CLIENT_CONNECT,
        CLIENT_DISCONNECT,
        CLIENT_DATA,

        // Mutual packet types
        PING,
        PONG,

        // Server packets going out
        SERVER_DATA,
        SERVER_KILL;

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
     * @param op Operation the the packet represents
     * @param payload raw data to be sent
     */
    public Packet(Operation op, byte[] payload) {
        this.op = op;
        if (payload.length > 65535) {
            throw new RuntimeException("Payload is too large");
        }
        this.payload = Arrays.copyOf(payload, payload.length);
    }

    public static Optional<Packet> fromBytes(byte[] raw) {
        ByteArrayInputStream bytes = new ByteArrayInputStream(raw);
        try {
            byte[] buffer = new byte[4];
            bytes.read(buffer, 0, 1);
            Operation op = Operation.fromByte(buffer[0]);

            // Use only two
            int length = bytes.read() << 8;
            length += bytes.read();

            if (bytes.available() < length) {
                System.err.println("Packet:FromByte: Advertised packet length (" + bytes.available() + ") does not match payload length " + length );
                return Optional.empty();
            }

            buffer = new byte[length];
            bytes.read(buffer);
            // TODO: Convert to Event

            return Optional.of(new Packet(op, buffer));
        } catch (IOException ex) {

        }
        return Optional.empty();
    }

    public byte[] toBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            bytes.write(new byte[]{op.toByte()});

            bytes.write((payload.length >> 8) & 255);
            bytes.write(payload.length & 255);
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
}
