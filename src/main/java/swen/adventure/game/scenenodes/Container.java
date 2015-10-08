package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.*;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 8/10/15.
 */
public class Container extends GameObject {
    private final List<Item> _items;
    private final int _capacity;

    /** If this container should display its top item, then that item will be parented to this transform and made visible. */
    private final boolean _showTopItem;

    public Container(final String id, final TransformNode parent, final int capacity, final boolean showTopItem) {
        super(id, parent);
        _capacity = capacity;
        _showTopItem = showTopItem;
        _items = new ArrayList<>(capacity);
    }

    private void setVisibilityOnContents() {
        if (_showTopItem) {
            for (Item item : _items) {
                item.setEnabled(false);
            }
            this.peek().ifPresent(item -> item.setEnabled(true));
        }
    }

    public boolean push(Item item) {
        if (_items.size() < _capacity) {
            _items.add(item);
            item.setContainer(this);

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
        Item item = _items.remove(_items.size() - 1);
        item.setContainer(null);
        this.setVisibilityOnContents();
        return Optional.of(item);
    }

    public Item itemAtIndex(int index) {
        return _items.get(index);
    }

}
