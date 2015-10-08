package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 8/10/15.
 */
public class Container extends GameObject {
    /** If this container should display its top item, then that item will be parented to this transform. */
    private final Optional<TransformNode> _childItemsTransform;
    private Stack<Item> _items = new Stack<>();
    private final int _capacity;

    public Container(final String id, final TransformNode parent, final int capacity, final TransformNode childItemsTransform) {
        super(id, parent);
        _childItemsTransform = Optional.ofNullable(childItemsTransform);
        _capacity = capacity;
    }

    public Container(final String id, final TransformNode parent, final int capacity) {
        this(id, parent, capacity, null);
    }

    private void setVisibilityOnContents() {
        if (_childItemsTransform.isPresent()) {
            for (Item item : _items) {
                item.setEnabled(false);
            }
            this.peek().ifPresent(item -> item.setEnabled(true));
        }
    }

    public boolean push(Item item) {
        if (_items.size() < _capacity) {
            _items.push(item);

            _childItemsTransform.ifPresent(item.parent().get()::setParent);

            this.setVisibilityOnContents();

            return true;
        }
        return false;
    }

    public Optional<Item> peek() {
        return _items.isEmpty() ? Optional.empty() : Optional.of(_items.peek());
    }

    public Optional<Item> pop() {
        this.setVisibilityOnContents();
        return _items.isEmpty() ? Optional.empty() : Optional.of(_items.pop());
    }

}
