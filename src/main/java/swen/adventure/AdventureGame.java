package swen.adventure;
import swen.adventure.rendering.GLRenderer;
import swen.adventure.rendering.maths.Quaternion;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.rendering.maths.Vector4;
import swen.adventure.scenegraph.*;

import java.util.HashMap;
import java.util.Map;

public class AdventureGame {

    private GLRenderer _glRenderer;
    private TransformNode _sceneGraph;
    private Player player;

    private KeyInput keyInput = new KeyInput();

    public void setup() {

        _sceneGraph = new TransformNode("root", new Vector3(0.f, 0.f, -200.f), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode groundPlaneTransform = new TransformNode("groundPlaneTransform", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis((float)Math.PI/2.f, -1, 0, 0), new Vector3(250, 250, 1));
        MeshNode groundPlane = new MeshNode("Plane.obj", groundPlaneTransform);
        groundPlane.setColour(new Vector4(0.1f, 0.8f, 0.3f, 1.f));

        TransformNode yAxisTransform = new TransformNode("yAxis", _sceneGraph, false, new Vector3(0, 0, 0), new Quaternion(), new Vector3(2, 1000, 2));
        MeshNode yAxis = new MeshNode("box.obj", yAxisTransform);
        yAxis.setColour(new Vector4(0.f, 1.f, 0.f, 1.f));

        TransformNode xAxisTransform = new TransformNode("xAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(1000, 2, 2));
        MeshNode xAxis = new MeshNode("box.obj", xAxisTransform);
        xAxis.setColour(new Vector4(0.f, 0.f, 1.f, 1.f));

        TransformNode zAxisTransform = new TransformNode("zAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(2, 2, 1000));
        MeshNode zAxis = new MeshNode("box.obj", zAxisTransform);
        zAxis.setColour(new Vector4(1.f, 1.f, 0.f, 1.f));

        TransformNode playerTransform = new TransformNode("playerTransform", _sceneGraph, true, new Vector3(0, 20, 200), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode cameraTransform = new TransformNode("cameraTransform", playerTransform, true, new Vector3(0, 0, 0), new Quaternion(), new Vector3(1, 1, 1));
        new CameraNode("playerCamera", cameraTransform);
        player = new Player("player", playerTransform);

        TransformNode tableTransform = new TransformNode("ObjBoxTransform", _sceneGraph, true, new Vector3(20f, 5.f, -5.f), new Quaternion(), new Vector3(3.f, 3.f, 3.f));
        new MeshNode("Table.obj", tableTransform);

        Light.createAmbientLight("ambientLight", _sceneGraph, new Vector3(0.3f, 0.5f, 0.4f), 3.f);
        Light.createDirectionalLight("directionalLight", _sceneGraph, new Vector3(0.7f, 0.7f, 0.7f), 7.f, new Vector3(0.4f, 0.2f, 0.6f));
        Light.createPointLight("pointLight", cameraTransform, new Vector3(1.f, 0.9f, 0.8f), 9.f, Light.LightFalloff.Quadratic);

        _glRenderer = new GLRenderer(800, 600);
    }

    boolean hasBeenCentred = false;

    public void update(long deltaMillis) {

        ((TransformNode)_sceneGraph.nodeWithID("ObjBoxTransform").get()).rotateY(0.005f);

//        float centreX = width/2;
//        float centreY = height/2;
//
//        float yOffset = mouseY - centreY;
//        float xOffset = mouseX - centreX;
//
//        // wait until the mouse has been centred by robot at least once
//        // otherwise you start looking off in a direction offset by the window size
//        if (yOffset == 0 && xOffset == 0) {
//            hasBeenCentred = true;
//        }
//
//        if (hasBeenCentred) {
//            TransformNode playerTransform = player.parent().get();
//
//            playerTransform.rotateX(-yOffset/(width));
//            playerTransform.rotateY(-xOffset/(height));
//        }

        // handle the movement input from the player
        if (keyInput.isKeyPressed('w')) {
            player.move(new Vector3(0, 0, -20));
        }
        if (keyInput.isKeyPressed('d')) {
            player.move(new Vector3(20, 0, 0));
        }
        if (keyInput.isKeyPressed('s')) {
            player.move(new Vector3(0, 0, 20));
        }
        if (keyInput.isKeyPressed('a')) {
            player.move(new Vector3(-20, 0, 0));
        }

        _glRenderer.render(_sceneGraph, (CameraNode) _sceneGraph.nodeWithID("playerCamera").get());

    }

    private static class KeyInput {
        private Map<Character, Boolean> keyPressedMap = new HashMap<>();

        public void pressKey(Character key) {
            keyPressedMap.put(key, true);
        }

        public void releaseKey(Character key) {
            keyPressedMap.put(key, false);
        }

        public boolean isKeyPressed(Character c) {
            return keyPressedMap.getOrDefault(c, false);
        }

    }

//    @Override
//    public void mouseMoved(MouseEvent event) {
//        super.mouseMoved(event);
//    }
//
//    @Override
//    public void keyPressed(processing.event.KeyEvent event) {
//        super.keyPressed(event);
//        keyInput.pressKey(event.getKey());
//    }
//
//    @Override
//    public void keyReleased(processing.event.KeyEvent event) {
//        super.keyReleased(event);
//        keyInput.releaseKey(event.getKey());
//    }
}
