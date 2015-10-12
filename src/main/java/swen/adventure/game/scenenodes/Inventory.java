package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Input;

import java.util.Optional;

/**
 * Created by josephbennett on 29/09/15
 * Modefied by Daniel Braithwaite id: 300313770
 */
public class Inventory extends Container {
    public static final int InventoryCapacity = 5;

    public static final Action<Input, Input, Inventory> actionSelectSlot1 = (eventObject, triggeringObject, listener, data) -> {
        listener.selectSlot(0);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot2 = (eventObject, triggeringObject, listener, data) -> {
        listener.selectSlot(1);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot3 = (eventObject, triggeringObject, listener, data) -> {
        listener.selectSlot(2);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot4 = (eventObject, triggeringObject, listener, data) -> {
        listener.selectSlot(3);
    };

    public static final Action<Input, Input, Inventory> actionSelectSlot5 = (eventObject, triggeringObject, listener, data) -> {
        listener.selectSlot(4);
    };

    /**
     * The slot that is currently selected
     */
    private int _selectedSlot = 0;

    private Player _player;

    public Inventory(Player player) {
        super(player.id + "Inventory", player.parent().get(), InventoryCapacity);
        _player = player;

        this.setShowTopItem(false);
    }

    public Player player() {
        return _player;
    }

    public void selectSlot(int slot) {
        if (slot < 0 || slot >= this.capacity()) {
            throw new IllegalArgumentException("Given slot cannot be selected. Available slots between 0 and " + this.capacity());
        }

        _selectedSlot = slot;
    }

    public Optional<Item> selectedItem() {
        return itemAtIndex(_selectedSlot);
    }

    public int selectedSlot() {
        return _selectedSlot;
    }
}
