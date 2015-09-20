package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PGraphics;
import swen.adventure.ui.layoutmanagers.LinearLayout;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Panel extends UIComponent {

    private boolean dynamicSize;

    public Panel(PApplet p, int x, int y, int width, int height) {
        super(p, x, y, width, height);
    }

    public Panel(PApplet p, int x, int y) {
        super(p, x, y, 0, 0);

        dynamicSize = true;

        setLayoutManager(new LinearLayout(LinearLayout.LINEAR_LAYOUT_VERTICAL));
    }

    @Override
    public void drawComponent(PGraphics g) {

        g.fill(23, 54, 123);
        g.rect(x, y, width, height);

        // Translate coord system so that positioning inside the
        // panel is relative
        g.translate(x, y);

        for (UIComponent c : children) {
            c.draw(g);
        }

        g.translate(-x, -y);
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return true;
    }

    @Override
    protected void componentClicked(int x, int y) {
        for (UIComponent c : children) {
            c.mouseClicked(x - this.x, y - this.y);
        }
    }
}
