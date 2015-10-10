package swen.adventure.game.scenenodes;

/**
 * Created by josephbennett on 29/09/15
 */
public class Inventory extends Container {
    public static final int InventoryCapacity = 5;

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

    public int selectedSlot() {
        return _selectedSlot;
    }
}
