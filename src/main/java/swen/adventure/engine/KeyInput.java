package swen.adventure.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 2/10/15.
 */
public abstract class KeyInput {
    protected Map<Character, Event<KeyInput>> keyMappings = new HashMap<>();
    private Map<Character, Boolean> keyPressedMap = new HashMap<>();

    public void pressKey(Character key) {
        keyPressedMap.put(Character.toLowerCase(key), true);
    }

    public void releaseKey(Character key) {
        keyPressedMap.put(Character.toLowerCase(key), false);
    }

    public void handleInput() {
        keyPressedMap.entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .map((entry) -> keyMappings.get(entry.getKey()))
                .filter(event -> event != null)
                .forEach(keyInputEvent -> keyInputEvent.trigger(this, Collections.emptyMap()));
    }
}
