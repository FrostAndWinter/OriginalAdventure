package swen.adventure.engine;

import swen.adventure.game.EventDataKeys;

import java.util.*;
import java.util.function.Function;

public class MouseInput implements Input {
    public enum Button {
        Left, Right
    }

    protected EnumMap<Button, Event<MouseInput, MouseInput>> onPressMappings = new EnumMap<>(Button.class);
    protected EnumMap<Button, Event<MouseInput, MouseInput>> onHeldMappings = new EnumMap<>(Button.class);
    protected EnumMap<Button, Event<MouseInput, MouseInput>> onReleasedMappings = new EnumMap<>(Button.class);

    public void pressButton(Button button) {
        Event<MouseInput, MouseInput> event = this.onPressMappings.get(button);
        if (event != null) {
            event.trigger(this, new HashMap<String, Object>() {{
                put(EventDataKeys.Event, event);
            }});
        }
    }

    public void checkHeldButtons(Function<Button, Boolean> isButtonPressedFunc, long elapsedTime) {
        onHeldMappings.entrySet()
                .stream()
                .filter(entry -> isButtonPressedFunc.apply(entry.getKey()))
                .map(Map.Entry::getValue)
                .forEach(event -> {
                    event.trigger(this, new HashMap<String, Object>() {{
                        put(EventDataKeys.Event, event);
                        put(EventDataKeys.ElapsedMillis, elapsedTime);
                    }});
                });
    }


    public void releaseButton(Button button) {
        Event<MouseInput, MouseInput> event = this.onReleasedMappings.get(button);
        if (event != null) {
            event.trigger(this, new HashMap<String, Object>() {{
                put(EventDataKeys.Event, event);
            }});
        }
    }
}
