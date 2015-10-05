package swen.adventure.engine;

import java.lang.ref.WeakReference;
import java.util.*;

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
 * @param <T> the type of object that will trigger this event.
 */
public class Event<E, T> {

    private static class ActionData<E, T, L> {
        public final WeakReference<L> listener;
        public final Action<E, T, L> action;

        public ActionData(L listener, Action<E, T, L> action) {
            this.listener = new WeakReference<>(listener);
            this.action = action;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ActionData<?, ?, ?> that = (ActionData<?, ?, ?>) o;

            return listener == that.listener && action == that.action;

        }

        @Override
        public int hashCode() {
            int result = listener.hashCode();
            result = 31 * result + action.hashCode();
            return result;
        }
    }

    public static class EventSet<E, T> {
        public final String eventName;
        private List<Event<E, T>> _events = new ArrayList<>();
        private List<ActionData<E, T, ?>> _actions = new ArrayList<>();

        public EventSet(String eventName) {
            this.eventName = eventName;
        }

        public <L> void addAction(L listener, Action<E, T, L> action) {
            for (Event<E, T> event  : _events) {
                event.addAction(listener, action);
            }
            _actions.add(new ActionData<>(listener, action));
        }

        public <L> void removeAction(L listener, Action<E, T, L> action) {
            for (Event<E, T> event  : _events) {
                event.removeAction(listener, action);
            }
            _actions.remove(new ActionData<>(listener, action));
        }

        private void addEvent(Event<E, T> event) {
            _events.add(event);
            for (ActionData action : _actions) {
                event.addAction(action.listener.get(), action.action);
            }
        }

    }

    private static Map<String, EventSet> _eventNamesToEvents = new HashMap<>();

    private List<ActionData<E, T, ?>> _actions = new ArrayList<>();
    private final E _eventObject;
    public final String name;


    public EventSet<?, ?> onAllObjects() {
        EventSet<?, ?> events = _eventNamesToEvents.get(this.name);
        if (events == null) {
            events = new EventSet<>(this.name);
            _eventNamesToEvents.put(this.name, events);
        }
        return events;
    }

    public static EventSet<?, ?> eventSetForName(String name) {
        EventSet<?, ?> events = _eventNamesToEvents.get(name);
        if (events == null) {
            events = new EventSet<>(name);
            _eventNamesToEvents.put(name, events);
        }
        return events;
    }

    private static <E, T> void addEventForName(Event<E, T> event, String name) {
        EventSet<E, T> events = _eventNamesToEvents.get(name);
        if (events == null) {
            events = new EventSet<>(name);
            _eventNamesToEvents.put(name, events);
        }
        events.addEvent(event);
    }

    public Event(String name, E eventObject) {
        this.name = name;
        _eventObject = eventObject;

        Event.addEventForName(this, name);
    }

    public <L> void addAction(L listener, Action<E, T, L> action) {
        _actions.add(new ActionData<>(listener, action));
    }

    public <L> void removeAction(L listener, Action<E, T, L> action) {
        _actions.remove(new ActionData<>(listener, action));
    }

    /**
     *
     * @param triggeringObject The object that produced the event signal
     * @param data A dictionary of extraneous data that can be passed as an argument.
     */
    public void trigger(final T triggeringObject, final Map<String, Object> data) {
        for (ActionData actionData : _actions) {
            actionData.action.execute(_eventObject, triggeringObject, actionData.listener.get(), data);
        }
    }
}
