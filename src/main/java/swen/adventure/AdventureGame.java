package swen.adventure;


import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PShapeOBJ;
import processing.opengl.PGraphics3D;
import swen.adventure.rendering.Renderer;
import swen.adventure.rendering.maths.Quaternion;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.scenegraph.MeshNode;
import swen.adventure.scenegraph.SceneNode;
import swen.adventure.scenegraph.TransformNode;

import java.io.*;

public class AdventureGame extends PApplet {

    private Renderer _renderer;
    private SceneNode _sceneGraph;

    @Override
    public void setup() {
        super.setup();
        _renderer = new Renderer((PGraphics3D)this.getGraphics());

        _sceneGraph = new TransformNode("root", new Vector3(0.f, 0.f, 0.f), Quaternion.makeWithAngleAndAxis((float)Math.PI/6.f, 1.f, 0.5f, 0.f), new Vector3(1.f, 1.f, 1.f));

        new MeshNode("box", _sceneGraph, createShape(BOX, 1.f));

        //_table = loadShape("/Users/Thomas/Desktop/Table.obj");
    }

    @Override
    public void draw() {
        super.draw();

        _renderer.render(_sceneGraph);
    }

    public void settings() {
        size(800, 600, P3D);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"swen.adventure.AdventureGame"});
    }
}
