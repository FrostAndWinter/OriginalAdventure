package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public abstract class Componenet {
    protected PApplet applet;

    public Componenet(PApplet a) {
        applet = a;
    }

    public abstract void draw(PGraphics g);
}
