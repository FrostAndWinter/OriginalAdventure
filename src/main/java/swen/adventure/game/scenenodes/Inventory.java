/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Input;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.Optional;

/**
 * An inventory is a container that belongs to a player. It has a slot that is selected and always holds 5 items.
 *
 * Joseph Bennett, 30019773
 * Daniel Braithwaite, 300313770
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

    public Inventory(String id, TransformNode parent) {
        super(id, parent, InventoryCapacity);
        this.setShowTopItem(false);
    }

    /**
     * Select the given slot in this player's inventory.
     *
     * @param slot the slot to select
     */
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

    /**
     * Returns the item that is currently selected.
     *
     * @return the item that is currently selected.
     */
    public Optional<Item> selectedItem() {
        return itemAtIndex(_selectedSlot);
    }

    /**
     * Returns the slot number that is selected.
     *
     * @return the slot number that is selected.
     */
    public int selectedSlot() {
        return _selectedSlot;
    }
}