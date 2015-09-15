package swen.adventure;


import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PShapeOBJ;

import java.io.*;

public class Processing extends PApplet {

    private PShape _table;
    float xRotation = 0.f;
    float yRotation = 0.f;
    float zRotation = 0.f;

    @Override
    public void setup() {
        super.setup();
        _table = loadShape("/Users/Thomas/Desktop/Table.obj");
    }

    @Override
    public void draw() {
        super.draw();

        background(0);

        lights();

        translate(400, 400, -300);
        color(80, 200, 300);

        rotateX(xRotation);
        rotateY(yRotation);
        rotateZ(zRotation);
        //box(100);

        scale(5, 5, 5);
        shape(_table);

        xRotation += 0.05;
        yRotation += 0.1;
        zRotation += 0.15;
    }

    public void settings() {
        size(800, 600, P3D);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"--present", "swen.adventure.Processing"});
    }
}
