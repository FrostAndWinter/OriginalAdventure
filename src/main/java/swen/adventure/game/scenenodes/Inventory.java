package swen.adventure.game.scenenodes;

import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.engine.scenegraph.TransformNode;

import java.util.*;

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

    private List<GameObject> items = new ArrayList<>(capacity);

    public Inventory(String id, TransformNode parent) {
        super(id, parent, false);
    }

    public void selectSlot(int slot) {
        if (slot < 0 || slot > capacity) {
            throw new IllegalArgumentException("Given slot cannot be selected. Available slots between 0 and " + capacity);
        }

        selectedSlot = slot;
        eventItemSelected.trigger(null, Collections.singletonMap(SelectedSlot, slot));
    }

    /**
     * Stores the given game object in the inventory.
     *
     * @param gameObject game object to store
     * @return true if inventory has enough space, false otherwise.
     */
    public boolean storeItem(GameObject gameObject) {
        if (items.size() < capacity) {
            gameObject.changeParentTo(this.parent().get());

            items.add(gameObject);
            return true;
        }

        return false; // inventory is full
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public final Event<Inventory> eventItemSelected = new Event<>("eventItemSelected", this);
}
