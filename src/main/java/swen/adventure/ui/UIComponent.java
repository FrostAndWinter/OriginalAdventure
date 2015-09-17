package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;

import processing.event.MouseEvent;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public abstract class UIComponent {
    protected PApplet applet;

    public UIComponent(PApplet a) {
        applet = a;
    }

    public abstract void draw(PGraphics g);

    public abstract boolean withinBounds(int x, int y);

    public void mouseClicked(MouseEvent e) {
        if (this.withinBounds(e.getX(), e.getY())) {
            componentClicked(e);
        }
    }

    protected abstract void componentClicked(MouseEvent e);
}
