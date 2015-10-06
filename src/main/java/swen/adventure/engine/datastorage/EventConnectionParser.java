package swen.adventure.engine.datastorage;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.SceneNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by josephbennett on 29/09/15
 * Modified by Thomas Roughton, Student ID 300313924.
 *
 * EventConnectionParser is a parser for our domain-specific language to set up events between different objects.
 * It can read a file in the format:
 *
 *  EventName; EventObject1ID, EventObject2ID, ..., EventObjectNID; ActionName; TargetObject1ID, TargetObject2ID, ..., TargetObjectNID
 *
 * It also provides methods to automatically build the connections between existing objects in a scene graph.
 */
public class EventConnectionParser {
    public static class EventConnection {
        public final String eventName;
        public final List<String> objectNames;
        public final String actionName;
        public final List<String> listenerNames;

        public EventConnection(String eventName, List<String> objectNames, String actionName, List<String> listenerNames) {
            this.eventName = eventName;
            this.objectNames = objectNames;
            this.actionName = actionName;
            this.listenerNames = listenerNames;
        }
    }

    public static List<EventConnection> parseFile(List<String> lines) {
        return lines.stream()
                .map(EventConnectionParser::parseLine)
                .collect(Collectors.toList());
    }

    public static EventConnection parseLine(String line) {
        line = line.replaceAll("\\s+", ""); // remove whitespace
        String[] components = line.split(";");

        String eventName = components[0];
        String actionName = components[2];
        List<String> objectNames = EventConnectionParser.parseList(components[1]);
        List<String> listenerNames = EventConnectionParser.parseList(components[3]);
        return new EventConnection(eventName, objectNames, actionName, listenerNames);
    }

    private static List<String> parseList(String list) {
        return Arrays.asList(list.split(","));
    }

    /**
     * Takes a Scene Graph and connects the actions and events within it according to connections.
     * @param connections A list of EventConnections describing the event-action connections between the objects.
     * @param sceneGraph The scene graph from which to retrieve the objects.
     */
    @SuppressWarnings("unchecked")
    public static void setupConnections(List<EventConnection> connections, SceneNode sceneGraph) {
        for (EventConnection connection : connections) {
            for (String objectName : connection.objectNames) {
                SceneNode targetObject = sceneGraph.nodeWithID(objectName).get();
                Event event = targetObject.eventWithName(connection.eventName);
                for (String listenerName : connection.listenerNames) {
                    Optional<SceneNode> listeningObject = sceneGraph.nodeWithID(listenerName);
                    if (!listeningObject.isPresent()) {
                        System.err.println("Error retrieving listening object with id" + listenerName);
                        break;
                    }
                    Action action = Action.actionWithName(connection.actionName, listeningObject.get());
                    event.addAction(listeningObject.get(), action);
                }
            }
        }
    }
}
