package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public class Button extends Component implements Clickable {
    protected String text;
    protected int x;
    protected int y;
    protected int height;
    protected int width;

    private boolean dynamicSize;

    private List<OnClickListener> listeners;

    private PFont font;

    public Button(PApplet app, String text, int x, int y) {
        super(app);

        this.text = text;
        this.x = x;
        this.y = y;

        dynamicSize = true;

        listeners = new ArrayList<>();

        // Create the font
        font = applet.createFont("Arial", 16);
    }

    public Button(PApplet app, String text, int x, int y, int height, int width) {
        super(app);

        this.text = text;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;

        dynamicSize = false;

        // Create the font
        font = applet.createFont("Arial", 16);
    }

    @Override
    public void draw(PGraphics g) {
        int stringWidth = (int) g.textWidth(text);
        int stringHeight = (int) (g.textAscent() + g.textDescent());

        if (dynamicSize) {
            width = 10 + stringWidth;
            height = 10 + stringHeight;
        }

        // Draw the background
        g.fill(255);
        g.color(50);
        g.rect(x, y, width, height);


        g.fill(0);
        g.textFont(font);

        g.text(text.toCharArray(), 0, text.length(), x + 5, y + stringHeight);
    }

    public synchronized void addClickListener(OnClickListener c) {
        listeners.add(c);
    }

    public synchronized void removeClickListener(OnClickListener c) {
        listeners.add(c);
    }

    public void clicked(MouseEvent e) {
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
    protected void componentClicked(MouseEvent e) {
        clicked(e);
    }
}
