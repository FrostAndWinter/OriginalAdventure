/* Contributor List  */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 package swen.adventure.engine.network;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by David Barnett, Student ID 3003123764, on 12/10/15.
 */
public class EventBoxTest extends TestCase {

    @Test
    public void testFromBytes() throws Exception {
        EventBox box = EventBox.fromBytes(new byte[]{116,101,115,116,33,116,101,115,116,101,114,33,116,101,115,116,105,101,33,102,114,111,109,58,118,33,106,97,118,97,46,108,97,110,103,46,83,116,114,105,110,103,33,107,58});

        assertEquals("test",
                box.eventName);
        assertEquals("tester",
                box.sourceId);
        assertEquals("testie",
                box.targetId);
        assertEquals("from",
                box.from);
        assertEquals("k",
                box.eventData.get("v"));
    }

    @Test
    public void testGetBytes() throws Exception {
        EventBox box = new EventBox("test", "tester", "testie", "from", Collections.singletonMap("v", "k"));

        Assert.assertArrayEquals(
                new byte[]{116,101,115,116,33,116,101,115,116,101,114,33,116,101,115,116,105,101,33,102,114,111,109,58,118,33,106,97,118,97,46,108,97,110,103,46,83,116,114,105,110,103,33,107,58},
                box.getBytes());
    }
}