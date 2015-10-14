/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.components;

import processing.core.PGraphics;
import processing.core.PImage;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;

/**
 * Created by danielbraithwt on 9/23/15.
 *
 * Image view is a ui component that displays an image
 */
public class ImageView extends UIComponent {

    private PImage image;

    public ImageView(PImage image, int x, int y, int width, int height) {
        super(x, y, width, height);

        this.image = image;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        g.image(image, x * scaleX, y * scaleY, width * scaleX, height * scaleY);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public int getWidth(PGraphics g) {
        return width;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public int getHeight(PGraphics g) {
        return height;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    protected void componentClicked(int x, int y) {
        // Image doesn't do anything when clicked
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Button cant use a layout manager");
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }
}