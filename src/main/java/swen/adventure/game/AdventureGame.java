package swen.adventure.game;

import processing.opengl.PGraphics2D;
import swen.adventure.engine.Event;
import swen.adventure.engine.KeyInput;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.rendering.Material;
import swen.adventure.engine.rendering.PickerRenderer;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.components.Frame;
import swen.adventure.game.ui.components.Inventory;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.ui.components.ProgressBar;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.game.scenenodes.Player;

import java.util.Optional;

public class AdventureGame implements swen.adventure.engine.GameInterface {

    private GLRenderer _glRenderer;
    private PickerRenderer _pickerRenderer;
    private PGraphics2D _pGraphics;
    private TransformNode _sceneGraph;

    // Elements of the UI
    private swen.adventure.engine.ui.components.Frame _frame;

    private Player player;

    private KeyInput keyInput = new KeyInput();

    private float mouseSensitivity = 1;
    private float viewAngleX;
    private float viewAngleY;

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

        TransformNode tableTransform = new TransformNode("ObjBoxTransform", _sceneGraph, true, new Vector3(20f, 5.f, -5.f), new Quaternion(), new Vector3(3.f, 3.f, 3.f));
        MeshNode table = new MeshNode("tableMesh", "Table.obj", tableTransform);
        table.setMaterialOverride(new Material(Vector3.zero, new Vector3(0.8f, 0.3f, 0.4f), new Vector3(0.7f, 0.6f, 0.6f), 0.f, 0.2f));
        new GameObject("tableGameObject", tableTransform);

        Light.createAmbientLight("ambientLight", _sceneGraph, new Vector3(0.3f, 0.5f, 0.4f), 3.f);
        Light.createDirectionalLight("directionalLight", _sceneGraph, new Vector3(0.7f, 0.3f, 0.1f), 7.f, new Vector3(0.4f, 0.2f, 0.6f));
        Light.createPointLight("pointLight", cameraTransform, new Vector3(0.4f, 0.5f, 0.8f), 9.f, Light.LightFalloff.Quadratic);

        _glRenderer = new GLRenderer(width, height);
        _pickerRenderer = new PickerRenderer();

        keyInput.eventMoveForwardKeyPressed.addAction(player, Player.actionPlayerMoveForward);
        keyInput.eventMoveBackwardKeyPressed.addAction(player, Player.actionPlayerMoveBackward);
        keyInput.eventMoveLeftKeyPressed.addAction(player, Player.actionPlayerMoveLeft);
        keyInput.eventMoveRightKeyPressed.addAction(player, Player.actionPlayerMoveRight);

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
        keyInput.handleInput();

        player.parent().get().setRotation(Quaternion.makeWithAngleAndAxis(viewAngleX / 500, 0, -1, 0).multiply(Quaternion.makeWithAngleAndAxis(viewAngleY / 500, -1, 0, 0)));
        ;

        this.render();
    }

    private void render() {
        CameraNode camera = (CameraNode) _sceneGraph.nodeWithID("playerCamera").get();
        _pickerRenderer.render(_sceneGraph, camera);
        _glRenderer.render(_sceneGraph, camera);

        lookingAt().map(node -> node.parent().map(transformNode -> {
            transformNode.translateBy(new Vector3(1.f, 0, 0.f));
            return transformNode;
        }));

                _pGraphics.beginDraw();
        _frame.draw(_pGraphics);
        _pGraphics.endDraw();
    }

    private Optional<SceneNode> lookingAt() {
        return _pickerRenderer
                .selectedNode()
                .flatMap(node -> node.siblings().stream()
                        .filter(sibling -> sibling instanceof GameObject)
                        .findFirst());
    }

    /**
     * Returns the key input manager thing. This is temporary and will not be the way we actually do this.
     *
     * @return key manager thing.
     */
    @Override
    public KeyInput keyInput() {
        return keyInput;
    }

    @Override
    public void onMouseDeltaChange(float deltaX, float deltaY) {
        viewAngleX = (viewAngleX + deltaX / mouseSensitivity);
        viewAngleY = (viewAngleY + deltaY / mouseSensitivity);
    }
}
