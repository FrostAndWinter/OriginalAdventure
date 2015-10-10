package swen.adventure.game;

import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.game.scenenodes.AdventureGameObject;
import swen.adventure.game.scenenodes.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    public void interactionEndedByPlayer(Player player) {
        this.gameObject.eventInteractionEnded.trigger(player, Collections.singletonMap(EventDataKeys.Interaction, this));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Interaction that = (Interaction) o;
        return Objects.equals(interactionType, that.interactionType) &&
                Objects.equals(gameObject, that.gameObject) &&
                Objects.equals(meshNode, that.meshNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interactionType, gameObject, meshNode);
    }

    @Override
    public String toString() {
        return "Interaction{" +
                "interactionType=" + interactionType +
                ", gameObject=" + gameObject +
                ", meshNode=" + meshNode +
                '}';
    }
}