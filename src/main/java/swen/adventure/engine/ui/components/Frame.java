package swen.adventure.engine.ui.components;

import processing.core.PGraphics;

/**
 * Created by danielbraithwt on 9/17/15.
 *
 *
 */
public class Frame extends UIComponent {

    public Frame(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        for (UIComponent c : children) {
            c.draw(g, scaleX, scaleY);
        }
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean withinBounds(int x, int y) {
        return true;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    protected void componentClicked(int x, int y) {
        for (UIComponent c : children) {
            c.mouseClicked(x, y);
        }
    }
}
