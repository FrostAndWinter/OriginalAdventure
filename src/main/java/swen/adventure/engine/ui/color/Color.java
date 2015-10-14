/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.ui.color;

/**
 * Created by danielbraithwt on 9/23/15.
 */
public class Color {
    private float r;
    private float g;
    private float b;
    private float a;

    /**
     * Stores information about a color
     * @param r red component of the color
     * @param g green component of the color
     * @param b blue component of the color
     * @param a alpha component of the color
     */
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