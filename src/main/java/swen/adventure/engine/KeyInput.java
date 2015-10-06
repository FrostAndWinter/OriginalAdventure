package swen.adventure.engine;

import swen.adventure.game.EventDataKeys;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 */
public abstract class KeyInput {
    protected Map<Character, Event<KeyInput, KeyInput>> keyMappings = new HashMap<>();
    private Map<Character, Boolean> keyPressedMap = new HashMap<>();

    public void pressKey(Character key) {
        keyPressedMap.put(Character.toLowerCase(key), true);
    }

    public void releaseKey(Character key) {
        keyPressedMap.put(Character.toLowerCase(key), false);
    }

    public void handleInput(long deltaMillis) {
        keyPressedMap.entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .forEach(entry -> {
                    Character key = entry.getKey();
                    Event<KeyInput, KeyInput> event = keyMappings.get(key);
                    if (event != null) {
                        event.trigger(this, new HashMap() {{
                            put(EventDataKeys.ElapsedMillis, deltaMillis);
                            put(EventDataKeys.Event, event);
                        }});
                    }

                });

    }
}
