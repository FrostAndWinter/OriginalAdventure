package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import swen.adventure.ui.layoutmanagers.LayoutManager;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class ProgressBar extends UIComponent {
    private int maxValue;
    private int count;

    private PFont font;

    public ProgressBar(PApplet a, int maxValue, int startingVal, int x, int y) {
        super(a, x, y, 200, 25);

        this.count = startingVal;
        this.maxValue = maxValue;

        this.font = applet.createFont("Arial", 16);
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
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        // Draw the grey bar
        g.fill(150, 130, 180);
        g.rect(x * scaleX, y * scaleY, width * scaleX, height * scaleY);

        // Draw the filled portion of the bar
        int completed = (width/maxValue) * count;

        g.fill(255, 0, 0);
        g.rect(x * scaleX, y * scaleY, completed * scaleX, height * scaleY);

        // Draw the current bar value
        g.textFont(font);
        g.color(255);
        g.fill(255);
        String countString = String.format("%d", count);

        int stringWidth = (int) g.textWidth(countString);
        int stringHeight = (int) (g.textAscent() + g.textDescent());

        g.text(countString.toCharArray(), 0, countString.length(), (x + width / 2 - stringWidth / 2)  * scaleX, (y + stringHeight) * scaleY);
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    protected void componentClicked(int x, int y) {}

    @Override
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Inventory cant use a layout manager");
    }

    @Override
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }

    @Override
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }
}
