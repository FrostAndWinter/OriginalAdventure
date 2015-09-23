package swen.adventure.scenegraph;

import swen.adventure.utils.BoundingBox;
import swen.adventure.Event;

import java.lang.reflect.Field;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class GameObject extends SceneNode {

    private boolean _needsRecalculateBoundingBox = false;
    private BoundingBox _boundingBox;
    private BoundingBox _transformedBoundingBox;

    public GameObject(String id, final TransformNode parent) {
        super(id, parent, true);
    }

    public BoundingBox boundingBox() {
        if (_needsRecalculateBoundingBox) {
            _transformedBoundingBox = _boundingBox.transformByMatrix(this.nodeToWorldSpaceTransform());
        }
        return _transformedBoundingBox;
    }

    public void transformDidChange() {
        _needsRecalculateBoundingBox = true;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameObject that = (GameObject) o;

        if (_needsRecalculateBoundingBox != that._needsRecalculateBoundingBox) return false;
        if (_boundingBox != null ? !_boundingBox.equals(that._boundingBox) : that._boundingBox != null) return false;
        return !(_transformedBoundingBox != null ? !_transformedBoundingBox.equals(that._transformedBoundingBox) : that._transformedBoundingBox != null);

    }

    @Override
    public int hashCode() {
        int result = (_needsRecalculateBoundingBox ? 1 : 0);
        result = 31 * result + (_boundingBox != null ? _boundingBox.hashCode() : 0);
        result = 31 * result + (_transformedBoundingBox != null ? _transformedBoundingBox.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GameObject{" +
                "_boundingBox=" + _boundingBox +
                ", _needsRecalculateBoundingBox=" + _needsRecalculateBoundingBox +
                ", _transformedBoundingBox=" + _transformedBoundingBox +
                '}';
    }
}
