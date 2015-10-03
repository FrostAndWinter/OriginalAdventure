package swen.adventure.engine;

import org.lwjgl.Sys;
import processing.core.PApplet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by danielbraithwt on 10/3/15.
 */
public class Signal {
    public final Event<Signal> eventSignalTrue = new Event<>("eventSignalTrue", this);
    public final Event<Signal> eventSignalFalse = new Event<>("eventSignalFalse", this);

    private boolean[] _firedEvents;

    public Signal(boolean[] state, Event[] events) {
        if (state.length != events.length) {
            throw new IllegalArgumentException("Must be same number of states as events");
        }

        _firedEvents = new boolean[events.length];

        for (int i = 0; i < events.length; i++) {
            _firedEvents[i] = !state[i];

            final int currentId = i;

            events[i].addAction(this, (Action) (eventObject, triggeringObject, listener, data) -> recivedEvent(currentId));
        }
    }

    private void recivedEvent(int id) {
        _firedEvents[id] = !_firedEvents[id];

        for (boolean b : _firedEvents) {
            if(!b) {
                eventSignalFalse.trigger(this, Collections.EMPTY_MAP);
                return;
            }
        }

        eventSignalTrue.trigger(this, Collections.EMPTY_MAP);
    }
}
