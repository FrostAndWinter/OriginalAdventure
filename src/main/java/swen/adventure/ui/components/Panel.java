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
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {

        g.fill(23, 54, 123);
        g.rect(x * scaleX, y * scaleY, width * scaleX, height * scaleY);

        // Translate coord system so that positioning inside the
        // panel is relative
        g.translate(x * scaleX, y * scaleY);

        for (UIComponent c : children) {
            c.draw(g, scaleX, scaleY);
        }

        g.translate(-x * scaleX, -y * scaleY);
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
