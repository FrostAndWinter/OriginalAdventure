package swen.adventure.game.input;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.game.EventDataKeys;

import java.util.HashMap;
import java.util.Map;

public class AdventureGameKeyInput extends KeyInput {

        public AdventureGameKeyInput() {
            super();
            this.onHeldMappings.put('W', this.eventMoveForwardKeyPressed);
            this.onHeldMappings.put('S', this.eventMoveBackwardKeyPressed);
            this.onHeldMappings.put('A', this.eventMoveLeftKeyPressed);
            this.onHeldMappings.put('D', this.eventMoveRightKeyPressed);
            this.onPressMappings.put('I', this.eventHideShowInventory);
            this.onReleasedMappings.put('I', this.eventHideShowInventory);

            this.onPressMappings.put('C', this.eventHideShowControls);
            this.onReleasedMappings.put('C', this.eventHideShowControls);

            this.onPressMappings.put('E', this.eventPrimaryAction);
            this.onPressMappings.put('Q', this.eventSecondaryAction);
            this.onReleasedMappings.put('E', this.eventPrimaryActionEnded);
            this.onReleasedMappings.put('Q', this.eventSecondaryActionEnded);

            this.eventMoveBackwardKeyPressed.addAction(this, actionMoveKeyPressed);
            this.eventMoveForwardKeyPressed.addAction(this, actionMoveKeyPressed);
            this.eventMoveLeftKeyPressed.addAction(this, actionMoveKeyPressed);
            this.eventMoveRightKeyPressed.addAction(this, actionMoveKeyPressed);

        }

    public final Event<KeyInput, KeyInput> eventPrimaryAction = new Event<>("eventPrimaryAction", this);
    public final Event<KeyInput, KeyInput> eventSecondaryAction = new Event<>("eventSecondaryAction", this);
    public final Event<KeyInput, KeyInput> eventPrimaryActionEnded = new Event<>("eventPrimaryActionEnded", this);
    public final Event<KeyInput, KeyInput> eventSecondaryActionEnded = new Event<>("eventSecondaryActionEnded", this);

    public final Event<KeyInput, KeyInput> eventHideShowInventory = new Event<>("eventHideShowInventory", this);

    public final Event<KeyInput, KeyInput> eventHideShowControls = new Event<>("eventHideShowControls", this);

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
            this.eventMoveInDirection.trigger(this, new HashMap<String, Object>(){{
                put(EventDataKeys.Direction, finalDirection);
                put(EventDataKeys.ElapsedMillis, elapsedTime);
            }});
        }
    });

    /**
     * Given a particular event, finds the character to press to trigger that event by searching through the key mappings.
     * @param event The event to look for.
     * @return The character to press in order to trigger that event.
     */
    public final Character characterForEvent(Event<KeyInput, KeyInput> event) {

        for (Map.Entry<Character, Event<KeyInput, KeyInput>> entry : this.onPressMappings.entrySet()) {
            if (entry.getValue() == event) {
                return entry.getKey();
            }
        }

        for (Map.Entry<Character, Event<KeyInput, KeyInput>> entry : this.onHeldMappings.entrySet()) {
            if (entry.getValue() == event) {
                return entry.getKey();
            }
        }

        for (Map.Entry<Character, Event<KeyInput, KeyInput>> entry : this.onReleasedMappings.entrySet()) {
            if (entry.getValue() == event) {
                return entry.getKey();
            }
        }

        return null;
    }
}