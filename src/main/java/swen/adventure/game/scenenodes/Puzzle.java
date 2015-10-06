package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 6/10/15.
 */
public class Puzzle extends GameObject {

    private boolean _puzzleSolved;

    private Supplier<Boolean>[] _getters;
    private boolean[] _requiredStates;

    public final Event<Puzzle, Puzzle> eventPuzzleSolved = new Event<>("eventPuzzleSolved", this);
    public final Event<Puzzle, Puzzle> eventPuzzleUnsolved = new Event<>("eventPuzzleUnsolved", this);

    public static final Action<FlickeringLight, Player, Puzzle> actionLightToggled = (light, player, puzzle, data) -> {
        puzzle.checkForStateChange();
    };

    public Puzzle(final String id, final TransformNode parent, Supplier<Boolean>[] getters, boolean[] requiredStates) {
        super(id, parent);

        if (_getters.length != _requiredStates.length) {
            throw new RuntimeException("The lights and required states arrays must be the same length.");
        }

        _getters = getters;
        _requiredStates = requiredStates;

        _puzzleSolved = this.isPuzzleSolved();
        this.triggerPuzzleStateEvent();
    }

    private boolean isPuzzleSolved() {
        boolean puzzleSolved = true;
        for (int i = 0; i < _getters.length; i++) {
            if (_getters[i].get() != _requiredStates[i]) {
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
