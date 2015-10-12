package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 8/10/15.
 */
public class Container extends SceneNode {

    private static final int DefaultCapacity = 5;

    private final List<Item> _items = new ArrayList<>();
    private final int _capacity;

    private boolean _showTopItem = false;

    public Container(final String id, final TransformNode parent) {
        super(id, parent, true);
        _capacity = DefaultCapacity;
    }

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

    public boolean isFull() {
        return _items.size() == _capacity;
    }

    public boolean push(Item item) {
        if (_items.size() < _capacity) {
            _items.add(item);

            item.parent().get().setParent(this.parent().get());
            this.setVisibilityOnContents();

            return true;
        }

        return false;
    }

    public Optional<Item> peek() {
        return _items.isEmpty() ? Optional.empty() : Optional.of(_items.get(_items.size() - 1));
    }

    public Optional<Item> pop() {
        if (_items.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(this.removeItemAtIndex(_items.size() - 1));
    }

    public Item removeItemAtIndex(int index) {
        if (index < 0 || index >= _items.size()) {
            throw new RuntimeException("Impossible index given for container. " + index  + " is greater than size.");
        }

        Item item = _items.remove(index);
        this.setVisibilityOnContents();
        return item;
    }

    public Optional<Item> itemAtIndex(int index) {
        if (index >= _capacity) {
            throw new RuntimeException("Impossible index given for container. " + index  + " is outside of capacity.");
        }

        if (index < _items.size()) {
            return Optional.of(_items.get(index));
        } else {
            return Optional.empty();
        }
    }

    public int itemCount() {
        return _items.size();
    }

    public int capacity() {
        return _capacity;
    }
}
