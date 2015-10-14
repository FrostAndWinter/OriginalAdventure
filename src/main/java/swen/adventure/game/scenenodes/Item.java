package swen.adventure.game.scenenodes;

import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;
import swen.adventure.game.InteractionType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Joseph Bennett, 300319773
 * Daniel Braithwate, 300313770
 */
public class Item extends AdventureGameObject {

    public Optional<String> description;

    private Optional<Container> _containingContainer = Optional.empty();

    public final Event<Item, Player> eventPlayerPickedUpItem = new Event<>("PlayerPickedUpItem", this);
    public final Event<Item, Player> eventPlayerDroppedItem = new Event<>("PlayerDroppedItem", this);

    /**
     * An Item's parent transform must directly bring the item into
     * world space (including centering the mesh at the origin); any extra translations must be done
     * in a separate transform.
     * */
    public Item(String id, TransformNode parent, String name, String description) {
        super(id, parent, name);
        this.description = Optional.ofNullable(description);
    }

    /**
     * Move this item to the given container. Note that this will also remove it from
     * the previous container it was in.
     *
     * @param container the container to move this item to
     * @throws NullPointerException if the given container is null
     */
    public void moveToContainer(Container container) {
        if (container == null) {
            throw new NullPointerException("Cannot move to a null container");
        }

        if (container.push(this)) {
            _containingContainer.ifPresent(Container::pop);
            _containingContainer = Optional.of(container);
        }
    }

    @Override
    public void setMesh(MeshNode mesh) {
        super.setMesh(mesh);
        registerMeshForInteraction(mesh);
    }

    @Override
    public List<Interaction> possibleInteractions(final MeshNode meshNode, final Player player) {
        if (!player.inventory().isFull()) {
            return Collections.singletonList(new Interaction(InteractionType.PickUp, this, meshNode));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void performInteraction(final Interaction interaction, final MeshNode meshNode, final Player player) {
        switch (interaction.interactionType) {
            case PickUp:
                this.moveToContainer(player.inventory());
                this.eventPlayerPickedUpItem.trigger(player, Collections.emptyMap());
                break;
        }
    }

    /**
     * Returns the container that this item is in
     *
     * @return the conteainer that this item is in
     */
    public Optional<Container> containingContainer() {
        return _containingContainer;
    }

    /**
     * Sets the description for this item.
     *
     * @param description the description for this item.
     */
    public void setDescription(String description) {
        this.description = Optional.of(description);
    }
}
