/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 16/09/15.
 *
 * An action is a lambda that can be triggered in response to an Event.
 */
@FunctionalInterface
public interface Action<E, T, L> {
    /**
     * Executes a lambda with the specified parameters.
     * @param eventObject The object on which the event was triggered.
     * @param triggeringObject The object that triggered the event.
     * @param listener The object that was listening to the event (since Actions may be declared statically, this functions as the 'this' parameter).
     * @param data A data dictionary that contains extra information specified by the object that triggered the event.
     */
    void execute(E eventObject, T triggeringObject, L listener, Map<String, Object> data);

    @SuppressWarnings("unchecked")
    /**
     * Takes a name for an action (e.g. OpenDoor) and tries to find the action corresponding to the name.
     * Firstly, it looks for a field on the listeningObject of the name action{name}.
     * Then, it looks for a concrete class implementing Action called {name}.
     * @param name A name in UpperCamelCase for the action (minus the action- prefix).
     * @return The action.
     * @throws RuntimeException if the action could not be found.
     */
    static <L> Action<?, ?, L> actionWithName(String name, L listeningObject) {
        String fieldName = "action" + name;

        try {
            Field field = listeningObject.getClass().getField(fieldName);
            return (Action<?, ?, L>) field.get(listeningObject);
        } catch (IllegalAccessException e) {
            System.err.println("Error accessing action with name " + name + ": " + e);
        } catch (NoSuchFieldException ignored) {

        }

        try {
            Class<? extends Action<?, ?, L>> actionClass = (Class<? extends Action<?, ?, L>>) Class.forName(name);
            return actionClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("Error instantiating Action class for name " + name + ": ");
        } catch (ClassNotFoundException ignored) {
        }

        throw new RuntimeException("Could not find an action with name " + name + " on object " + listeningObject);
    }
}