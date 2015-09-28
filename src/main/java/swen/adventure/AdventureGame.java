package swen.adventure;

import processing.opengl.PGraphics2D;
import swen.adventure.rendering.GLRenderer;
import swen.adventure.rendering.Material;
import swen.adventure.rendering.maths.Quaternion;
import swen.adventure.rendering.maths.Vector3;
import swen.adventure.scenegraph.*;
import swen.adventure.utils.BoundingBox;
import swen.adventure.ui.color.Color;
import swen.adventure.ui.components.Frame;
import swen.adventure.ui.components.Inventory;
import swen.adventure.ui.components.Panel;
import swen.adventure.ui.components.ProgressBar;

import java.util.HashMap;
import java.util.Map;

public class AdventureGame {

    private GLRenderer _glRenderer;
    private PGraphics2D _pGraphics;
    private TransformNode _sceneGraph;

    // Elements of the UI
    private swen.adventure.ui.components.Frame _frame;

    private Player player;

    private KeyInput keyInput = new KeyInput();

    private float mouseSensitivity = 1;
    private float viewAngleX;
    private float viewAngleY;

    public void setup(int width, int height) {
        _sceneGraph = new TransformNode("root", new Vector3(0.f, 0.f, -200.f), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode groundPlaneTransform = new TransformNode("groundPlaneTransform", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis((float)Math.PI/2.f, -1, 0, 0), new Vector3(250, 250, 1));
        MeshNode groundPlane = new MeshNode("Plane.obj", groundPlaneTransform);
        groundPlane.setMaterial(new Material(Vector3.zero, new Vector3(0.1f, 0.8f, 0.3f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.8f));

        TransformNode yAxisTransform = new TransformNode("yAxis", _sceneGraph, false, new Vector3(0, 0, 0), new Quaternion(), new Vector3(2, 1000, 2));
        MeshNode yAxis = new MeshNode("box.obj", yAxisTransform);
        yAxis.setMaterial(new Material(Vector3.zero, new Vector3(0.f, 1.f, 0.f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.01f));

        TransformNode xAxisTransform = new TransformNode("xAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(1000, 2, 2));
        MeshNode xAxis = new MeshNode("box.obj", xAxisTransform);
        xAxis.setMaterial(new Material(Vector3.zero, new Vector3(0.f, 0.f, 1.f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.01f));

        TransformNode zAxisTransform = new TransformNode("zAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(2, 2, 1000));
        MeshNode zAxis = new MeshNode("box.obj", zAxisTransform);
        zAxis.setMaterial(new Material(Vector3.zero, new Vector3(1.f, 1.f, 0.f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.01f));

        TransformNode cubeTransform = new TransformNode("cubeTransform", _sceneGraph, false, new Vector3(0, 50, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(100, 100, 100));
        MeshNode cubeMesh = new MeshNode("cubeMesh", "box.obj", cubeTransform);
        cubeMesh.setMaterial(new Material(Vector3.zero, new Vector3(1.f, 1.f, 1.f), new Vector3(0.5f, 0.5f, 0.5f), 0f, 0.05f));
        new CollisionNode("cubeCollision", cubeTransform, true, new BoundingBox(Vector3.zero, new Vector3(1, 1, 1)));

        TransformNode playerTransform = new TransformNode("playerTransform", _sceneGraph, true, new Vector3(0, 20, 200), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode cameraTransform = new TransformNode("cameraTransform", playerTransform, true, new Vector3(0, 0, 0), new Quaternion(), new Vector3(1, 1, 1));
        TransformNode playerTableTransform = new TransformNode("playerTableTransform", playerTransform, true, new Vector3(0, 0, -100), new Quaternion(), new Vector3(1, 1, 1));
        MeshNode playerMesh = new MeshNode("Table.obj", playerTableTransform);
        playerMesh.setMaterial(new Material(Vector3.zero, new Vector3(1.f, 1.f, 1.f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.01f));
        new CameraNode("playerCamera", cameraTransform);
        new CollisionNode("playerCollision", playerTableTransform, true, new BoundingBox(new Vector3(0, 0, 0), new Vector3(100, 100, 100)));
        player = new Player("player", playerTransform);

        TransformNode tableTransform = new TransformNode("ObjBoxTransform", _sceneGraph, true, new Vector3(20f, 5.f, -5.f), new Quaternion(), new Vector3(3.f, 3.f, 3.f));
        MeshNode table = new MeshNode("Table.obj", tableTransform);
        table.setMaterial(new Material(Vector3.zero, new Vector3(0.8f, 0.3f, 0.4f), new Vector3(0.7f, 0.6f, 0.6f), 0.f, 0.2f));

        Light.createAmbientLight("ambientLight", _sceneGraph, new Vector3(0.3f, 0.5f, 0.4f), 3.f);
        Light.createDirectionalLight("directionalLight", _sceneGraph, new Vector3(0.7f, 0.3f, 0.1f), 7.f, new Vector3(0.4f, 0.2f, 0.6f));
        Light.createPointLight("pointLight", cameraTransform, new Vector3(0.4f, 0.5f, 0.8f), 9.f, Light.LightFalloff.Quadratic);

        _glRenderer = new GLRenderer(width, height);

        this.setupUI(width, height);
    }

    private void setupUI(int width, int height) {

        _pGraphics = new PGraphics2D();
        _pGraphics.setPrimary(true);
        _pGraphics.setSize(width, height);

        // Set up the UI elements
        _frame = new Frame(0, 0, width, height);

        Panel panel = new Panel(0, 0, width, height);
        panel.setColor(new Color(0, 0, 0, 0));

        ProgressBar healthBar = new ProgressBar(100, 100, 30, 30);
        panel.addChild(healthBar);

        Inventory inventory = new Inventory(5, 275, 500);
        inventory.setBoxSize(50);

        panel.addChild(inventory);

        _frame.addChild(panel);
    }

    public void setSize(int width, int height) {
        _glRenderer.setSize(width, height);
        _pGraphics.setSize(width, height);
    }

    public void setSizeInPixels(int width, int height) {
        _pGraphics.setPixelDimensions(width, height);
    }

    public void update(long deltaMillis) {
        ((TransformNode) _sceneGraph.nodeWithID("ObjBoxTransform").get()).rotateY(0.005f);

        this.handleMovement();

        // change player's rotation according to mouse delta
        player.parent().get().setRotation(Quaternion.makeWithAngleAndAxis(viewAngleX / 500, 0, -1, 0).multiply(Quaternion.makeWithAngleAndAxis(viewAngleY / 500, -1, 0, 0)));

        // make the cube turn red when player is near it
        CollisionNode playerCollision = ((CollisionNode) _sceneGraph.nodeWithID("playerCollision").get());
        CollisionNode cubeCollision = ((CollisionNode) _sceneGraph.nodeWithID("cubeCollision").get());
        MeshNode cubeMesh = ((MeshNode) _sceneGraph.nodeWithID("cubeMesh").get());
        if (playerCollision.boundingBox().intersectsWith(cubeCollision.boundingBox())) {
            cubeMesh.setMaterial(new Material(Vector3.zero, new Vector3(1.f, 0.f, 0.f), new Vector3(0.5f, 0.5f, 0.5f), 0f, 0.05f));
        } else {
            cubeMesh.setMaterial(new Material(Vector3.zero, new Vector3(1.f, 1.f, 1.f), new Vector3(0.5f, 0.5f, 0.5f), 0f, 0.05f));
        }

        player.parent().get().setRotation(Quaternion.makeWithAngleAndAxis(viewAngleX/500, 0, -1, 0).multiply(Quaternion.makeWithAngleAndAxis(viewAngleY / 500, -1, 0, 0)));;

        this.render();
    }

    private void render() {
        _glRenderer.render(_sceneGraph, (CameraNode) _sceneGraph.nodeWithID("playerCamera").get());

        _pGraphics.beginDraw();
        _frame.draw(_pGraphics);
        _pGraphics.endDraw();
    }

    float playerSpeed = 3.0f;

    private void handleMovement() {
        // handle the movement input from the player
        if (keyInput.isKeyPressed('w')) {
            player.move(new Vector3(0, 0, -playerSpeed));
        }
        if (keyInput.isKeyPressed('d')) {
            player.move(new Vector3(playerSpeed, 0, 0));
        }
        if (keyInput.isKeyPressed('s')) {
            player.move(new Vector3(0, 0, playerSpeed));
        }
        if (keyInput.isKeyPressed('a')) {
            player.move(new Vector3(-playerSpeed, 0, 0));
        }
        if (keyInput.isKeyPressed('q')) {
            player.move(new Vector3(0, playerSpeed, 0));
        }
        if (keyInput.isKeyPressed('e')) {
            player.move(new Vector3(0, -playerSpeed, 0));
        }
    }

    /**
     * Returns the key input manager thing. This is temporary and will not be the way we actually do this.
     *
     * @return key manager thing.
     */
    public KeyInput keyInput() {
        return keyInput;
    }

    public static class KeyInput {
        private Map<Character, Boolean> keyPressedMap = new HashMap<>();

        public void pressKey(Character key) {
            keyPressedMap.put(Character.toUpperCase(key), true);
        }

        public void releaseKey(Character key) {
            keyPressedMap.put(Character.toUpperCase(key), false);
        }

        public boolean isKeyPressed(Character c) {
            return keyPressedMap.getOrDefault(Character.toUpperCase(c), false);
        }
    }

    public void onMouseDeltaChange(float deltaX, float deltaY) {
        viewAngleX = (viewAngleX + deltaX/mouseSensitivity) % ((float)Math.PI * 2);
        viewAngleY = (viewAngleY + deltaY/mouseSensitivity) % ((float)Math.PI * 2);
    }
}
