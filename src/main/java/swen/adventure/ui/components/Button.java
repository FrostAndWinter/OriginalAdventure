package swen.adventure.ui.components;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

import swen.adventure.ui.color.Color;
import swen.adventure.ui.layoutmanagers.LayoutManager;
import swen.adventure.ui.clickable.ClickEvent;
import swen.adventure.ui.clickable.Clickable;
import swen.adventure.ui.clickable.OnClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public class Button extends UIComponent implements Clickable {
    private static final int DEFAULT_PADDING = 20;

    protected String text;
    protected int padding;

    private boolean dynamicSize;

    private List<OnClickListener> listeners;

    private PFont font;
    private Color color;

    public Button(String text, int x, int y) {
        super(x, y, 0, 0);

        this.text = text;

        dynamicSize = true;

        listeners = new ArrayList<>();

        padding = DEFAULT_PADDING;
    }

    public Button(String text, int x, int y, int height, int width) {
        super(x, y, height, width);

        this.text = text;

        dynamicSize = false;

        listeners = new ArrayList<>();

        padding = DEFAULT_PADDING;
    }

    public void setPadding(int p) {
        padding = p;
    }

    public void setFont(PFont f) {
        font = f;
    }

    public void setColor(Color c) {
        color = c;
    }


    @Override
    public void drawComponent(PGraphics g, float scaleX, float scaleY) {
        int stringWidth = (int) g.textWidth(text);
        int stringHeight = (int) (g.textAscent() + g.textDescent());

        if (dynamicSize) {
            width = padding + stringWidth;
            height = padding + stringHeight;
        }

        int stringX = (width - stringWidth)/2;
        int stringY = (height - stringHeight)/2;
        // Draw the background
        g.fill(255);

        if(color == null) {
            g.color(50);
        } else {
            g.fill(color.getB(), color.getG(), color.getB(), color.getA());
        }

        g.rect(x * scaleX, y * scaleY, width * scaleX, height * scaleY);


        g.fill(0);
        g.textFont(font, font.getSize());

        g.text(text.toCharArray(), 0, text.length(), (stringX + padding/2) * scaleX, (stringY + stringHeight + padding/2) * scaleY);
    }

    public synchronized void addClickListener(OnClickListener c) {
        listeners.add(c);
    }

    public synchronized void removeClickListener(OnClickListener c) {
        listeners.add(c);
    }

    public void clicked(int x, int y) {
        ClickEvent event = new ClickEvent(this);

        for (OnClickListener l : listeners) {
            l.onClick(event);
        }
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    public int getWidth(PGraphics g) {
        if (dynamicSize) {
            g.textFont(font);
            return padding + (int) g.textWidth(text);
        }

        return width;
    }

    @Override
    public int getHeight(PGraphics g) {
        if (dynamicSize) {
            g.textFont(font);
            return padding + (int) (g.textAscent() + g.textDescent());
        }

        return height;
    }

    @Override
    protected void componentClicked(int x, int y) {
        clicked(x, y);
    }

    @Override
    public void setLayoutManager(LayoutManager lm) {
        throw new UnsupportedOperationException("Button cant use a layout manager");
    }

    @Override
    public void addChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }

    @Override
    public void removeChild(UIComponent c) {
        throw new UnsupportedOperationException("Button cant contain child ui elements");
    }
}
