/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.components;

import processing.core.PGraphics;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/15/15.
 *
 * Base class for all the UI Components, defines the
 * basic functionality all the components must have.
 */
public abstract class UIComponent {
    protected PGraphics graphics;
    protected List<UIComponent> children;
    private LayoutManager manager;
    private boolean visible = true;

    protected int x;
    protected int y;
    protected int width;
    protected int height;

    /**
     * Takes the dimensions of the components
     *
     * @param x x position of the component
     * @param y y position of the component
     * @param w width of the component
     * @param h height of the component
     */
    public UIComponent(int x, int y, int w, int h) {
        children = new ArrayList<>();

        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    /**
     * Calls the draw method with the scaling set to 1,1
     * @param g pGraphics to be drawn to
     */
    public void draw(PGraphics g) {
        draw(g, 1, 1);
    }

    /**
     * Draws the components to the graphcis with specified scale
     * @param g pGraphics to be drawn to
     * @param scaleX how much to scale the x
     * @param scaleY how much to scale the y
     */
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

    /**
     * Controls how the component is drawn
     * @param g pGraphics to be drawn to
     * @param scaleX scale for the x
     * @param scaleY scale for the y
     */
    protected abstract void drawComponent(PGraphics g, float scaleX, float scaleY);

    protected void drawComponent(PGraphics g) {
        draw(g, 1, 1);
    }

    /**
     * Adds a UI component to the list of children for this element
     * @param c
     */
    public void addChild(UIComponent c) {
        children.add(c);
    }

    /**
     * Removes a UI component from the list of children for this element     *
     * @param c
     */
    public void removeChild(UIComponent c) {
        children.remove(c);
    }

    /**
     * Sets the layout manager for this component
     * @param lm layout manager to use
     */
    public void setLayoutManager(LayoutManager lm) {
        manager = lm;
        manager.setComponents(children);
    }

    /**
     * Handles passing mouse clicks to the child components
     * @param x x position of the mouse click
     * @param y y position of the mouse click
     * @param scaleX scale for the x
     * @param scaleY scale for the y
     */
    public void mouseClicked(int x, int y, float scaleX, float scaleY) {
        mouseClicked((int) (x / scaleX), (int) (y / scaleY));
    }

    /**
     * If the component is visible will call the action defined
     * for when the component is clicked
     * @param x x position of the click
     * @param y y position of the click
     */
    public void mouseClicked(int x, int y) {
        if (!visible) {
            return;
        }

        if (this.withinBounds(x, y)) {
            componentClicked(x, y);
        }
    }

    /**
     * Sets the visiblity of the component
     * @param v true to set the component visible otherwise false
     */
    public void setVisible(boolean v) {
        visible = v;
    }

    /**
     * Gets the visibility of the component
     * @return true if the component is visible otherwise false
     */
    public boolean getVisible() {
        return visible;
    }

    /**
     * Method defines what happens when the component is clicked
     * @param x x position of click
     * @param y y position of click
     */
    protected abstract void componentClicked(int x, int y);

    /**
     * @return the x position of the component
     */
    public int getX() {
        return x;
    }

    /**
     * @param x new x position for the component
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return y position of the component
     */
    public int getY() {
        return y;
    }

    /**
     * @param y new y position of the component
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Gets the width of the component in the context of
     * a PGraphics object. Some components involving text
     * there size will depend on the font
     *
     * @param g pGraphics we would be drawing to
     * @return width of component in the context of g
     */
    public int getWidth(PGraphics g) {
        return width;
    }

    /**
     * @param width new width of the component
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Gets the height of the component in the context of
     * a PGraphics object. Some components involving text
     * there size will depend on the font
     *
     * @param g pGraphics we would be drawing to
     * @return height of component in the context of g
     */
    public int getHeight(PGraphics g) {
        return height;
    }

    /**
     * @param height new height of the component
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Checks to see if a coord is within the bounds of a component
     * @param x
     * @param y
     * @return true if (x,y) is within the component otherwise false
     */
    public abstract boolean withinBounds(int x, int y);

}