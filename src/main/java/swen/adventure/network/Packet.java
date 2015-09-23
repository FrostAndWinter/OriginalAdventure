package swen.adventure.network;

import java.io.*;
import java.lang.reflect.Array;
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

    public Packet(Operation op, byte[] payload) {
        this.op = op;
        this.payload = Arrays.copyOf(payload, payload.length);
    }

    public static Optional<Packet> fromBytes(byte[] raw) {
        ByteArrayInputStream bytes = new ByteArrayInputStream(raw);
        try {
            byte[] buffer = new byte[4];
            bytes.read(buffer, 0, 1);
            Operation op = Operation.fromByte(buffer[0]);

            int length = bytes.read(); // FIXME THIS IS ONLY ONE BYTE!
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

            bytes.write(payload.length); // FIXME THIS IS ONLY ONE BYTE!
            bytes.write(payload);
        } catch (IOException ex) {

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
