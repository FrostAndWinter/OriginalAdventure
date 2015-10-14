/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.layoutmanagers;

import processing.core.PGraphics;
import swen.adventure.engine.ui.components.UIComponent;

import java.util.List;

/**
 * Created by danielbraithwt on 9/19/15.
 */
public abstract class LayoutManager {
    private static final int DEFAULT_PADDING = 20;

    protected List<UIComponent> components;

    protected int padding; // Padding round the edge of the component

    protected int width;
    protected int height;

    public void setComponents(List<UIComponent> c) {
        components = c;

        padding = DEFAULT_PADDING;
    }

    /**
     * @return the width of the layout including padding
     */
    public int getWidth() {
        return width + padding;
    }

    /**
     * @return the height of the layout including padding
     */
    public int getHeight() {
        return height + padding;
    }

    /**
     * Will change position and size of the components given
     * to the layout manager
     * @param g pGraphics that will be drawn to
     */
    public abstract void applyLayout(PGraphics g);
}