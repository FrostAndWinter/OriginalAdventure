package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Panel extends UIComponent {
    private List<UIComponent> children;

    private int x;
    private int y;
    private int width;
    private int height;

    public Panel(PApplet p, int x, int y, int width, int height) {
        super(p);

        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;

        children = new ArrayList<>();
    }

    @Override
    public void drawComponent(PGraphics g) {

        g.fill(23, 54, 123);
        g.rect(x, y, width, height);

        g.translate(x, y);

        for (UIComponent c : children) {
            c.draw(g);
        }
    }

    public void addChild(UIComponent c) {
        children.add(c);
    }

    public void removeChild(UIComponent c) {
        children.remove(c);
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return true;
    }

    @Override
    protected void componentClicked(MouseEvent e) {
        for (UIComponent c : children) {
            c.mouseClicked(e);
        }
    }
}
