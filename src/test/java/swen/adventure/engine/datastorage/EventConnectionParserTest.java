package swen.adventure.engine.datastorage;

import org.junit.Before;
import org.junit.Test;
import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * Basic tests to make sure the event connection parser can set up connections between events and actions. There is
 * no error handling at the moment.
 *
 * @author Joseph Bennett 300319773
 */
public class EventConnectionParserTest {

    private TransformNode sceneRoot;

    @Before
    public void setup() {
        sceneRoot = new TransformNode("root", Vector3.zero, new Quaternion(), Vector3.one);
    }

    @Test
    public void testCanCreateCorrectEventConnectionFromValidSyntax() {
        // sound a house alarm when either the main entrance door or the secret safe door is opened
        String eventConnectionLine = "DoorOpened; MainEntranceDoor, SecretSafeDoor; SoundAlarm; HouseAlarm";

        // parse the event connection line
        EventConnectionParser.EventConnection eventConnection = EventConnectionParser.parseLine(eventConnectionLine);

        // check everything was put in the correct place
        assertEquals("DoorOpened", eventConnection.eventName);
        assertEquals("SoundAlarm", eventConnection.actionName);
        assertEquals(testList("MainEntranceDoor", "SecretSafeDoor"), eventConnection.objectNames);
        assertEquals(testList("HouseAlarm"), eventConnection.listenerNames);
    }

    @Test
    public void testConnectionsAreCorrectlySetupFromEventConnectionObject() {
        Door mainEntranceDoor = new Door("MainEntranceDoor", sceneRoot, true);
        Door secretSafeDoor = new Door("SecretSafeDoor", sceneRoot, true);
        Alarm houseAlarm = new Alarm("HouseAlarm", sceneRoot, true);

        // connection that sounds a house alarm when either the main entrance door or the secret safe door is opened
        EventConnectionParser.EventConnection eventConnection =
                new EventConnectionParser.EventConnection("DoorOpened",
                        testList("MainEntranceDoor", "SecretSafeDoor"),
                        "SoundAlarm",
                        testList("HouseAlarm")
                );

        EventConnectionParser.setupConnections(testList(eventConnection), sceneRoot);

        mainEntranceDoor.openDoor();
        secretSafeDoor.openDoor();
        assertTrue(houseAlarm.isSounding());
    }

    private static <T> List<T> testList(T... listItems) {
        return Arrays.asList(listItems);
    }

    public static class Door extends SceneNode {

        public final Event<Door, Door> eventDoorOpened = new Event<>("DoorOpened", this);

        public Door(String id) {
            super(id);
        }

        public Door(String id, TransformNode parent, boolean isDynamic) {
            super(id, parent, isDynamic);
        }


        public void openDoor() {
            eventDoorOpened.trigger(this, Collections.emptyMap());
        }
    }

    public static class Alarm extends SceneNode {

        public final Action<Door, Door, Alarm> actionSoundAlarm =
                (door, triggeringObject, alarm, data) -> alarm.soundAlarm();

        private boolean isSounding = false;

        public Alarm(String id) {
            super(id);
        }

        public Alarm(String id, TransformNode parent, boolean isDynamic) {
            super(id, parent, isDynamic);
        }


        public void soundAlarm() {
            isSounding = true;
        }

        public boolean isSounding() {
            return isSounding;
        }
    }
}
