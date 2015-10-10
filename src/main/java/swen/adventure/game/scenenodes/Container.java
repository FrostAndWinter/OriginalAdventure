package swen.adventure.game.scenenodes;

import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.Interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Thomas Roughton, Student ID 300313924, on 8/10/15.
 */
public class Container extends AdventureGameObject {

    private final List<Item> _items;
    private final int _capacity;

    private boolean _showTopItem;

    public Container(final String id, final TransformNode parent, final int capacity) {
        super(id, parent);
        _capacity = capacity;
        _items = new ArrayList<>(capacity);
    }

    /** If this container should display its top item, then that item will be made visible. */
    public void setShowTopItem(boolean showTopItem) {
        _showTopItem = showTopItem;
        this.setVisibilityOnContents();;
    }

    private void setVisibilityOnContents() {
        if (_showTopItem) {
            for (Item item : _items) {
                item.setEnabled(false);
            }
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

        Item item = _items.remove(_items.size() - 1);
        this.setVisibilityOnContents();
        return Optional.of(item);
    }

    public Optional<Item> itemAtIndex(int index) {
        if (index < itemCount()) {
            return Optional.of(_items.get(index));
        }

        return Optional.empty();
    }

    public int itemCount() {
        return _items.size();
    }

    public int capacity() {
        return _capacity;
    }

    @Override
    public void performInteraction(Interaction interaction, MeshNode meshNode, Player player) {
        super.performInteraction(interaction, meshNode, player);
        switch (interaction.interactionType) {
            case PlaceIn:
                player.inventory().selectedItem().ifPresent(item -> {
                    item.moveToContainer(container().get());
                    item.eventPlayerDroppedItem.trigger(player, Collections.emptyMap());
                });
                break;
            default:
                break;
        }
    }
}
