/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.components;

import processing.core.PGraphics;
import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.layoutmanagers.LinearLayout;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Panel extends swen.adventure.engine.ui.components.UIComponent {

    private boolean dynamicSize;
    private Color color;

    public Panel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Panel(int x, int y) {
        super(x, y, 0, 0);

        dynamicSize = true;

        setLayoutManager(new LinearLayout(LinearLayout.LINEAR_LAYOUT_VERTICAL));
    }

    /**
     * Set the background color of the panel
     * @param c background color
     */
    public void setColor(Color c) {
        color = c;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {

        if (color == null) {
            g.fill(23, 54, 123);
        } else {
            g.fill(color.getB(), color.getG(), color.getB(), color.getA());
        }
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
            c.mouseClicked(x - this.x, y - this.y);
        }
    }
}