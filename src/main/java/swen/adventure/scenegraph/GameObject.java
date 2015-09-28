package swen.adventure.scenegraph;

import swen.adventure.Event;
import swen.adventure.utils.BoundingBox;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class GameObject extends SceneNode {

    private Optional<BoundingBox> _localSpaceBoundingBox = Optional.empty();

    public GameObject(String id, final TransformNode parent) {
        super(id, parent, true);
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

    private boolean _isRunningBoundingBoxSearch = false;

    @Override
    public Optional<BoundingBox> boundingBox() {
        if (_localSpaceBoundingBox.isPresent() || _isRunningBoundingBoxSearch) {
            return _localSpaceBoundingBox;
        } else {
            _isRunningBoundingBoxSearch = true;
            Optional<BoundingBox> box = this.parent().isPresent() ? this.parent().get().boundingBox() : Optional.empty();
            _isRunningBoundingBoxSearch = false;
            return box;
        }
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        _localSpaceBoundingBox = Optional.of(boundingBox);
    }
}
