package swen.adventure.engine;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 16/09/15.
 */
@FunctionalInterface
public interface Action<E, T, L> {
    void execute(E eventObject, T triggeringObject, L listener, Map<String, Object> data);

    @SuppressWarnings("unchecked")
    /**
     * Takes a name for an action (e.g. OpenDoor) and tries to find the action corresponding to the name.
     * The search pattern is thus: firstly, it looks in Actions for a field of the name action{name}.
     * Next, it looks for a field on the listeningObject of the name action{name}.
     * Finally, it looks for a concrete class implementing Action called {name}.
     * @param name A name in UpperCamelCase for the action (minus the action- prefix).
     * @return The action.
     * @throws RuntimeException if the action could not be found.
     */
    static <L> Action<?, ?, L> actionWithName(String name, L listeningObject) {
        String fieldName = "action" + name;

        try {
            Field field = Actions.class.getDeclaredField(fieldName);
            //field.setAccessible(true);
            return (Action<?, ?, L>) field.get(Actions.class);
        } catch (IllegalAccessException e) {
            System.err.println("Error accessing action with name " + name + ": " + e);
        } catch (NoSuchFieldException ignored) {
        }

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