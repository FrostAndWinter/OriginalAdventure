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
        super(p, x, y, w, h);
    }

    @Override
    public void drawComponent(PGraphics g) {
        for (UIComponent c : children) {
            c.draw(g);
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
