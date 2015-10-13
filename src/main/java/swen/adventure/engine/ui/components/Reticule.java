package swen.adventure.engine.ui.components;

import processing.core.PConstants;
import processing.core.PGraphics;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;

/**
 * Created by danielbraithwt on 10/3/15.
 */
public class Reticule extends UIComponent {
    public Reticule(int x, int y, int r) {
        super(x, y, r * 2, r * 2);
    }

    @Override
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        int radius = (int) (width * scaleX)/2;

        g.fill(255,255,255,100);
        g.ellipseMode(PConstants.CORNER);
        g.ellipse(x * scaleX - radius, y * scaleY - radius, width * scaleX, height * scaleY);
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    public int getWidth(PGraphics g) {
        return width;
    }

    @Override
    public int getHeight(PGraphics g) {
        return height;
    }

    @Override
    protected void componentClicked(int x, int y) {
    }

    @Override
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Button cant use a layout manager");
    }

    @Override
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }

    @Override
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }
}
