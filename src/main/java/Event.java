import swen.adventure.Actor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */

/**
 * This Event class is based on the command pattern, and the way it works is basically as follows:
 ​
 On any object, you can add an event property. For instance, a door might have the property

 public final Event eventDoorOpened;
 ​
 Then, any other object that cares about the door opening can subscribe to that event. If, say, an alarm should go off when a particular door is opened, you'd write something like this
 ​
 Door door = (Door)sceneGraph.objectWithId("DoorToOutside");
 door.eventDoorOpened.addAction( (door, actor, otherData) -> {
 this.triggerAlarm();
 });

 in the alarm class (or somewhere more appropriate).
 ​
 Then, whenever the door is opened, the alarm will be triggered. This results in code that is fairly decoupled – these interactions could be specified in the level format for known types – and easy to write.

 * @param <T> the type of object this Event is paired to.
 */
public class Event<T> {
    public interface Action<T> {
        void execute(T object, Actor actor, List<String> data);
    }

    private List<Action<T>> _actions = new ArrayList<>();

    public Event() { }

    public void addAction(Action<T> action) {
        _actions.add(action);
    }

    public void removeAction(Action<T> action) {
        _actions.remove(action);
    }

    /**
     *
     * @param object The object that the event was triggered on/applies to.
     * @param actor The actor who produced the event signal
     * @param data A list of extraneous data that can be passed as an argument.
     */
    public void trigger(T object, Actor actor, List<String> data) {
        for (Action<T> action : _actions) {
            action.execute(object, actor, data);
        }
    }
}
