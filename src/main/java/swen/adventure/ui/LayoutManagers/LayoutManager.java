package swen.adventure.ui.LayoutManagers;

import swen.adventure.ui.components.UIComponent;

import java.util.List;

/**
 * Created by danielbraithwt on 9/19/15.
 */
public abstract class LayoutManager {

    protected   List<UIComponent> components;

    protected int width;
    protected int height;

    public LayoutManager(List<UIComponent> c) {
        components = c;
    }

    public abstract void applyLayout();
}
