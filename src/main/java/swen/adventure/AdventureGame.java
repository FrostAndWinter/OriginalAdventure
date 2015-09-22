package swen.adventure;


import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PGraphics3D;
import processing.opengl.PSurfaceJOGL;
import swen.adventure.rendering.GLRenderer;
import swen.adventure.rendering.ProcessingRenderer;
import swen.adventure.rendering.maths.Quaternion;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.ProcessingMesh;
import swen.adventure.scenegraph.CameraNode;
import swen.adventure.scenegraph.Player;
import swen.adventure.scenegraph.SceneNode;
import swen.adventure.scenegraph.TransformNode;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;

public class AdventureGame extends PApplet {

    private ProcessingRenderer _processingRenderer;
    private GLRenderer _glRenderer;
    private TransformNode _sceneGraph;
    private Player player;

    private Robot robot;

    @Override
    public void setup() {
        super.setup();

        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        noCursor();

        _sceneGraph = new TransformNode("root", new Vector3(0.f, 0.f, -200.f), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        PShape groundPlane = createShape(BOX, 500, 0, 500);
        groundPlane.setFill(0xFF00AA0B);
        new ProcessingMesh("groundPlane", _sceneGraph, groundPlane);

        TransformNode yAxisTransform = new TransformNode("yAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(1, 1, 1));
        PShape yAxisMesh = createShape(BOX, 2, 1000, 2);
        yAxisMesh.setFill(0xFF00FF00);
        new ProcessingMesh("yAxisMesh", yAxisTransform, yAxisMesh);

        TransformNode xAxisTransform = new TransformNode("xAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(1, 1, 1));
        PShape xAxisMesh = createShape(BOX, 1000, 2, 2);
        xAxisMesh.setFill(0xFF0000FF);
        new ProcessingMesh("xAxisMesh", xAxisTransform, xAxisMesh);

        TransformNode zAxisTransform = new TransformNode("zAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(1, 1, 1));
        PShape zAxisMesh = createShape(BOX, 2, 2, 1000);
        zAxisMesh.setFill(0xFFFF0000);
        new ProcessingMesh("zAxisMesh", zAxisTransform, zAxisMesh);


        TransformNode boxTransform = new TransformNode("boxTransform", _sceneGraph, true, new Vector3(-80, 25.f, 0.f), Quaternion.makeWithAngleAndAxis(1.f, 0.f, 1.f, 0.f), new Vector3(0.6f, 1.f, 0.8f));
        PShape box = createShape(BOX, 50.f);
        new ProcessingMesh("boxMesh", boxTransform, box);

        TransformNode sphereTransform = new TransformNode("sphereTransform", _sceneGraph, false, new Vector3(25, 60.f, -10.f), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode sphereOffset = new TransformNode("sphereOffset", sphereTransform, true, new Vector3(0, 0, 0), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        new ProcessingMesh("sphereMesh", sphereOffset, createShape(SPHERE, 20.f));

        TransformNode playerTransform = new TransformNode("playerTransform", _sceneGraph, true, new Vector3(0, 20, 0), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode cameraTransform = new TransformNode("cameraTransform", playerTransform, true, new Vector3(0, 0, 0), new Quaternion(), new Vector3(1, 1, 1));
        new CameraNode("playerCamera", cameraTransform);
        player = new Player("player", playerTransform);

        beginPGL();
        _processingRenderer = new ProcessingRenderer((PGraphics3D) this.getGraphics());
        _glRenderer = new GLRenderer((PGraphics3D) this.getGraphics());

        endPGL();
    }

    @Override
    public void draw() {
        super.draw();

        //robot.mouseMove(this.displayWidth / 2, this.displayHeight / 2);

        float xOffset = mouseX - width/2;
        float xAngle = -xOffset/(width);
        float yOffset = mouseY - height/2;
        float yAngle = -yOffset/(height);


        if (keyPressed) {
           switch (key) {
               case 'w':
                   player.move(new Vector3(0, 0, -1));
                   break;
               case 'd':
                   player.move(new Vector3(1, 0, 0));
                   break;
               case 's':
                   player.move(new Vector3(0, 0, 1));
                   break;
               case 'a':
                   player.move(new Vector3(-1, 0, 0));
                   break;
           }
        }
        ((TransformNode)player.parent().get()).setRotation(Quaternion.makeWithAngleAndAxis(xAngle, 0, 1, 0).multiply(Quaternion.makeWithAngleAndAxis(yAngle, 1, 0, 0)));

        TransformNode sphereOffset = (TransformNode) _sceneGraph.nodeWithID("sphereOffset").get();
        sphereOffset.setTranslation(new Vector3(0.f, 40 * sin(this.frameCount / 60.f), 0.f));

        TransformNode boxTransform = (TransformNode) _sceneGraph.nodeWithID("boxTransform").get();
        boxTransform.rotateY(0.02f);

        _glRenderer.render(_sceneGraph, (CameraNode) _sceneGraph.nodeWithID("playerCamera").get());


    }

    public void settings() {
        size(800, 600, P3D);
    }

    public static void main(String args[]) {
        PApplet.main(new String[]{"swen.adventure.AdventureGame"});
    }
}
