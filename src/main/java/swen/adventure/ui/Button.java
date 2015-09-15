package swen.adventure.ui;

import processing.core.PGraphics;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public class Button extends Componenet {
    protected int x;
    protected int y;
    protected int height;
    protected int width;

    public Button(int x, int y, int height, int width) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    @Override
    public void draw(PGraphics g) {
        g.beginDraw();
        g.color(50);
        g.rect(x, y, height, width);
        g.endDraw();
    }
}
