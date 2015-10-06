package swen.adventure.game;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.rendering.maths.Vector3;

import java.util.HashMap;

public class AdventureGameKeyInput extends KeyInput {

        public AdventureGameKeyInput() {
            super();
            keyMappings.put('w', this.eventMoveForwardKeyPressed);
            keyMappings.put('s', this.eventMoveBackwardKeyPressed);
            keyMappings.put('a', this.eventMoveLeftKeyPressed);
            keyMappings.put('d', this.eventMoveRightKeyPressed);

            this.eventMoveBackwardKeyPressed.addAction(this, actionMoveKeyPressed);
            this.eventMoveForwardKeyPressed.addAction(this, actionMoveKeyPressed);
            this.eventMoveLeftKeyPressed.addAction(this, actionMoveKeyPressed);
            this.eventMoveRightKeyPressed.addAction(this, actionMoveKeyPressed);

        }

        private final Event<KeyInput, KeyInput> eventMoveForwardKeyPressed = new Event<>("eventMoveForwardKeyPressed", this);
    private final Event<KeyInput, KeyInput> eventMoveBackwardKeyPressed = new Event<>("eventMoveBackwardKeyPressed", this);
    private final Event<KeyInput, KeyInput> eventMoveRightKeyPressed = new Event<>("eventMoveRightKeyPressed", this);
    private final Event<KeyInput, KeyInput> eventMoveLeftKeyPressed = new Event<>("eventMoveLeftKeyPressed", this);

    public final Event<KeyInput, KeyInput> eventMoveInDirection = new Event<>("eventMoveInDirection", this);

    public final Action<KeyInput, KeyInput, AdventureGameKeyInput> actionMoveKeyPressed = ((eventObject, triggeringObject, listener, data) -> {
       long elapsedTime = (Long)data.get(EventDataKeys.ElapsedMillis);
        Vector3 direction = null;
        Event<KeyInput, KeyInput> event = (Event<KeyInput, KeyInput>)data.get(EventDataKeys.Event);

        if (event == this.eventMoveForwardKeyPressed) {
            direction = new Vector3(0, 0, -1);
        } else if (event == this.eventMoveBackwardKeyPressed) {
            direction = new Vector3(0, 0, 1);
        } else if (event == this.eventMoveLeftKeyPressed) {
            direction = new Vector3(-1, 0, 0);
        } else if (event == this.eventMoveRightKeyPressed) {
            direction = new Vector3(1, 0, 0);
        }
        if (direction != null) {
            final Vector3 finalDirection = direction;
            this.eventMoveInDirection.trigger(this, new HashMap(){{
                put(EventDataKeys.Direction, finalDirection);
                put(EventDataKeys.ElapsedMillis, elapsedTime);
            }});
        }
    });
}