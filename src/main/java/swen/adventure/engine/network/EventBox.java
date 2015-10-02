package swen.adventure.engine.network;

import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.SceneNode;

import java.util.Collections;
import java.util.Map;

/**
 * Created by David Barnett, Student ID 3003123764, on 01/10/15.
 */
public class EventBox {
    /**
     * The name of the event that has been triggered
     */
    public final String eventName;

    /**
     * The ID  of the object that is triggering the event
     */
    public final String sourceId;

    /**
     * The ID of the object that is having the action preformed on it
     */
    public final String targetId;

    /**
     * The network id that sent the EventBox
     */
    public final String from;

    /**
     * The eventData of arguments names to values of the event
     */
    public final Map<String, Object> eventData;

    public EventBox(String eventName, String sourceId, String targetId, String from, Map<String, Object> eventData) {
        this.eventName = eventName;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.from = from;
        this.eventData = eventData;
    }

    /**
     * Convert bytes to an EventBox
     *
     * @param raw A byte array that represents an EventBox
     * @return An EventBox using given data
     */
    public static EventBox fromBytes(byte[] raw) {
        String[] parts = new String(raw).split(":");
        // TODO: Parse eventData from bytes
        return new EventBox(parts[0], parts[1], parts[2], parts[3], Collections.EMPTY_MAP);
    }

    /**
     * Converts to bytes
     *
     * @return array of bytes that represent an EventBox
     */
    public byte[] getBytes() {
        // TODO: Parse eventData to bytes
        return String.join(":", new String[] {eventName, sourceId, targetId, from}).getBytes();
    }

    public static <E> EventBox build(SceneNode source, Event<E> event, SceneNode target, Map<String,Object> data) {
        // FIXME: assumes source.id is a network client
        return new EventBox(event.name, source.id, target.id, source.id, data);
    }

    @Override
    public String toString() {
        return "EventBox{" +
                "eventName='" + eventName + '\'' +
                ", sourceId='" + sourceId + '\'' +
                ", targetId='" + targetId + '\'' +
                ", from='" + from + '\'' +
                ", eventData=" + eventData +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventBox eventBox = (EventBox) o;

        if (eventName != null ? !eventName.equals(eventBox.eventName) : eventBox.eventName != null) return false;
        if (sourceId != null ? !sourceId.equals(eventBox.sourceId) : eventBox.sourceId != null) return false;
        if (targetId != null ? !targetId.equals(eventBox.targetId) : eventBox.targetId != null) return false;
        if (from != null ? !from.equals(eventBox.from) : eventBox.from != null) return false;
        return !(eventData != null ? !eventData.equals(eventBox.eventData) : eventBox.eventData != null);

    }

    @Override
    public int hashCode() {
        int result = eventName != null ? eventName.hashCode() : 0;
        result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
        result = 31 * result + (targetId != null ? targetId.hashCode() : 0);
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (eventData != null ? eventData.hashCode() : 0);
        return result;
    }
}
