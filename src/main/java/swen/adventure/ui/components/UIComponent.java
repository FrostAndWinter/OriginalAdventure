package swen.adventure.ui.components;

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

    public void mouseClicked(int x, int y) {
        if (!visible) {
            return;
        }

        if (this.withinBounds(x, y)) {
            componentClicked(x, y);
        }
    }

    public void setVisible(boolean v) {
        visible = v;
    }

    public boolean getVisible() {
        return visible;
    }

    protected abstract void componentClicked(int x, int y);
}
