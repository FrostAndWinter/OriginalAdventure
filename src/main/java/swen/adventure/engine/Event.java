package swen.adventure.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */

/**
 * This Event class is based on the command pattern, and the way it works is basically as follows:
 ​
 On any object, you can add an event property. For instance, a door might have the property

 public final Event<Door> eventDoorOpened = new Event<Door>(this);
 ​
 Then, any other object that cares about the door opening can subscribe to that event. If, say, an alarm should go off when a particular door is opened, you'd write something like this
 ​
 Door door = (Door)sceneGraph.objectWithId("DoorToOutside");
 door.eventDoorOpened.addAction(alarm, (door, triggeringObject, alarm, otherData) -> {
     alarm.startRinging()
 });

 in the alarm class (or somewhere more appropriate).
 ​
 Then, whenever the door is opened, the alarm will be triggered. This results in code that is fairly decoupled – these interactions could be specified in the level format for known types – and easy to write.
 You can also build event chains. For example. The alarm.startRinging() call may result in an eventAlarmStartedRinging event being triggered – other objects could listen to that without having to care about
 what caused the alarm to start ringing.

 * @param <E> the type of object this swen.adventure.Event is paired to.
 */
public class Event<E> {

    private class ActionData<L> {
        public final L listener;
        public final Action<E, ?, L> action;

        public ActionData(L listener, Action<E, ?, L> action) {
            this.listener = listener;
            this.action = action;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ActionData<?> that = (ActionData<?>) o;

            return listener == that.listener && action == that.action;

        }

        @Override
        public int hashCode() {
            int result = listener.hashCode();
            result = 31 * result + action.hashCode();
            return result;
        }
    }

    /**
     * Special event that can be observed to see when any action is executed. Useful for networking code that needs to pass off any actions to the network.
     */
    public static final Event<Class<Event>> eventEventTriggered = new Event<>("EventTriggered", Event.class);

    private List<ActionData<?>> _actions = new ArrayList<>();
    private final E _eventObject;
    public final String name;

    public Event(String name, E eventObject) {
        this.name = name;
        _eventObject = eventObject;
    }

    public <L> void addAction(L listener, Action<E, ?, L> action) {
        _actions.add(new ActionData<>(listener, action));
    }

    public <L> void removeAction(L listener, Action<E, ?, L> action) {
        _actions.remove(new ActionData<>(listener, action));
    }

    /**
     *
     * @param triggeringObject The object that produced the event signal
     * @param data A dictionary of extraneous data that can be passed as an argument.
     */
    public <B> void trigger(final B triggeringObject, final Map<String, Object> data) {
        for (ActionData actionData : _actions) {
            actionData.action.execute(_eventObject, triggeringObject, actionData.listener, data);
        }
        if (triggeringObject != this) {
            Event.eventEventTriggered.trigger(this, Collections.emptyMap());
        }
    }
}
