package swen.adventure.engine;

import swen.adventure.game.EventDataKeys;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 * Modified by Joseph Bennett.
 */
public abstract class KeyInput implements Input {
    protected Map<Character, Event<KeyInput, KeyInput>> onPressMappings = new HashMap<>();
    protected Map<Character, Event<KeyInput, KeyInput>> onHeldMappings = new HashMap<>();
    protected Map<Character, Event<KeyInput, KeyInput>> onReleasedMappings = new HashMap<>();

    public void pressKey(Character key) {
        Event<KeyInput, KeyInput> event = this.onPressMappings.get(key);
        if (event != null) {
            event.trigger(this, new HashMap<String, Object>() {{
                put(EventDataKeys.Event, event);
            }});
        }
    }

    public void checkHeldKeys(Function<Character, Boolean> isKeyPressedFunc, long elapsedTime) {
        onHeldMappings.entrySet()
                .stream()
                .filter(entry -> isKeyPressedFunc.apply(entry.getKey()))
                .map(Map.Entry::getValue)
                .forEach(event -> {
                    event.trigger(this, new HashMap<String, Object>() {{
                        put(EventDataKeys.Event, event);
                        put(EventDataKeys.ElapsedMillis, elapsedTime);
                    }});
                });
    }


    public void releaseKey(Character key) {
        Event<KeyInput, KeyInput> event = this.onReleasedMappings.get(key);
        if (event != null) {
            event.trigger(this, new HashMap<String, Object>() {{
                put(EventDataKeys.Event, event);
            }});
        }
    }
}
