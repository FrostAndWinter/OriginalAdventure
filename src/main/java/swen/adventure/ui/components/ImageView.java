package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import swen.adventure.ui.layoutmanagers.LayoutManager;

/**
 * Created by danielbraithwt on 9/23/15.
 */
public class ImageView extends UIComponent {

    private PImage image;

    public ImageView(PImage image, int x, int y, int width, int height) {
        super(x, y, width, height);

        this.image = image;
    }

    @Override
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        g.image(image, x * scaleX, y * scaleY, width * scaleX, height * scaleY);
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    public int getWidth(PGraphics g) {
        return width;
    }

    @Override
    public int getHeight(PGraphics g) {
        return height;
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
