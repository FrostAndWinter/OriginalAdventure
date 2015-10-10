package swen.adventure.game;

import swen.adventure.game.scenenodes.AdventureGameObject;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 10/10/15.
 */
public final class Interaction {
    public enum InteractionType {
        PickUp,
        PlaceIn,
        Open,
        Close,
        Toggle;
    }

    public final InteractionType interactionType;
    public final AdventureGameObject gameObject;

    public Interaction(final InteractionType interactionType, final AdventureGameObject gameObject) {
        this.interactionType = interactionType;
        this.gameObject = gameObject;
    }
}