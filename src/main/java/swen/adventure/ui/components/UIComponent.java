package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PGraphics;

import swen.adventure.ui.layoutmanagers.LayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public abstract class UIComponent {
    protected PApplet applet;
    protected List<UIComponent> children;
    private LayoutManager manager;
    private boolean visible = true;

    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public UIComponent(PApplet a, int x, int y, int w, int h) {
        applet = a;
        children = new ArrayList<>();

        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public void draw(PGraphics g) {
        draw(g, 1, 1);
    }

    public void draw(PGraphics g, float scaleX, float scaleY) {
        if (!visible) {
            return;
        }

        if (manager != null) {
            manager.applyLayout(g);

            width = manager.getWidth();
            height = manager.getHeight();
        }

        drawComponent(g, scaleX, scaleY);
    }

    public void addChild(UIComponent c) {
        children.add(c);
    }

    public void removeChild(UIComponent c) {
        children.remove(c);
    }

    public void setLayoutManager(LayoutManager lm) {
        manager = lm;
        manager.setComponents(children);
    }

    public void mouseClicked(int x, int y, float scaleX, float scaleY) {
        mouseClicked((int) (x / scaleX), (int) (y / scaleY));
    }

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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth(PGraphics g) {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight(PGraphics g) {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    protected abstract void drawComponent(PGraphics g, float scaleX, float scaleY);

    protected void drawComponenet(PGraphics g) {
        draw(g, 1, 1);
    }

    public abstract boolean withinBounds(int x, int y);

}
