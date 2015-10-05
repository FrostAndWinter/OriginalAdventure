package swen.adventure.game;

import swen.adventure.engine.Event;
import swen.adventure.engine.KeyInput;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AdventureGameKeyInput extends KeyInput {

        public AdventureGameKeyInput() {
            super();
            keyMappings.put('w', this.eventMoveForwardKeyPressed);
            keyMappings.put('s', this.eventMoveBackwardKeyPressed);
            keyMappings.put('a', this.eventMoveLeftKeyPressed);
            keyMappings.put('d', this.eventMoveRightKeyPressed);
            keyMappings.put('q', this.eventMoveUpKeyPressed);
            keyMappings.put('e', this.eventMoveDownKeyPressed);
        }

        public final Event<KeyInput, KeyInput> eventMoveForwardKeyPressed = new Event<>("eventMoveForwardKeyPressed", this);
        public final Event<KeyInput, KeyInput> eventMoveBackwardKeyPressed = new Event<>("eventMoveBackwardKeyPressed", this);
        public final Event<KeyInput, KeyInput> eventMoveRightKeyPressed = new Event<>("eventMoveRightKeyPressed", this);
        public final Event<KeyInput, KeyInput> eventMoveLeftKeyPressed = new Event<>("eventMoveLeftKeyPressed", this);
    public final Event<KeyInput, KeyInput> eventMoveUpKeyPressed = new Event<>("eventMoveUpKeyPressed", this);
    public final Event<KeyInput, KeyInput> eventMoveDownKeyPressed = new Event<>("eventMoveDownKeyPressed", this);
}