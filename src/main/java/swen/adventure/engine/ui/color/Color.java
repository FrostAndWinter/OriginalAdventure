package swen.adventure.engine.ui.color;

/**
 * Created by danielbraithwt on 9/23/15.
 */
public class Color {
    private float r;
    private float g;
    private float b;
    private float a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getA() {
        return a;
    }
}