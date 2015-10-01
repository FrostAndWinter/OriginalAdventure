package swen.adventure.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KeyInput {
        private Map<Character, Boolean> keyPressedMap = new HashMap<>();
        private Map<Character, Event<KeyInput>> keyMappings = new HashMap<>();

        public KeyInput() {
            keyMappings.put('w', this.eventMoveForwardKeyPressed);
            keyMappings.put('s', this.eventMoveBackwardKeyPressed);
            keyMappings.put('a', this.eventMoveLeftKeyPressed);
            keyMappings.put('d', this.eventMoveRightKeyPressed);
        }

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

        public final Event<KeyInput> eventMoveForwardKeyPressed = new Event<>("eventMoveForwardKeyPressed", this);
        public final Event<KeyInput> eventMoveBackwardKeyPressed = new Event<>("eventMoveBackwardKeyPressed", this);
        public final Event<KeyInput> eventMoveRightKeyPressed = new Event<>("eventMoveRightKeyPressed", this);
        public final Event<KeyInput> eventMoveLeftKeyPressed = new Event<>("eventMoveLeftKeyPressed", this);
}