package swen.adventure.network;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by David Barnett, Student ID 3003123764, on 27/09/15.
 */
public class PacketTest extends TestCase {

    @Test
    public void testOverSizedPacket() throws Exception {
        try {
            new Packet(Packet.Operation.CLIENT_CONNECT, new byte[(int) Math.pow(2, 16)]);
            fail();
        } catch (RuntimeException ex) {
        }
    }

    @Test
    public void testFromBytes() throws Exception {
        Optional<Packet> p = Packet.fromBytes(new byte[]{0, 0, 1, 10});

        assertTrue(p.isPresent());
        Packet packet = p.get();

        assertEquals(Packet.Operation.fromByte((byte)0), packet.getOperation());

        assertEquals(1, packet.getPayload().length);

        assertTrue(Arrays.equals(new byte[] {10}, packet.getPayload()));
    }

    @Test
    public void testToBytes() throws Exception {
        Packet packet = new Packet(Packet.Operation.CLIENT_CONNECT, new byte[] {1 , 2 , 3});

        assertTrue(Arrays.equals(new byte[] {
                Packet.Operation.CLIENT_CONNECT.toByte(), 0, 3, 1 , 2 , 3
        }, packet.toBytes()));
    }

    @Test
    public void testGetPayload() throws Exception {
        Packet packet = new Packet(Packet.Operation.CLIENT_CONNECT, new byte[] {1 , 2 , 3});

        assertTrue(Arrays.equals(new byte[] {1 , 2 , 3 }, packet.getPayload()));
    }

    @Test
    public void testGetOperation() throws Exception {
        Packet packet = new Packet(Packet.Operation.CLIENT_CONNECT, new byte[] {1 , 2 , 3});

        assertEquals(Packet.Operation.CLIENT_CONNECT, packet.getOperation());
    }
}