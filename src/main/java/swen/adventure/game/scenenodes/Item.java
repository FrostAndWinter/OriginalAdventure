package swen.adventure.game.scenenodes;

import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by josephbennett on 6/10/15
 * Modified By Daniel Braithwate ID: 300313770
 */
public class Item extends AdventureGameObject {

    public final String name;
    public final Optional<String> description;

    private Optional<Container> _containingContainer = Optional.empty();

    /** An Item's parent transform must directly bring the item into world space (including centering the mesh at the origin); any extra translations must be done in a separate transform. */
    public Item(String id, TransformNode parent, String name, String description) {
        super(id, parent);
        this.name = name;
        this.description = Optional.ofNullable(description);
    }


    public void setContainingContainer(Container container) {
        _containingContainer.ifPresent(Container::pop);

        if (container != null) {
            container.push(this);
        }
    }

    @Override
    public List<Interaction> possibleInteractions(final MeshNode meshNode, final Player player) {
        if (!player.inventory().isFull()) {
            return Collections.singletonList(new Interaction(Interaction.InteractionType.PickUp, this, meshNode));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void performInteraction(final Interaction interaction, final MeshNode meshNode, final Player player) {
        switch (interaction.interactionType) {
            case PickUp:
                this.setContainingContainer(player.inventory());
                break;
        }
    }
}
