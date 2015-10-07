package swen.adventure.engine.scenegraph;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.game.scenenodes.FlickeringLight;
import swen.adventure.game.scenenodes.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 6/10/15.
 */
public final class Puzzle extends GameObject {

    public static class PuzzleCondition<T> {
        public final Supplier<T> getter;
        public final T requiredState;

        public PuzzleCondition(final Supplier<T> getter, final T requiredState) {
            this.getter = getter;
            this.requiredState = requiredState;
        }

        public boolean isTrue() {
            return this.getter.get().equals(requiredState);
        }
    }

    private boolean _puzzleSolved;

    private List<PuzzleCondition> _conditions;

    public final Event<Puzzle, Puzzle> eventPuzzleSolved = new Event<>("eventPuzzleSolved", this);
    public final Event<Puzzle, Puzzle> eventPuzzleUnsolved = new Event<>("eventPuzzleUnsolved", this);

    public static final Action<FlickeringLight, Player, Puzzle> actionCheckPuzzle = (light, player, puzzle, data) -> {
        puzzle.checkForStateChange();
    };

    public Puzzle(final String id, final TransformNode parent, List<PuzzleCondition> conditions) {
        super(id, parent);

        _conditions = conditions;

        _puzzleSolved = this.isPuzzleSolved();
        this.triggerPuzzleStateEvent();
    }

    private boolean isPuzzleSolved() {
        boolean puzzleSolved = true;
        for (PuzzleCondition condition : _conditions) {
            if (!condition.isTrue()) {
                puzzleSolved = false;
            }
        }
        return puzzleSolved;
    }

    private void triggerPuzzleStateEvent() {
        if (_puzzleSolved) {
            this.eventPuzzleSolved.trigger(this, Collections.emptyMap());
        } else {
            this.eventPuzzleUnsolved.trigger(this, Collections.emptyMap());
        }
    }

    private void checkForStateChange() {
        boolean isPuzzleSolved = this.isPuzzleSolved();
        if (isPuzzleSolved != _puzzleSolved) {
            _puzzleSolved = isPuzzleSolved;
            this.triggerPuzzleStateEvent();
        }
    }
}
