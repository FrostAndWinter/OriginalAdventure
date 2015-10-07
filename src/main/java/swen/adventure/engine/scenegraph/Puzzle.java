package swen.adventure.engine.scenegraph;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.game.scenenodes.FlickeringLight;
import swen.adventure.game.scenenodes.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 6/10/15.
 *
 * A puzzle is a component for triggering actions based on the state of properties in other game objects.
 * The puzzle should be triggered to be checked by any event that may cause the solved/unsolved state of the puzzle to be changed
 * and if the state /is/ changed by that action, then it will broadcast an event that it is solved or unsolved.
 */
public final class Puzzle extends GameObject {

    /**
     * A puzzle condition is a simple boolean expression that will return true if the value returned by its getter
     * is equal to its required state.
     * @param <T> The type of the object to compare.
     */
    public static class PuzzleCondition<T> {
        public final Supplier<T> getter;
        public final T requiredState;
        public final String source;

        public PuzzleCondition(final Supplier<T> getter, final T requiredState, String source) {
            this.getter = getter;
            this.requiredState = requiredState;
            this.source = source;
        }

        public boolean isTrue() {
            return this.getter.get().equals(requiredState);
        }
    }

    private boolean _puzzleSolved;

    private List<PuzzleCondition> _conditions;

    public final Event<Puzzle, Puzzle> eventPuzzleSolved = new Event<>("eventPuzzleSolved", this);
    public final Event<Puzzle, Puzzle> eventPuzzleUnsolved = new Event<>("eventPuzzleUnsolved", this);

    public static final Action<SceneNode, Player, Puzzle> actionCheckPuzzle = (sceneNode, player, puzzle, data) -> {
        puzzle.checkForStateChange();
    };

    /**
     * Creates a new puzzle with the specified id, scene graph, and conditions.
     * @param id The id of the puzzle.
     * @param sceneGraph The scene graph to add the puzzle to.
     * @param conditions The conditions that must be met for the puzzle to be solved.
     */
    public Puzzle(final String id, final TransformNode sceneGraph, List<PuzzleCondition> conditions) {
        super(id, sceneGraph);

        _conditions = conditions;

        _puzzleSolved = this.isPuzzleSolved();
        this.triggerPuzzleStateEvent();
    }
    
    public String getConditionSource() {
        return String.join(";", _conditions.stream().map(cond -> cond.source).collect(Collectors.toList()));
    }

    /**
     * @return Whether all of this puzzle's conditions are met.
     */
    private boolean isPuzzleSolved() {
        boolean puzzleSolved = true;
        for (PuzzleCondition condition : _conditions) {
            if (!condition.isTrue()) {
                puzzleSolved = false;
            }
        }
        return puzzleSolved;
    }

    /**
     * Triggers the event that accompanies this player's state.
     */
    private void triggerPuzzleStateEvent() {
        if (_puzzleSolved) {
            this.eventPuzzleSolved.trigger(this, Collections.emptyMap());
        } else {
            this.eventPuzzleUnsolved.trigger(this, Collections.emptyMap());
        }
    }

    /**
     * Checks whether the puzzle has become solved or unsolved.
     */
    private void checkForStateChange() {
        boolean isPuzzleSolved = this.isPuzzleSolved();
        if (isPuzzleSolved != _puzzleSolved) {
            _puzzleSolved = isPuzzleSolved;
            this.triggerPuzzleStateEvent();
        }
    }
}
