package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Frame extends UIComponent {

    public Frame(PApplet p, int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        for (UIComponent c : children) {
            c.draw(g, scaleX, scaleY);
        }
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return true;
    }

    @Override
    protected void componentClicked(int x, int y) {
        for (UIComponent c : children) {
            c.mouseClicked(x, y);
        }
    }
}
