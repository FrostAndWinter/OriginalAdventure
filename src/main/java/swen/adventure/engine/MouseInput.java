package swen.adventure.engine;

import java.util.*;

public class MouseInput {

    public static final String DataMouseButton = "DataMouseButton";

    public enum Button {
        Left, Right
    }

    private final static class MouseEvent {
        public MouseEvent(Event<MouseInput, MouseInput> mouseInputEvent, Button button) {
            this.mouseInputEvent = mouseInputEvent;
            this.button = button;
        }

        public final Event<MouseInput, MouseInput> mouseInputEvent;
        public final Button button;
    }

    private Queue<MouseEvent> mouseEventQueue = new LinkedList<>();

    public void pressButton(Button button) {
        MouseEvent mouseEvent = new MouseEvent(eventMouseButtonPressed, button);
        mouseEventQueue.add(mouseEvent);
    }

    public void releaseButton(Button button) {
        MouseEvent mouseEvent = new MouseEvent(eventMouseButtonReleased, button);
        mouseEventQueue.add(mouseEvent);
    }

    public void handleInput() {
        while (!mouseEventQueue.isEmpty()) {
            MouseEvent mouseEvent = mouseEventQueue.poll();
            Map<String, Object> data = new HashMap<>();
            data.put(DataMouseButton, mouseEvent.button);

            mouseEvent.mouseInputEvent.trigger(this, data);
        }
    }

    public final Event<MouseInput, MouseInput> eventMouseButtonPressed = new Event<>("eventMouseButtonPressed", this);
    public final Event<MouseInput, MouseInput> eventMouseButtonReleased = new Event<>("eventMouseButtonReleased", this);
}
