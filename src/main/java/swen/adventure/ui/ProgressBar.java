package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class ProgressBar extends UIComponent {
    private int x;
    private int y;
    private int width;
    private int height;

    private int maxValue;
    private int count;

    public ProgressBar(PApplet a, int maxValue, int x, int y) {
        super(a);

        x = 200;
        y = 200;
        height = 25;
        width = 200;

        count = 0;
        this.maxValue = maxValue;
    }

    public int changeProgress(int delta) {

        if (count + delta < maxValue) {
            count += delta;
        } else {
            count = maxValue;
        }

        return count;
    }

    @Override
    public void drawComponent(PGraphics g) {
        // Draw the grey bar
        g.fill(150, 130, 180);
        g.rect(x, y, width, height);

        int completed = (width/maxValue) * count;

        g.fill(255, 0, 0);
        g.rect(x, y, completed, height);
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    protected void componentClicked(int x, int y) {}
}
