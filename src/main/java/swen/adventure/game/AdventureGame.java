package swen.adventure.game;

import org.lwjgl.Sys;
import processing.opengl.PGraphics2D;
import swen.adventure.engine.*;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.PickerRenderer;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.components.Frame;
import swen.adventure.engine.utils.SharedLibraryLoader;
import swen.adventure.game.scenenodes.Button;
import swen.adventure.game.scenenodes.Door;
import swen.adventure.game.ui.components.Inventory;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.ui.components.ProgressBar;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.game.scenenodes.Player;

import java.util.Collections;
import java.util.Map;

public class AdventureGame implements Game {

    private GLRenderer _glRenderer;
    private PickerRenderer _pickerRenderer;
    private PGraphics2D _pGraphics;
    private TransformNode _sceneGraph;

    // Elements of the UI
    private swen.adventure.engine.ui.components.Frame _frame;

    private Player player;

    private AdventureGameKeyInput _keyInput = new AdventureGameKeyInput();
    private MouseInput _mouseInput = new MouseInput();

    private float _mouseSensitivity = 1;
    private float _viewAngleX;
    private float _viewAngleY;

    @Override
    public void setup(int width, int height) {
        _sceneGraph = new TransformNode("root", new Vector3(0.f, 0.f, 0.f), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode groundPlaneTransform = new TransformNode("groundPlaneTransform", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis((float) Math.PI / 2.f, -1, 0, 0), new Vector3(25000, 25000, 1));
        MeshNode groundPlane = new MeshNode("Plane.obj", groundPlaneTransform);
        groundPlane.setMaterialOverride(new Material(Vector3.zero, new Vector3(0.1f, 0.8f, 0.3f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 1.f));

        TransformNode keyTransform = new TransformNode("textureKeyTransform", _sceneGraph, false, new Vector3(0, 60, 40), Quaternion.makeWithAngleAndAxis(0.f, 1, 0, 0), new Vector3(20, 20, 20));
        new MeshNode("Key_B_02.obj", keyTransform);

        TransformNode yAxisTransform = new TransformNode("yAxis", _sceneGraph, false, new Vector3(0, 0, 0), new Quaternion(), new Vector3(2, 1000, 2));
        MeshNode yAxis = new MeshNode("box.obj", yAxisTransform);
        yAxis.setMaterialOverride(new Material(Vector3.zero, new Vector3(0.f, 1.f, 0.f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.01f));

        TransformNode xAxisTransform = new TransformNode("xAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(1000, 2, 2));
        MeshNode xAxis = new MeshNode("box.obj", xAxisTransform);
        xAxis.setMaterialOverride(new Material(Vector3.zero, new Vector3(0.f, 0.f, 1.f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.01f));

        TransformNode zAxisTransform = new TransformNode("zAxis", _sceneGraph, false, new Vector3(0, 0, 0), Quaternion.makeWithAngleAndAxis(0.0f, 0.f, 0.0f, 0.f), new Vector3(2, 2, 1000));
        MeshNode zAxis = new MeshNode("box.obj", zAxisTransform);
        zAxis.setMaterialOverride(new Material(Vector3.zero, new Vector3(1.f, 1.f, 0.f), new Vector3(0.5f, 0.5f, 0.5f), 0.f, 0.01f));

        TransformNode playerTransform = new TransformNode("playerTransform", _sceneGraph, true, new Vector3(0, 20, 200), new Quaternion(), new Vector3(1.f, 1.f, 1.f));
        TransformNode cameraTransform = new TransformNode("cameraTransform", playerTransform, true, new Vector3(0, 0, 0), new Quaternion(), new Vector3(1, 1, 1));
        TransformNode playerTableTransform = new TransformNode("playerTableTransform", playerTransform, true, new Vector3(0, 0, -100), new Quaternion(), new Vector3(0.05f, 0.05f, 0.05f));
        MeshNode playerMesh = new MeshNode("rocket.obj", playerTableTransform);
        new CameraNode("playerCamera", cameraTransform);
        player = new Player("player", playerTransform);
        player.collisionNode().setBoundingBox(new BoundingBox(new Vector3(-20, -20, -10), new Vector3(20, 20, 10)));

//        TransformNode tableTransform = new TransformNode("ObjBoxTransform", _sceneGraph, true, new Vector3(20f, 5.f, -5.f), new Quaternion(), new Vector3(3.f, 3.f, 3.f));
//        MeshNode table = new MeshNode("tableMesh", "Table.obj", tableTransform);
//        table.setMaterialOverride(new Material(Vector3.zero, new Vector3(0.8f, 0.3f, 0.4f), new Vector3(0.7f, 0.6f, 0.6f), 0.f, 0.2f));
//        new GameObject("tableGameObject", tableTransform);

        Light.createAmbientLight("ambientLight", _sceneGraph, new Vector3(0.3f, 0.5f, 0.4f), 1.5f);
        //Light.createDirectionalLight("directionalLight", _sceneGraph, new Vector3(0.7f, 0.3f, 0.1f), 7.f, new Vector3(0.4f, 0.2f, 0.6f));
        //Light.createPointLight("pointLight", cameraTransform, new Vector3(0.4f, 0.5f, 0.8f), 9.f, Light.LightFalloff.Quadratic);

        Light redPointLight = Light.createPointLight("redPointLight", _sceneGraph, new Vector3(1f, 0f, 0f), 15f, Light.LightFalloff.Linear);
        //redPointLight.toggleLight();

        Light greenPointLight = Light.createPointLight("greenPointLight", _sceneGraph, new Vector3(0f, 1f, 0f), 15f, Light.LightFalloff.Linear);
        //greenPointLight.toggleLight();

        Light bluePointLight = Light.createPointLight("bluePointLight", _sceneGraph, new Vector3(0f, 0f, 1f), 15f, Light.LightFalloff.Linear);
        //bluePointLight.toggleLight();


        final Door door = new Door("houseDoor", _sceneGraph);

        TransformNode redLightButtonTransform = new TransformNode("redButtonTransform", _sceneGraph, true, new Vector3(50, 0, 100), new Quaternion(), new Vector3(20, 20, 20));
        final Button redButton = new Button("redLightButton", redLightButtonTransform);
        redButton.mesh().setMaterialOverride(new Material(new Vector3(2.f, 0.f, 0.f), new Vector3(3.f, 0.f, 0.f), Vector3.zero, 0.f, 1.f));
        redButton.eventButtonPressed.addAction(redButton, (eventObject, triggeringObject, listener, data) -> {
            redPointLight.setOn(!redPointLight.isOn());
            redButton.mesh().materialOverride().ifPresent(material -> {
                material.setAmbientColour(redPointLight.isOn() ? new Vector3(5.f, 0.f, 0.f) : new Vector3(1.f, 0.f, 0.f));
            });
        });

        TransformNode blueLightButtonTransform = new TransformNode("blueButtonTransform", _sceneGraph, true, new Vector3(100, 0, 100), new Quaternion(), new Vector3(20, 20, 20));
        final Button blueButton = new Button("blueLightButton", blueLightButtonTransform);
        blueButton.mesh().setMaterialOverride(new Material(new Vector3(0.f, 0.f, 2.f), new Vector3(0.f, 0.f, 3.f), Vector3.zero, 0.f, 1.f));
        blueButton.eventButtonPressed.addAction(blueButton, (eventObject, triggeringObject, listener, data) -> {
            bluePointLight.setOn(!bluePointLight.isOn());
            blueButton.mesh().materialOverride().ifPresent(material -> {
                material.setAmbientColour(bluePointLight.isOn() ? new Vector3(0.f, 0.f, 5.f) : new Vector3(0.f, 0.f, 1.f));
            });
        });

        TransformNode greenLightButtonTransform = new TransformNode("blueButtonTransform", _sceneGraph, true, new Vector3(150, 0, 100), new Quaternion(), new Vector3(20, 20, 20));
        final Button greenButton = new Button("greenLightButton", greenLightButtonTransform);
        greenButton.mesh().setMaterialOverride(new Material(new Vector3(0.f, 2.f, 0.f), new Vector3(0.f, 3.f, 0.f), Vector3.zero, 0.f, 1.f));
        greenButton.eventButtonPressed.addAction(greenButton, (eventObject, triggeringObject, listener, data) -> {
            greenPointLight.setOn(!greenPointLight.isOn());
            greenButton.mesh().materialOverride().ifPresent(material -> {
                material.setAmbientColour(greenPointLight.isOn() ? new Vector3(0.f, 5.f, 0.f) : new Vector3(0.f, 1.f, 0.f));
            });
        });

        _glRenderer = new GLRenderer(width, height);
        _pickerRenderer = new PickerRenderer();

        _keyInput.eventMoveForwardKeyPressed.addAction(player, Player.actionPlayerMoveForward);
        _keyInput.eventMoveBackwardKeyPressed.addAction(player, Player.actionPlayerMoveBackward);
        _keyInput.eventMoveLeftKeyPressed.addAction(player, Player.actionPlayerMoveLeft);
        _keyInput.eventMoveRightKeyPressed.addAction(player, Player.actionPlayerMoveRight);

        _mouseInput.eventMouseButtonPressed.addAction(this, AdventureGame.clickAction);

        this.setupUI(width, height);
    }

    private static final Action<MouseInput, MouseInput, AdventureGame> clickAction = (eventObject, triggeringObject, listener, data) -> {
           listener._pickerRenderer.selectedNode()
                   .ifPresent(meshNode -> meshNode.eventMeshClicked.trigger(listener.player, Collections.emptyMap()));
    };

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

        player.getInventory().eventItemSelected.addAction(inventory, Inventory.actionSelectItem);

        panel.addChild(inventory);

        _frame.addChild(panel);
    }

    @Override
    public void setSize(int width, int height) {
        _glRenderer.setSize(width, height);
        _pGraphics.setSize(width, height);
    }

    @Override
    public void setSizeInPixels(int width, int height) {
        _pGraphics.setPixelDimensions(width, height);
    }

    @Override
    public void update(long deltaMillis) {
        _keyInput.handleInput();
        _mouseInput.handleInput();

        player.parent().get().setRotation(Quaternion.makeWithAngleAndAxis(_viewAngleX / 500, 0, -1, 0).multiply(Quaternion.makeWithAngleAndAxis(_viewAngleY / 500, -1, 0, 0)));

        this.render();
    }

    private void render() {
        CameraNode camera = (CameraNode) _sceneGraph.nodeWithID("playerCamera").get();
        _pickerRenderer.render(_sceneGraph, camera);
        _glRenderer.render(_sceneGraph, camera);

        _pGraphics.beginDraw();
        _frame.draw(_pGraphics);
        _pGraphics.endDraw();
    }

    /**
     * Returns the key input manager thing. This is temporary and will not be the way we actually do this.
     *
     * @return key manager thing.
     */
    @Override
    public KeyInput keyInput() {
        return _keyInput;
    }

    @Override
    public MouseInput mouseInput() {
        return _mouseInput;
    }

    @Override
    public void onMouseDeltaChange(float deltaX, float deltaY) {
        _viewAngleX = (_viewAngleX + deltaX / _mouseSensitivity);
        _viewAngleY = (_viewAngleY + deltaY / _mouseSensitivity);
    }

    public static void main(String[] args) {
        SharedLibraryLoader.load();
        GameDelegate.setGame(new AdventureGame());
    }
}
