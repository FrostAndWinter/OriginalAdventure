package swen.adventure.game;

import org.lwjgl.Sys;
import org.lwjgl.opengl.GLContext;
import processing.core.PImage;
import processing.opengl.PGraphics2D;
import swen.adventure.engine.*;
import swen.adventure.engine.Event;
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
import swen.adventure.engine.ui.components.*;
import swen.adventure.engine.ui.components.Frame;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.utils.SharedLibraryLoader;
import swen.adventure.game.scenenodes.*;
import swen.adventure.game.ui.components.InventoryComponent;

import javax.imageio.ImageIO;
import java.awt.*;
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
    private Panel splashScreen;
    private ProgressBar loadingProgressBar;

    private boolean glStarted;

    private Player player;

    private AdventureGameKeyInput _keyInput = new AdventureGameKeyInput();
    private MouseInput _mouseInput = new MouseInput();

    private float _mouseSensitivity = 1.f;
    private float _viewAngleX;
    private float _viewAngleY;

    private float virtualUIWidth;
    private float virtualUIHeight;

    public AdventureGame(Client<EventBox> client) {
        _client = client;
    }

    @Override
    public void setup(int width, int height) {
        if (!Utilities.isHeadlessMode) {
            _glRenderer = new GLRenderer(width, height);
            _pickerRenderer = new PickerRenderer();
        }

        this.setupUI(width, height);
        this.render();

        new Thread(() -> {
            GLContext.createFromCurrent();

            try {
                _sceneGraph = SceneGraphParser.parseSceneGraph(new File(Utilities.pathForResource("SceneGraph", "xml")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            this.player = (Player)_sceneGraph.nodeWithID("player").get();
            this.player.setCamera((CameraNode) _sceneGraph.nodeWithID("playerCamera").get());

            try {
                List<EventConnectionParser.EventConnection> connections = EventConnectionParser.parseFile(Utilities.readLinesFromFile(Utilities.pathForResource("EventConnections", "event")));
                EventConnectionParser.setupConnections(connections, _sceneGraph);

            } catch (IOException e) {
                e.printStackTrace();
            }

            _keyInput.eventMoveForwardKeyPressed.addAction(player, Player.actionPlayerMoveForward);
            _keyInput.eventMoveBackwardKeyPressed.addAction(player, Player.actionPlayerMoveBackward);
            _keyInput.eventMoveLeftKeyPressed.addAction(player, Player.actionPlayerMoveLeft);
            _keyInput.eventMoveRightKeyPressed.addAction(player, Player.actionPlayerMoveRight);

            _keyInput.eventMoveUpKeyPressed.addAction(player, Player.actionPlayerMoveUp);
            _keyInput.eventMoveDownKeyPressed.addAction(player, Player.actionPlayerMoveDown);

            _mouseInput.eventMouseButtonPressed.addAction(this, AdventureGame.clickAction);

            splashScreen.setVisible(false);

            glStarted = true;
        }).start();

        this.render();
    }

    private static final Action<MouseInput, MouseInput, AdventureGame> clickAction = (eventObject, triggeringObject, listener, data) -> {
        listener.player.camera().ifPresent(cameraNode -> {
            listener._pickerRenderer.selectedNode(listener._sceneGraph, cameraNode)
                    .ifPresent(
                            meshNode ->
                                    meshNode.eventMeshClicked.trigger(listener.player, Collections.emptyMap()));
        });
    };

    private void setupUI(int width, int height) {

        virtualUIWidth = width;
        virtualUIHeight = height;

        _pGraphics = new PGraphics2D();
        _pGraphics.setPrimary(true);
        _pGraphics.setSize(width, height);

        // Set up the UI elements
        _frame = new Frame(0, 0, width, height);

        splashScreen = new Panel(0, 0, width, height);
        try {
            Image im = ImageIO.read(new File(Utilities.pathForResource("background", "png")));
            PImage image = new PImage(im);

            ImageView imageView = new ImageView(image, 0, 0, width, height);

            splashScreen.addChild(imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadingProgressBar = new ProgressBar(100, 100, 275, 500);
        splashScreen.addChild(loadingProgressBar);

        splashScreen.setVisible(true);

        Panel panel = new Panel(0, 0, width, height);
        panel.setColor(new Color(0, 0, 0, 0));

        ProgressBar healthBar = new ProgressBar(100, 100, 30, 30);
        panel.addChild(healthBar);

        InventoryComponent inventoryComponent = new InventoryComponent(5, 275, 500);
        inventoryComponent.setBoxSize(50);

        //player.inventory().eventItemSelected.addAction(inventoryComponent, InventoryComponent.actionSelectSlot);

        panel.addChild(inventoryComponent);

        int size = 5;
        Reticule reticule = new Reticule(width/2 - (size), height/2 - size, size);
        panel.addChild(reticule);

        _frame.addChild(panel);
        _frame.addChild(splashScreen);
    }

    @Override
    public void setSize(int width, int height) {
        if (glStarted) {
            _glRenderer.setSize(width, height);
        }

        _pGraphics.setSize(width, height);
    }

    @Override
    public void setSizeInPixels(int width, int height) {
        _pGraphics.setPixelDimensions(width, height);
    }

    @Override
    public void update(long deltaMillis) {
        if (glStarted) {
            _keyInput.handleInput();
            _mouseInput.handleInput();

            Optional<EventBox> box;
            while ((box = _client.poll()).isPresent()) {
                EventBox event = box.get();
                SceneNode source = _sceneGraph.nodeWithID(event.sourceId).get();
                SceneNode target = _sceneGraph.nodeWithID(event.targetId).get();
                Event e = target.eventWithName(event.eventName);
                e.trigger(source, event.eventData);
            }

            player.parent().get().setRotation(Quaternion.makeWithAngleAndAxis(_viewAngleX / 500, 0, -1, 0).multiply(Quaternion.makeWithAngleAndAxis(_viewAngleY / 500, -1, 0, 0)));
        }

        this.render();
    }

    private void render() {
        if (glStarted) {
            if (Utilities.isHeadlessMode) {
                return;
            }
            this.player.camera().ifPresent(cameraNode -> {
                _glRenderer.render(_sceneGraph, _sceneGraph.allLights(), cameraNode);
            });
        }

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
