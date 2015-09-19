package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Frame extends UIComponent {

    private List<UIComponent> children;

    public Frame(PApplet p) {
        super(p);

        children = new ArrayList<>();
    }

    @Override
    public void drawComponent(PGraphics g) {
        for (UIComponent c : children) {
            c.draw(g);
        }
    }

    public void addChild(UIComponent c) {
        children.add(c);
    }

    public void removeChild(UIComponent c) {
        children.remove(c);
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
