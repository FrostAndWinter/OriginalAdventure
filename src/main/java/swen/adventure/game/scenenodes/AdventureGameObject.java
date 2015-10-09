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

    public void setContainer(Container container) {
        _container = Optional.ofNullable(container);
    }
}
