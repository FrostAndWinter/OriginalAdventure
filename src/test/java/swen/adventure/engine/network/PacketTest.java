package swen.adventure.engine.network;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by David Barnett, Student ID 3003123764, on 27/09/15.
 */
public class PacketTest extends TestCase {

    @Test
    public void testToBytes() throws Exception {
        Packet packet = new Packet(Packet.Operation.CLIENT_CONNECT, new byte[] {1 , 2 , 3});

        assertTrue(Arrays.equals(new byte[] {
                Packet.Operation.CLIENT_CONNECT.toByte(),0 ,0, 0, 3, 1 , 2 , 3
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