/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.components;

import processing.core.PGraphics;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;

/**
 * Created by danielbraithwt on 9/19/15.
 *
 * UI Component to display text on a interface
 */
public class TextBox extends UIComponent {
    private static final int DEFAULT_PADDING = 20;

    private String text;
    private int padding;

    public TextBox(String text, int x, int y) {
        super(x, y, 0, 0);

        this.text = text;

        padding = DEFAULT_PADDING;
    }

    /**
     * Sets the text to be displayed in the
     * text box
     *
     * @param t string to be displayed
     */
    public void setText(String t) {
        text = t;
    }

    public void setPadding(int p) {
        padding = p;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        int stringWidth = (int) g.textWidth(text);
        int stringHeight = (int) (g.textAscent() + g.textDescent());

        width = padding + stringWidth;
        height = padding + stringHeight;

        g.fill(255);
        g.text(text.toCharArray(), 0, text.length(), (x + padding/2) * scaleX, (y + stringHeight + padding/2) * scaleY);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    public int getWidth(PGraphics g) {
        return padding + (int) g.textWidth(text);
    }

    @Override
    public int getHeight(PGraphics g) {
        return padding + (int) (g.textAscent() + g.textDescent());
    }

    @Override
    protected void componentClicked(int x, int y) {
    }

    @Override
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Button cant use a layout manager");
    }

    @Override
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }

    @Override
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }

}