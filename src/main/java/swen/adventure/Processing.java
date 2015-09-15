package swen.adventure;


import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PShapeOBJ;

import java.io.*;

public class Processing extends PApplet {

    @Override
    public void setup() {
        super.setup();

    }

    @Override
    public void draw() {
        super.draw();

        background(0);

        directionalLight(100f, 200f, 300f, -0.6f, -0.3f, -2.0f);

        translate(400, 400, -300);
        color(80, 200, 300);

        rotateX(1.1f);
        rotateZ((float)Math.PI/4);
        box(100);


    }

    public void settings() {
        size(800, 600, P3D);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "swen.adventure.Processing"});
    }
}
