package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PGraphics;
import swen.adventure.ui.layoutmanagers.LayoutManager;

import java.util.Objects;

/**
 * Created by danielbraithwt on 9/18/15.
 */
public class Inventory extends UIComponent {
    private static final int BOX_SIZE = 30;

    private Object[] items;

    private int numItems;
    private int boxSize;

    private int selectedItem = 0;

    public Inventory(int numItems, int x, int y) {
        super(x, y, numItems * BOX_SIZE, BOX_SIZE);

        this.numItems = numItems;
        boxSize = BOX_SIZE;

        items = new Object[numItems];
    }

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

        g.fill(34, 50, 90);
        for (int i = 0; i < numItems; i++) {
            g.rect(currentX * scaleX, currentY * scaleY, boxSize * scaleX, boxSize * scaleY);
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
