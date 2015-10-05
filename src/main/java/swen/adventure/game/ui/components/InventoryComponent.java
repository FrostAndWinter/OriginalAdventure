package swen.adventure.game.ui.components;

import processing.core.PGraphics;
import swen.adventure.engine.Action;
import swen.adventure.engine.ui.components.UIComponent;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;
import swen.adventure.game.scenenodes.Inventory;
import swen.adventure.game.scenenodes.Player;

/**
 * Created by danielbraithwt on 9/18/15.
 */
public class InventoryComponent extends UIComponent {
    private static final int BOX_SIZE = 30;

    private Object[] items;

    private int numItems;
    private int boxSize;

    private int selectedItem = 0;

    public InventoryComponent(int numItems, int x, int y) {
        super(x, y, numItems * BOX_SIZE, BOX_SIZE);

        this.numItems = numItems;
        boxSize = BOX_SIZE;

        items = new Object[numItems];
    }

    public static final Action<Inventory, Player, InventoryComponent> actionSelectSlot =
            (playerInventory, triggeringObject, inventoryView, data) -> {
                Integer item = (Integer) data.get(Inventory.SelectedSlot);
                inventoryView.setSelectedItem(item);
            };

    public void setBoxSize(int boxSize) {
        this.boxSize = boxSize;
    }

    public void setSelectedItem(int s) {
        selectedItem = s;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setItemAt(int index, Object o) {
        items[index] = o;
    }

    public Object getItemAt(int index) {
        return items[index];
    }

    @Override
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Inventory cant use a layout manager");
    }

    @Override
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }

    @Override
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }

    @Override
    protected void drawComponent(PGraphics g, float scaleX, float scaleY) {
        int currentX = x;
        int currentY = y;

        for (int i = 0; i < numItems; i++) {
            g.fill(34, 50, 90);
            g.rect(currentX * scaleX, currentY * scaleY, boxSize * scaleX, boxSize * scaleY);

            // If the item is selected
            if (i == selectedItem) {
                g.fill(255, 0, 0);
                g.rect(currentX * scaleX + 10, currentY * scaleY + 10, boxSize * scaleX - 20, boxSize * scaleY - 20);
            }

            currentX += boxSize;
        }
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    protected void componentClicked(int x, int y) {

    }
}
