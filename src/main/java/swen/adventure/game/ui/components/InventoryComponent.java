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

    private Inventory inventory;

    private int numItems;
    private int boxSize;

    private int selectedItem = 0;

    public InventoryComponent(Inventory inventory, int x, int y) {
        super(x, y, inventory.getCapacity() * BOX_SIZE, BOX_SIZE);

        this.numItems = numItems;
        boxSize = BOX_SIZE;

        this.inventory = inventory;
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

        float scale = Math.min(scaleX, scaleY);

        for (int i = 0; i < numItems; i++) {
            g.fill(34, 50, 90);
            g.rect(currentX * scaleX, currentY * scaleY, boxSize * scaleX, boxSize * scaleY);

            // If the item is selected
//            if (i == selectedItem) {
//                g.fill(255, 0, 0);
//                g.rect((currentX + 10) * scaleX, (currentY + 10) * scaleY, (boxSize - 20) * scaleX, (boxSize- 20) * scaleY);
//            }

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
