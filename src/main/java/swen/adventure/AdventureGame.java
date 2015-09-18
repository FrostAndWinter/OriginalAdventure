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

        _sceneGraph = new TransformNode("root", new Vector3(0.f, 0.f, -200.f), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        PShape groundPlane = this.createShape(BOX, 500.f, 0.f, 500.f);
        groundPlane.setFill(0xFF00AA0B);
        new MeshNode("groundPlane", _sceneGraph, groundPlane);

        TransformNode boxTransform = new TransformNode("boxTransform", _sceneGraph, true, new Vector3(-80, 25.f, 0.f), Quaternion.makeWithAngleAndAxis(1.f, 0.f, 1.f, 0.f), new Vector3(0.6f, 1.f, 0.8f));
        PShape box = createShape(BOX, 50.f);
        new MeshNode("boxMesh", boxTransform, box);

        TransformNode sphereTransform = new TransformNode("sphereTransform", _sceneGraph, false, new Vector3(25, 60.f, -10.f), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode sphereOffset = new TransformNode("sphereOffset", sphereTransform, true, new Vector3(0, 0, 0), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        new MeshNode("sphereMesh", sphereOffset, createShape(SPHERE, 20.f));
    }

    @Override
    public void draw() {
        super.draw();

        TransformNode sphereOffset = (TransformNode) _sceneGraph.nodeWithID("sphereOffset").get();
        sphereOffset.setTranslation(new Vector3(0.f, 40 * sin(this.frameCount / 60.f), 0.f));

        TransformNode boxTransform = (TransformNode) _sceneGraph.nodeWithID("boxTransform").get();
        boxTransform.rotateY(0.02f);

        _renderer.render(_sceneGraph);
    }

    public void settings() {
        size(800, 600, P3D);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"swen.adventure.AdventureGame"});
    }
}
