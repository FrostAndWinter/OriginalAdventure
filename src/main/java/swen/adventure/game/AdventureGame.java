package swen.adventure.game;

import processing.opengl.PGraphics2D;
import swen.adventure.Settings;
import swen.adventure.engine.*;
import swen.adventure.engine.datastorage.EventConnectionParser;
import swen.adventure.engine.datastorage.SceneGraphParser;
import swen.adventure.engine.network.Client;
import swen.adventure.engine.network.DumbClient;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkClient;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.rendering.PickerRenderer;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.components.Frame;
import swen.adventure.engine.ui.components.Reticule;
import swen.adventure.game.scenenodes.*;
import swen.adventure.game.ui.components.InventoryComponent;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.ui.components.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AdventureGame implements Game {

    private GLRenderer _glRenderer;
    private PickerRenderer _pickerRenderer;
    private PGraphics2D _pGraphics;
    private TransformNode _sceneGraph;
    private final Client<EventBox> _client;

    // Elements of the UI
    private swen.adventure.engine.ui.components.Frame _frame;
    private swen.adventure.game.ui.components.InventoryComponent _inventory;

    private Player player;

    private AdventureGameKeyInput _keyInput = new AdventureGameKeyInput();
    private MouseInput _mouseInput = new MouseInput();

    private float _mouseSensitivity = Settings.MouseSensitivity;
    private float _viewAngleX;
    private float _viewAngleY;

    private float virtualUIWidth;
    private float virtualUIHeight;

    private Optional<MeshNode> _meshBeingLookedAt = Optional.empty();

    public AdventureGame(Client<EventBox> client) {
        _client = client;
    }

    @Override
    public void setup(int width, int height) {

        try {
            _sceneGraph = SceneGraphParser.parseSceneGraph(new File(Utilities.pathForResource("SceneGraph", "xml")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.player = (Player)_sceneGraph.nodeWithID("player").get();
        this.player.setCamera((CameraNode)_sceneGraph.nodeWithID("playerCamera").get());

        try {
            List<EventConnectionParser.EventConnection> connections = EventConnectionParser.parseFile(Utilities.readLinesFromFile(Utilities.pathForResource("EventConnections", "event")));
            EventConnectionParser.setupConnections(connections, _sceneGraph);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!Utilities.isHeadlessMode) {
            _glRenderer = new GLRenderer(width, height);
            _pickerRenderer = new PickerRenderer();
        }

        _keyInput.eventMoveInDirection.addAction(this.player, Player.actionMoveInDirection);

        _mouseInput.eventMouseButtonPressed.addAction(this, AdventureGame.pressAction);
        _mouseInput.eventMouseButtonReleased.addAction(this, AdventureGame.releaseAction);
        _keyInput.eventHideShowInventory.addAction(this, (eventObject, triggeringObject, listener, data) -> {
            _inventory.setShowItem(!_inventory.getShowItem());
        });

        this.setupUI(width, height);
    }

    private static final Action<MouseInput, MouseInput, AdventureGame> pressAction = (eventObject, triggeringObject, listener, data) -> {
        listener._meshBeingLookedAt.ifPresent(
                            meshNode ->
                                    meshNode.eventMeshPressed.trigger(listener.player, Collections.emptyMap())
        );


    };

    private static final Action<MouseInput, MouseInput, AdventureGame> releaseAction = (eventObject, triggeringObject, listener, data) -> {
        listener._meshBeingLookedAt.ifPresent(
                meshNode ->
                        meshNode.eventMeshReleased.trigger(listener.player, Collections.emptyMap())
        );


    };

    private void setupUI(int width, int height) {

        virtualUIWidth = width;
        virtualUIHeight = height;

        _pGraphics = new PGraphics2D();
        _pGraphics.setPrimary(true);
        _pGraphics.setSize(width, height);

        // Set up the UI elements
        _frame = new Frame(0, 0, width, height);

        Panel panel = new Panel(0, 0, width, height);
        panel.setColor(new Color(0, 0, 0, 0));

        ProgressBar healthBar = new ProgressBar(100, 100, 30, 30);
        panel.addChild(healthBar);

        _inventory = new InventoryComponent(this.player.inventory(), 275, 500);
        _inventory.setBoxSize(50);

        player.inventory().eventItemSelected.addAction(_inventory, InventoryComponent.actionSelectSlot);

        panel.addChild(_inventory);

        int size = 5;
        Reticule reticule = new Reticule(width/2, height/2, size);
        panel.addChild(reticule);

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
        GameDelegate.pollInput();

        Optional<EventBox> box;
        while ((box = _client.poll()).isPresent()) {
            EventBox event = box.get();
            SceneNode source = _sceneGraph.nodeWithID(event.sourceId).get();
            SceneNode target = _sceneGraph.nodeWithID(event.targetId).get();
            Event e = target.eventWithName(event.eventName);
            e.trigger(source, event.eventData);
        }

        player.parent().get().setRotation(Quaternion.makeWithAngleAndAxis(_viewAngleX / 500, 0, -1, 0).multiply(Quaternion.makeWithAngleAndAxis(_viewAngleY / 500, -1, 0, 0)));

        this.render();
    }

    private void render() {
        if (Utilities.isHeadlessMode) {
            return;
        }

        this.player.camera().ifPresent(cameraNode -> {
            List<MeshNode> meshNodesSortedByZ = DepthSorter.sortedMeshNodesByZ(_sceneGraph, cameraNode.worldToNodeSpaceTransform());

            Optional<MeshNode> previousMeshBeingLookedAt = _meshBeingLookedAt;
            _meshBeingLookedAt = _pickerRenderer.selectedNode(meshNodesSortedByZ, cameraNode.worldToNodeSpaceTransform());

            // if the mesh being looked at changes, trigger appropriate events on the meshes
            if (!previousMeshBeingLookedAt.equals(_meshBeingLookedAt)) {
                _meshBeingLookedAt.ifPresent(meshNode -> meshNode.eventMeshLookedAt.trigger(this.player, Collections.emptyMap()));
                previousMeshBeingLookedAt.ifPresent(meshNode ->  meshNode.eventMeshLookedAwayFrom.trigger(this.player, Collections.emptyMap()));
            }

            _glRenderer.render(meshNodesSortedByZ, _sceneGraph.allNodesOfType(Light.class), cameraNode.worldToNodeSpaceTransform(), cameraNode.fieldOfView(), cameraNode.hdrMaxIntensity());
        });


        float scaleX = _pGraphics.width / virtualUIWidth;
        float scaleY = _pGraphics.height / virtualUIHeight;
        float scale = Math.min(scaleX, scaleY);

        float dw = (_pGraphics.width - (scale * virtualUIWidth))/2;
        float dh = (_pGraphics.height - (scale * virtualUIHeight))/2;

        _pGraphics.beginDraw();
        _pGraphics.noStroke();
        _pGraphics.translate(dw, dh);
        _frame.draw(_pGraphics, scale, scale);
        _pGraphics.endDraw();

        _inventory.drawItems(_glRenderer, scale, scale, dw, dh, _pGraphics.width, _pGraphics.height);
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
        // Start with networking using CLI arguments <player id> <host> <port>
        Client<EventBox> client;
        if (args.length == 3) {
            client = new NetworkClient("player" + Math.random());
            try {
                client.connect(args[1], Integer.parseInt(args[2]));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            client = new DumbClient();
        }

        GameDelegate.setGame(new AdventureGame(client));
    }
}
