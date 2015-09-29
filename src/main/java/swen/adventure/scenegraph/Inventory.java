package swen.adventure.scenegraph;

import swen.adventure.Event;

import java.util.Collections;

/**
 * Created by josephbennett on 29/09/15
 */
public class Inventory extends SceneNode {

    private int selectedItem = 0;

    public Inventory(String id, TransformNode parent) {
        super(id, parent, false);
    }

    public void selectItem(int item) {
        selectedItem = item;
        eventItemSelected.trigger(null, Collections.singletonMap("item", item));
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public final Event<Inventory> eventItemSelected = new Event<>("eventItemSelected", this);
}
