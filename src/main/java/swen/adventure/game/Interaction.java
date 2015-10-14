package swen.adventure.game;

import swen.adventure.engine.Event;
import swen.adventure.engine.network.Client;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.game.scenenodes.AdventureGameObject;
import swen.adventure.game.scenenodes.Player;

import java.util.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 10/10/15.
 */
public final class Interaction {

    public enum ActionType {
        Primary,
        Secondary;
    }

    public final InteractionType interactionType;
    public final AdventureGameObject gameObject;
    public final MeshNode meshNode;

    public Interaction(final InteractionType interactionType, final AdventureGameObject gameObject, final MeshNode meshNode) {
        this.interactionType = interactionType;
        this.gameObject = gameObject;
        this.meshNode = meshNode;
    }

    /**
     * Perform interaction with player
     *
     * @param player player that will fire the interaction
     */
    public void performInteractionWithPlayer(Player player) {
        this.gameObject.performInteraction(this, this.meshNode, player);
    }

    /**
     * End an interaction
     *
     * @param player player that stopped the interaction
     */
    public void interactionEndedByPlayer(Player player) {
        this.gameObject.eventInteractionEnded.trigger(player, Collections.singletonMap(EventDataKeys.Interaction, this));
    }

    /**
     * Get a string that describes the interaction
     *
     * @param player player that will start the interaction
     * @param buttonName button name that will be used to start the interaction
     * @return A formatted string that describes the interaction
     */
    public String interactionMessage(Player player, Character buttonName) {
        switch (this.interactionType) {
            case PickUp:
                return String.format("Press %c to pick up %s", buttonName, this.gameObject.name);
            case PlaceIn:
                final String[] message = {null};
                player.inventory().selectedItem().ifPresent(item -> {
                    message[0] = String.format("Press %s to place %s in %s", buttonName, item.name, this.gameObject.name);
                });
                return message[0];
            case Open:
                return String.format("Press %c to open %s", buttonName, this.gameObject.name);
            case Close:
                return String.format("Press %c to close %s", buttonName, this.gameObject.name);
            case Pull:
                return String.format("Press %c to pull %s", buttonName, this.gameObject.name);
            case Give:
                final String[] responseArr = new String[1]; //since variables in lambdas should be final.
                player.inventory().selectedItem().ifPresent(item -> {
                    responseArr[0] = String.format("Press %c to give %s to %s", buttonName, item.name, this.gameObject.name);
                });
                String response = responseArr[0];
                return response != null ? response : "";
        }
        throw new RuntimeException("Interaction type not implemented for message: " + interactionType); //Should never happen if switch statement is exhaustive.
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