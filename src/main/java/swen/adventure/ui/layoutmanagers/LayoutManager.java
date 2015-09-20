package swen.adventure.ui.layoutmanagers;

import processing.core.PGraphics;
import swen.adventure.ui.components.UIComponent;

import java.util.List;

/**
 * Created by danielbraithwt on 9/19/15.
 */
public abstract class LayoutManager {
    private static final int DEFAULT_PADDING = 20;

    protected List<UIComponent> components;

    protected int padding;

    protected int width;
    protected int height;

    public void setComponents(List<UIComponent> c) {
        components = c;

        padding = DEFAULT_PADDING;
    }

    public int getWidth() {
        return width + padding;
    }

    public int getHeight() {
        return height + padding;
    }

    public abstract void applyLayout(PGraphics g);
}
