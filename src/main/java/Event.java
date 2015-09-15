import swen.adventure.Actor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 15/09/15.
 */
public class Event<T> {
    public interface Command<T> {
        void execute(T object, Actor actor, Object data);
    }

    private List<Command<T>> _commands = new ArrayList<>();

    public Event() { }

    public void addCommand(Command<T> command) {
        _commands.add(command);
    }

    public void removeCommand(Command<T> command) {
        _commands.remove(command);
    }

    public void trigger(T object, Actor actor, Object data) {
        for (Command<T> command : _commands) {
            command.execute(object, actor, data);
        }
    }
}
