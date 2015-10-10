package swen.adventure.game;

import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.game.scenenodes.AdventureGameObject;
import swen.adventure.game.scenenodes.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 10/10/15.
 */
public final class Interaction {

    public enum ActionType {
        Primary,
        Secondary;
    }

    public enum InteractionType {
        PickUp,
        PlaceIn,
        Open,
        Close,
        Pull;


        public static List<InteractionType> typesForActionType(ActionType actionType) {
            switch (actionType) {
                case Primary:
                    return Arrays.asList(PickUp, Open, Close, Pull);
                case Secondary:
                    return Collections.singletonList(PlaceIn);
                default:
                    return Collections.emptyList();
            }
        }
    }

    public final InteractionType interactionType;
    public final AdventureGameObject gameObject;
    public final MeshNode meshNode;

    public Interaction(final InteractionType interactionType, final AdventureGameObject gameObject, final MeshNode meshNode) {
        this.interactionType = interactionType;
        this.gameObject = gameObject;
        this.meshNode = meshNode;
    }

    public void performInteractionWithPlayer(Player player) {
        this.gameObject.performInteraction(this, this.meshNode, player);
    }
}