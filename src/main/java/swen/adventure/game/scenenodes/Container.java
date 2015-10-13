package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A container stores Items inside it. It is a component that is attached to AdventureGameObjects.
 *
 * It behaves like a Pringle's carton (or a stack :p) - Items are pushed and popped off the top of the container.
 *
 * The Item at the top of the container can be
 *
 * @author Thomas Roughton, 300313924
 * @author Joseph Bennett, 300319773
 *
 * @see Item An item in the game which can be stored in a container.
 */
public class Container extends SceneNode {

    private static final int DefaultCapacity = 5;

    private final List<Item> _items = new ArrayList<>();
    private final int _capacity;

    private boolean _showTopItem = false;

    /**
     * Creates a new container with a default capacity of 5 items.
     *
     * @param id the id of this node in the scene graph
     * @param parent the parent transform of this node
     */
    public Container(final String id, final TransformNode parent) {
        super(id, parent, true);
        _capacity = DefaultCapacity;
    }

    /**
     * Creates a new container with the given capacity.
     *
     * @param capacity the capacity of this container
     * @param id the id of this node in the scene graph
     * @param parent the parent transform of this node
     */
    public Container(final String id, final TransformNode parent, final int capacity) {
        super(id, parent, true);
        _capacity = capacity;
    }

    /** If this container should display its top item, then that item will be made visible. */
    public void setShowTopItem(boolean showTopItem) {
        _showTopItem = showTopItem;
        this.setVisibilityOnContents();
    }

    private void setVisibilityOnContents() {
        for (Item item : _items) {
            item.setEnabled(false);
        }
        if (_showTopItem) {
            this.peek().ifPresent(item -> item.setEnabled(true));
        }
    }

    /**
     * Whether or not this container is has reached its capacity. i.e the container is full.
     *
     * @return true if full, false if not full.
     */
    public boolean isFull() {
        return _items.size() == _capacity;
    }

    /**
     * Push an item onto the top of this container. If the container is full then this method will return false and
     * the container will remained unchanged.
     *
     * @param item the item to push
     *
     * @return true if the item was successfully added, false if the container was full and the item could not be.
     */
    public boolean push(Item item) {
        if (_items.size() < _capacity) {
            _items.add(item);

            item.parent().get().setParent(this.parent().get());
            this.setVisibilityOnContents();

            return true;
        }

        return false;
    }

    /**
     * Returns the item at the top of this container but does not remove it. The item is returned as an optional which
     * will only be empty if there is nothing in the container.
     *
     * @return an optional either containing the item as the top of this container, or an empty optional.
     */
    public Optional<Item> peek() {
        return _items.isEmpty() ? Optional.empty() : Optional.of(_items.get(_items.size() - 1));
    }

    /**
     * Returns and removes the item at the top of this container. The item is returned as an optional which
     * will only be empty if there is nothing in the container.
     *
     * @return an optional either containing the item as the top of this container, or an empty optional.
     */
    public Optional<Item> pop() {
        if (_items.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(this.removeItemAtIndex(_items.size() - 1));
    }

    /**
     * Removes and returns the item at the given index in the container.
     *
     * @param index the index to remove the item from.
     * @return the item at the given index
     * @throws RuntimeException if the index is not between 0 and the current amount of items in the container
     */
    public Item removeItemAtIndex(int index) {
        if (index < 0 || index >= _items.size()) {
            throw new RuntimeException("Impossible index given for container. " + index  + " is greater than size.");
        }

        Item item = _items.remove(index);
        this.setVisibilityOnContents();
        return item;
    }

    /**
     * Returns the item at the given index in the container.
     *
     * @param index the index to get the item from.
     * @return the item at the given index
     * @throws RuntimeException if the index is not between 0 and the current amount of items in the container
     */
    public Optional<Item> itemAtIndex(int index) {
        if (index < 0 || index >= _capacity) {
            throw new RuntimeException("Impossible index given for container. " + index  + " is outside of capacity.");
        }

        if (index < _items.size()) {
            return Optional.of(_items.get(index));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Amount of items currently in this container.
     *
     * @return the amount of items currently in this container
     */
    public int itemCount() {
        return _items.size();
    }

    /**
     * The maximum amount of items that could be in this container. i.e its capacity
     *
     * @return the capacity of this container
     */
    public int capacity() {
        return _capacity;
    }

    /**
     * Whether or not to show the top item in this container. Showing the top item in the container will mean that
     * the top item is always set to enabled and every other item in this container will be disabled within
     * the scene graph.
     *
     * @return whether or not this container shows the top item.
     */
    public boolean getShowTopItem() {
        return _showTopItem;
    }
}
