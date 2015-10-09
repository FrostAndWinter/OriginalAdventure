package swen.adventure.game.scenenodes;

import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 9/10/15.
 *
 * An AdventureGameObject is a specialisation of GameObject allowing for game-specific components.
 */
public class AdventureGameObject extends GameObject {

    public final Event<AdventureGameObject, Player> eventGameObjectLookedAt = new Event<>("eventGameObjectLookedAt", this);
    public final Event<AdventureGameObject, Player> eventGameObjectLookedAwayFrom = new Event<>("eventGameObjectLookedAwayFrom", this);

    private Optional<Container> _container = Optional.empty();

    public AdventureGameObject(final String id, final TransformNode parent) {
        super(id, parent);
    }

    /**
     * @return The container associated with this object, if it exists.
     *         This is used to either link a game object as being a container, or, in the case of an item, specify the container the item is in.
     */
    public Optional<Container> container() {
        return _container;
    }

    @Override
    protected void addMesh(MeshNode mesh) {
        super.addMesh(mesh);

        mesh.eventMeshLookedAt.addAction(this, (eventObject, player, listener, data) -> {
            listener.eventGameObjectLookedAt.trigger(player, Collections.emptyMap());
        });

        mesh.eventMeshLookedAwayFrom.addAction(this, (eventObject, player, listener, data) -> {
            listener.eventGameObjectLookedAwayFrom.trigger(player, Collections.emptyMap());
        });
    }

    /**
     * Sets this object's container to be the specified container.
     * In the case of GameObjects that are represent container meshes, this should be overridden to
     * set up the connections required for the container to be modified when the object is interacted with.
     * In the case of items, this should call pushItem on the container with the item as an argument.
     * @param container
     */
    public void setContainer(Container container) {
        _container = Optional.ofNullable(container);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
