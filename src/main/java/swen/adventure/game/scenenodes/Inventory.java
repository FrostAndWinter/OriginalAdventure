package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Input;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Optional;

/**
 * Created by josephbennett on 29/09/15
 * Modefied by Daniel Braithwaite id: 300313770
 */
public class Inventory extends Container {
    public static final int InventoryCapacity = 5;

    public static final Action<Input, Input, Inventory> actionSelectSlot1 = (eventObject, triggeringObject, inventory, data) -> {
        inventory.selectSlot(0);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot2 = (eventObject, triggeringObject, inventory, data) -> {
        inventory.selectSlot(1);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot3 = (eventObject, triggeringObject, inventory, data) -> {
        inventory.selectSlot(2);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot4 = (eventObject, triggeringObject, inventory, data) -> {
        inventory.selectSlot(3);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot5 = (eventObject, triggeringObject, inventory, data) -> {
        inventory.selectSlot(4);
    };

    /**
     * The slot that is currently selected
     */
    private int _selectedSlot = 0;

    public Inventory(Player player) {
        super(player.id + "Inventory", player.parent().get(), InventoryCapacity);
        this.setShowTopItem(false);
    }

    public Inventory(String id, TransformNode parent) {
        super(id, parent, InventoryCapacity);
        this.setShowTopItem(false);
    }

    public void selectSlot(int slot) {
        if (slot < 0 || slot >= this.capacity()) {
            throw new IllegalArgumentException("Given slot cannot be selected. Available slots between 0 and " + this.capacity());
        }

        _selectedSlot = slot;
    }


    @Override
    public Optional<Item> pop() {
        if (this.itemCount() == 0) {
            return Optional.empty();
        }

        return Optional.of(this.removeItemAtIndex(_selectedSlot));
    }

    public Optional<Item> selectedItem() {
        return itemAtIndex(_selectedSlot);
    }

    public int selectedSlot() {
        return _selectedSlot;
    }
}
