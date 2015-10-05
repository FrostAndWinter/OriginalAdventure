package swen.adventure.engine;

import java.util.Collections;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 4/10/15.
 */
public class ConditionalEvent {

    @FunctionalInterface
    public interface BooleanCondition {
        boolean isAsserted();
    }

    public final Event<ConditionalEvent, Event<?, ?>> eventAsserted = new Event<>("eventAsserted", this);
    public final Event<ConditionalEvent, Event<?, ?>> eventDeasserted = new Event<>("eventDeasserted", this);

    private BooleanCondition _condition;

    private final Action<?, Event<?, ?>, ConditionalEvent> actionEventTriggered = ((eventObject, triggeringObject, listener, data) -> {
       if (_condition.isAsserted()) {
           this.eventAsserted.trigger(triggeringObject, Collections.emptyMap());
       } else {
           this.eventDeasserted.trigger(triggeringObject, Collections.emptyMap());
       }
    });

    public ConditionalEvent(BooleanCondition condition, Event... eventsToListenTo) {
        _condition = condition;
        for (Event<?, ?> event : eventsToListenTo) {
            event.addAction(event, (Action)actionEventTriggered);
        }
    }
}
