package swen.adventure.scenegraph;

import swen.adventure.utils.BoundingBox;
import swen.adventure.Event;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class GameObject extends SceneNode {

    private BoundingBox _boundingBox;

    public GameObject(String id, final SceneNode parent) {
        super(id, parent, true);
    }

    public BoundingBox boundingBox() {
        return _boundingBox;
    }

    /**
     * Given an event name in UpperCamelCase, finds and returns the event instance associated with that name on this object.
     * The field is expected to be named in the form event{eventName}.
     * @param eventName The name of the event e.g. DoorOpened.
     * @return The event for that name on this object.
     * @throws RuntimeException if the event does not exist on this object.
     */
    public Event<? extends GameObject> eventWithName(String eventName) {
        try {
            Field field = this.getClass().getField("event" + eventName);
            return (Event<? extends GameObject>) field.get(this);
        } catch (IllegalAccessException e) {
            System.err.println("Error accessing event with name " + eventName + ": " + e);
        } catch (NoSuchFieldException e) {
        }

        throw new RuntimeException("Could not find an event of name " + eventName + " on " + this);
    }
}
