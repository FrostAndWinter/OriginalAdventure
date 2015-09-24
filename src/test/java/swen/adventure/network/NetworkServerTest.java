package swen.adventure.network;

import junit.framework.TestCase;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by David Barnett, Student ID 3003123764, on 17/09/15.
 */
public class NetworkServerTest extends TestCase {

    @Test
    public void testStart() throws Exception {
        Server srv = new NetworkServer();
        try {
            srv.start(61451);
        } catch (IOException ex) {
            Assume.assumeNoException(ex);
            return;
        }
        assertTrue(srv.isRunning());
        srv.stop();
    }

    @Test
    public void testStartAfterStarted() throws Exception {
        Server srv = new NetworkServer();
        try {
            srv.start(61452);
        } catch (IOException ex) {
            Assume.assumeNoException(ex);
            return;
        }
        assertTrue(srv.isRunning());

        try {
            srv.start(61452); // expected to error here
            fail();
        } catch (RuntimeException ex) {
        }

        srv.stop();
    }

    @Test
    public void testStop() throws Exception {
        Server srv = new NetworkServer();
        try {
            srv.start(61453);
        } catch (IOException ex) {
            Assume.assumeNoException(ex);
            return;
        }

        assertTrue(srv.isRunning());

        srv.stop();

        assertFalse(srv.isRunning());
    }
}