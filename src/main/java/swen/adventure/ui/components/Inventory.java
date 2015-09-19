package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PGraphics;
import swen.adventure.ui.LayoutManagers.LayoutManager;

/**
 * Created by danielbraithwt on 9/18/15.
 */
public class Inventory extends UIComponent {
    private static final int BOX_SIZE = 30;

    private int numItems;

    public Inventory(PApplet a, int numItems, int x, int y) {
        super(a, x, y, numItems * BOX_SIZE, BOX_SIZE);

        this.numItems = numItems;
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
    protected void drawComponent(PGraphics g) {
        int currentX = x;
        int currentY = y;

        g.fill(34, 50, 90);
        for (int i = 0; i < numItems; i++) {
            g.rect(currentX, currentY, BOX_SIZE, BOX_SIZE);
            currentX += BOX_SIZE;
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
