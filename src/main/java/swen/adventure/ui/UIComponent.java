package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;

import processing.event.MouseEvent;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public abstract class UIComponent {
    protected PApplet applet;
    private boolean visible = true;

    public UIComponent(PApplet a) {
        applet = a;
    }

    public void draw(PGraphics g) {
        if (!visible) {
            return;
        }

        drawComponent(g);
    }

    protected abstract void drawComponent(PGraphics g);

    public abstract boolean withinBounds(int x, int y);

    public void mouseClicked(MouseEvent e) {
        if (!visible) {
            return;
        }

        if (this.withinBounds(e.getX(), e.getY())) {
            componentClicked(e);
        }
    }

    public void setVisible(boolean v) {
        visible = v;
    }

    public boolean getVisible() {
        return visible;
    }

    protected abstract void componentClicked(MouseEvent e);
}
