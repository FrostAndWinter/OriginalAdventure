package swen.adventure;


import processing.core.PApplet;

public class Processing extends PApplet {

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void draw() {
        super.draw();


        background(0);

        lights();

        translate(300, 300, -100);
        color(80);
        sphere(80);
    }

    public void settings() {
        size(800, 600, P3D);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "swen.adventure.Processing"});
    }
}
