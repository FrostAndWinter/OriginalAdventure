package swen.adventure.engine.network;

import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.ParserManager;
import swen.adventure.engine.scenegraph.SceneNode;

import java.util.HashMap;
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

    private final static ParserManager PARSER = new ParserManager();
    private final static String END_LINE = ":";
    private final static String SEPARATORS = "!";


    public EventBox(String eventName, SceneNode source, SceneNode target, SceneNode from, Map<String, Object> eventData) {
        this.eventName = eventName;
        this.sourceId = source.id;
        this.targetId = target.id;
        this.from = from.id;
        this.eventData = eventData;
    }


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
        String[] lines = new String(raw).split(END_LINE);
        String[] parts = lines[0].split(SEPARATORS);
        Map<String, Object> objectMap = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] obj = lines[i].split(SEPARATORS);
            try {
                objectMap.put(obj[0], PARSER.convertFromString(obj[2], Class.forName(obj[1])));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        // TODO: Parse eventData from bytes
        return new EventBox(parts[0], parts[1], parts[2], parts[3], objectMap);
    }

    /**
     * Converts to bytes
     *
     * @return array of bytes that represent an EventBox
     */
    public byte[] getBytes() {
        // TODO: Parse eventData to bytes
        StringBuilder data = new StringBuilder();
        data.append(String.join(SEPARATORS, new String[] {eventName, sourceId, targetId, from}))
            .append(END_LINE);
        for (Map.Entry<String, Object> entry : eventData.entrySet()) {
            data.append(entry.getKey())
                .append(SEPARATORS)
                .append(entry.getValue().getClass().getCanonicalName())
                .append(SEPARATORS)
                .append(PARSER.convertToString(entry.getValue(), (Class) entry.getValue().getClass()))
                .append(END_LINE);
        }

        return data.toString().getBytes();
    }

    public static <E, T> EventBox build(SceneNode source, Event<E, T> event, SceneNode target, Map<String,Object> data) {
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
