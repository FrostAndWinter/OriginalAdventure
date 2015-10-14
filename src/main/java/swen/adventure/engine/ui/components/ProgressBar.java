/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.components;

import processing.core.PGraphics;
import swen.adventure.engine.ui.layoutmanagers.LayoutManager;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class ProgressBar extends UIComponent {
    private int maxValue;
    private int count;

    public ProgressBar(int maxValue, int startingVal, int x, int y) {
        super(x, y, 200, 25);

        this.count = startingVal;
        this.maxValue = maxValue;
    }

    /**
     * Update the progress of the progress bar
     *
     * @param delta ammount to update the progress bar
     * @return the new value of the progress bar
     */
    public int changeProgress(int delta) {

        if (count + delta < maxValue) {
            count += delta;
        } else {
            count = maxValue;
        }

        return count;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        // Draw the grey bar
        g.fill(150, 130, 180);
        g.rect(x * scaleX, y * scaleY, width * scaleX, height * scaleY);

        // Draw the filled portion of the bar
        int completed = (width/maxValue) * count;

        g.fill(255, 0, 0);
        g.rect(x * scaleX, y * scaleY, completed * scaleX, height * scaleY);

        // Draw the current bar value
        //g.textFont(font);
        g.color(255);
        g.fill(255);
        String countString = String.format("%d", count);

        int stringWidth = (int) g.textWidth(countString);
        int stringHeight = (int) (g.textAscent() + g.textDescent());

        g.text(countString.toCharArray(), 0, countString.length(), (x + width / 2 - stringWidth / 2)  * scaleX, (y + stringHeight) * scaleY);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    protected void componentClicked(int x, int y) {}

    @Override
    /**
     * {@inheritDoc}
     */
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Inventory cant use a layout manager");
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Inventory cant contain child ui elements");
    }
}