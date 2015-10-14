/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 package swen.adventure.engine;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
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
 * @param <T> the type of object that will trigger this event.
 */
public class Event<E, T> {

    private static boolean ShowEventDebugLog = false;

    /**
     * ActionData is used to internally store records of registered actions.
     * @param <E> The type of object that the event is attached to.
     * @param <T> The type of object that may trigger the event.
     * @param <L> The type of object that is listening to the event.
     */
    private static class ActionData<E, T, L> {
        public final WeakReference<L> listener;
        public final Action<? super E, ? super T, L> action;

        public ActionData(L listener, Action<? super E, ? super T, L> action) {
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

    /**
     * An EventSet provides access to all events by a given name.
     * It can be used when you want to listen to an event, but you don't know which specific objects the event will be on.
     * @param <E> The type of objects the events are attached to.
     * @param <T> The type of object that may trigger the event.
     */
    public static class EventSet<E, T> {
        public final String eventName;
        private List<Event<E, T>> _events = new ArrayList<>();
        private List<ActionData<E, T, ?>> _actions = new ArrayList<>();

        public EventSet(String eventName) {
            this.eventName = eventName;
        }

        /**
         * Adds an action to be performed when the events collected by this event set trigger.
         * @param listener The object that is listening to the action.
         * @param action The action to perform.
         * @param <L> The type of object that is listening to the action.
         */
        public <L> void addAction(L listener, Action<? super E, ? super T, L> action) {
            for (Event<E, T> event  : _events) {
                event.addAction(listener, action);
            }
            _actions.add(new ActionData<>(listener, action));
        }

        /**
         * Removes an action from being performed when the events collected by this event set trigger.
         * @param listener The object that is listening to the action.
         * @param action The action to perform.
         * @param <L> The type of object that is listening to the action.
         */
        public <L> void removeAction(L listener, Action<? super E, ? super T, L> action) {
            for (Event<E, T> event  : _events) {
                event.removeAction(listener, action);
            }
            _actions.remove(new ActionData<>(listener, action));
        }

        /**
         * Adds an event to this object's internal list of events.
         * @param event the event to add.
         */
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

    /**
     * Given an event attached to an object, this will return the event set that encompassing this event occuring on any object.
     * @return The event set of this event occurring on any object.
     */
    public EventSet<E, T> onAllObjects() {
        EventSet<?, ?> events = _eventNamesToEvents.get(this.name);
        if (events == null) {
            events = new EventSet<>(this.name);
            _eventNamesToEvents.put(this.name, events);
        }
        return (EventSet<E, T>)events;
    }

    /**
     * Finds and returns the event set for events with a given name.
     * @param name The name of the events in the event set.
     * @return The event set for that name.
     */
    public static EventSet<?, ?> eventSetForName(String name) {
        EventSet<?, ?> events = _eventNamesToEvents.get(name);
        if (events == null) {
            events = new EventSet<>(name);
            _eventNamesToEvents.put(name, events);
        }
        return events;
    }

    /**
     * Adds an event with a given name to the event set for that name.
     */
    private static <E, T> void addEventForName(Event<E, T> event, String name) {
        EventSet<E, T> events = _eventNamesToEvents.get(name);
        if (events == null) {
            events = new EventSet<>(name);
            _eventNamesToEvents.put(name, events);
        }
        events.addEvent(event);
    }

    /**
     * Constructs a new event with the given name on the given object.
     * @param name The name of the event.
     * @param eventObject The object that the event is attached to.
     */
    public Event(String name, E eventObject) {
        this.name = name;
        _eventObject = eventObject;

        Event.addEventForName(this, name);
    }

    /**
     * Adds an action to be performed when this event triggers.
     * @param listener The object that is listening to the action.
     * @param action The action to perform.
     * @param <L> The type of object that is listening to the action.
     */
    public <L> void addAction(L listener, Action<? super E, ? super T, L> action) {
        _actions.add(new ActionData<>(listener, action));
    }

    /**
     * Removes an action from being performed when this event triggers.
     * @param listener The object that is listening to the action.
     * @param action The action to perform.
     * @param <L> The type of object that is listening to the action.
     */
    public <L> void removeAction(L listener, Action<? super E, ? super T, L> action) {
        _actions.remove(new ActionData<>(listener, action));
    }

    /**
     * Trigger this event.
     * @param triggeringObject The object that produced the event signal
     * @param data A dictionary of extraneous data that can be passed as an argument.
     */
    public <U extends T> void trigger(final U triggeringObject, final Map<String, Object> data) {
        for (ActionData actionData : _actions) {

            // if the listener is the trigger, then it doesn't need to know about the update it sent out
            if (actionData.listener == triggeringObject) {
                System.out.println("Skipping action - listener is the trigger and probably already knows");
                continue;
            }

            if (actionData.listener.get() == null) { continue; } //skip if the listener object has expired.
            actionData.action.execute(_eventObject, triggeringObject, actionData.listener.get(), data);
        }

        if (ShowEventDebugLog) {
            String log = name;

            if (name.equals("ValueChanged")) return;
            if (name.equals("MeshLookedAt")) return;
            if (name.equals("PlayerMoved")) return;


            log += " triggered by " + triggeringObject;
            log += " on " + _eventObject;

            if (!data.isEmpty()) {
                log += " {";

                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    log += entry.getKey() + ":" + entry.getValue() + ", ";
                }

                log += "}";
            }
            System.out.println(log);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}