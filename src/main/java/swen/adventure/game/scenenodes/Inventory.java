package swen.adventure.game.scenenodes;

import swen.adventure.engine.Event;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josephbennett on 29/09/15
 */
public class Inventory extends SceneNode {

    public static final String SelectedSlot = "selectedSlot";

    /**
     * The slot that is currently selected
     */
    private int selectedSlot = 0;

    /**
     * The capacity of the inventory (i.e how many game objects it can hold)
     */
    private final int capacity = 5;


    private List<Item> items = new ArrayList<>(capacity);
    private Player _player;

    public Inventory(Player player) {
        super(player.id + "Inventory", player.parent().get(), false);
        _player = player;


         Event.EventSet<Item, Player> eventSetItemPickup = (Event.EventSet<Item, Player>) Event.eventSetForName("eventItemPickup");
         eventSetItemPickup.addAction(this, (item, playerWhoPickedUpItem, listener, data) -> {
             // make sure this is inventory of the player who picked up the item and not someone else.
             if (_player.equals(playerWhoPickedUpItem)) {
                storeItem(item);
             }
         });
    }

    public void selectSlot(int slot) {
        if (slot < 0 || slot > capacity) {
            throw new IllegalArgumentException("Given slot cannot be selected. Available slots between 0 and " + capacity);
        }

        selectedSlot = slot;
    }

    /**
     * Stores the given item in the inventory.
     *
     * @param item game object to store
     * @return true if inventory has enough space, false otherwise.
     */
    public boolean storeItem(Item item) {
        if (items.size() < capacity) {
            item.setEnabled(false); // hide the item in the world

            items.add(item);
            return true;
        }

        return false; // inventory is full
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public final Event<Inventory, Player> eventItemSelected = new Event<>("eventItemSelected", this);
}
