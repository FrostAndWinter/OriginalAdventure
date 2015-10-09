package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 9/10/15.
 *
 * An AdventureGameObject is a specialisation of GameObject allowing for game-specific components.
 */
public class AdventureGameObject extends GameObject {

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
}
