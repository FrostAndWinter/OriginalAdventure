package swen.adventure;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 16/09/15.
 */
public interface Action<E, T, L> {
    void execute(E eventObject, T triggeringObject, L listener, Map<String, Object> data);

    /**
     * Takes a name for an action (e.g. OpenDoor) and tries to find the action corresponding to the name.
     * The search pattern is thus: firstly, it looks in Actions for a field of the name action{name}.
     * Next, it looks for a field on the listeningObject of the name action{name}.
     * Finally, it looks for a concrete class implementing Action called {name}.
     * @param name A name in UpperCamelCase for the action (minus the action- prefix).
     * @return The action.
     * @throws RuntimeException if the action could not be found.
     */
    public static <L> Action<?, ?, L> actionWithName(String name, L listeningObject) {
        String fieldName = "action" + name;

        try {
            Field field = Actions.class.getDeclaredField(name);
            //field.setAccessible(true);
            Action<?, ?, L> action = (Action<?, ?, L>) field.get(Actions.class);
            return action;
        } catch (IllegalAccessException e) {
            System.err.println("Error accessing action with name " + name + ": " + e);
        } catch (NoSuchFieldException e) {
        }

        try {
            Field field = listeningObject.getClass().getDeclaredField(name);
            Action<?, ?, L> action = (Action<?, ?, L>) field.get(listeningObject);
            return action;
        } catch (IllegalAccessException e) {
            System.err.println("Error accessing action with name " + name + ": " + e);
        } catch (NoSuchFieldException e) {
        }

        try {
            Class<? extends Action<?, ?, L>> actionClass = (Class<? extends Action<?, ?, L>>) Class.forName(name);
            Action<?, ?, L> action = actionClass.newInstance();
            return action;
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("Error instantiating Action class for name " + name + ": ");
        } catch (ClassNotFoundException e) {
        }

        throw new RuntimeException("Could not find an action with name " + name + " on object " + listeningObject);
    }
}