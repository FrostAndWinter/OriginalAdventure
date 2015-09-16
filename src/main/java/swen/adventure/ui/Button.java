package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public class Button extends Componenet {
    protected String text;
    protected int x;
    protected int y;
    protected int height;
    protected int width;

    private boolean dynamicSize;

    private PFont font;

    public Button(PApplet app, String text, int x, int y) {
        super(app);

        this.text = text;
        this.x = x;
        this.y = y;

        dynamicSize = true;

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
        g.beginDraw();

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

        g.endDraw();
    }
}
