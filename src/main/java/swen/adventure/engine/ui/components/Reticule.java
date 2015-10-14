/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.components;

import processing.core.PConstants;
import processing.core.PGraphics;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;

/**
 * Created by danielbraithwt on 10/3/15.
 *
 * UI component to be used as a an indicator of where the user
 * is looking
 */
public class Reticule extends UIComponent {
    public Reticule(int x, int y, int r) {
        super(x, y, r * 2, r * 2);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        int radius = (int) (width * scaleX)/2;

        g.fill(255,255,255,100);
        g.ellipseMode(PConstants.CORNER);
        g.ellipse(x * scaleX - radius, y * scaleY - radius, width * scaleX, height * scaleY);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public int getWidth(PGraphics g) {
        return width;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public int getHeight(PGraphics g) {
        return height;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    protected void componentClicked(int x, int y) {
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Button cant use a layout manager");
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }
}