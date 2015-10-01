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
        }

        public final Event<KeyInput> eventMoveForwardKeyPressed = new Event<>("eventMoveForwardKeyPressed", this);
        public final Event<KeyInput> eventMoveBackwardKeyPressed = new Event<>("eventMoveBackwardKeyPressed", this);
        public final Event<KeyInput> eventMoveRightKeyPressed = new Event<>("eventMoveRightKeyPressed", this);
        public final Event<KeyInput> eventMoveLeftKeyPressed = new Event<>("eventMoveLeftKeyPressed", this);
}