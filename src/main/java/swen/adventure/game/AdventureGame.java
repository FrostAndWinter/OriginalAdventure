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
import java.util.*;

public class AdventureGame implements Game {

    private GLRenderer _glRenderer;
    private PickerRenderer _pickerRenderer;
    private PGraphics2D _pGraphics;
    private TransformNode _sceneGraph;
    private final Client<EventBox> _client;

    // Elements of the UI
    private swen.adventure.engine.ui.components.Frame _frame;
    private swen.adventure.game.ui.components.InventoryComponent _inventory;

    private Player _player;

    private AdventureGameKeyInput _keyInput = new AdventureGameKeyInput();
    private MouseInput _mouseInput = new MouseInput();

    private float _mouseSensitivity = Settings.MouseSensitivity;
    private float _viewAngleX;
    private float _viewAngleY;

    private float virtualUIWidth;
    private float virtualUIHeight;

    private Optional<MeshNode> _meshBeingLookedAt = Optional.empty();

    private Map<Interaction.InteractionType, Interaction> _interactionsForStep = new HashMap<>();

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

        this._player = (Player)_sceneGraph.nodeWithID("player").get();
        this._player.setCamera((CameraNode) _sceneGraph.nodeWithID("playerCamera").get());

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

        _keyInput.eventMoveInDirection.addAction(this._player, Player.actionMoveInDirection);

        _mouseInput.eventMouseButtonPressed.addAction(this, AdventureGame.primaryActionFired);

        _keyInput.eventPrimaryAction.addAction(this, AdventureGame.primaryActionFired);
        _keyInput.eventSecondaryAction.addAction(this, AdventureGame.secondaryActionFired);

        _keyInput.eventHideShowInventory.addAction(this, (eventObject, triggeringObject, listener, data) -> { //FIXME this should not be here.
            _inventory.setShowItem(!_inventory.getShowItem());
        });

        // get the possible interactions a player can make this step
        Event.EventSet<AdventureGameObject, Player> interactionEvents = (Event.EventSet<AdventureGameObject, Player>) Event.eventSetForName("eventShouldProvideInteraction");
        interactionEvents.addAction(this, (gameObject, player, adventureGame, data) -> {
            Interaction interaction = (Interaction) data.get(EventDataKeys.Interaction);
            _interactionsForStep.put(interaction.interactionType, interaction);
        });

        this.setupUI(width, height);
    }

    private static final Action<Input, Input, AdventureGame> primaryActionFired = (eventObject, triggeringObject, adventureGame, data) -> {
        adventureGame.performInteractions(Interaction.ActionType.Primary);
    };

    private static final Action<Input, Input, AdventureGame> secondaryActionFired = (eventObject, triggeringObject, adventureGame, data) -> {
        adventureGame.performInteractions(Interaction.ActionType.Secondary);
    };

    private void performInteractions(Interaction.ActionType actionType) {
        List<Interaction.InteractionType> interactionTypes = Interaction.InteractionType.typesForActionType(actionType);

        interactionTypes.stream()
                .map(_interactionsForStep::get)
                .filter(interaction -> interaction != null)
                .forEach(interaction -> interaction.performInteractionWithPlayer(_player));
    }

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

        _inventory = new InventoryComponent(_player.inventory(), 275, 500);
        _inventory.setBoxSize(50);

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

        _interactionsForStep.clear();

        Optional<EventBox> box;
        while ((box = _client.poll()).isPresent()) {
            EventBox event = box.get();
            SceneNode source = _sceneGraph.nodeWithID(event.sourceId).get();
            SceneNode target = _sceneGraph.nodeWithID(event.targetId).get();
            Event e = target.eventWithName(event.eventName);
            e.trigger(source, event.eventData);
        }

        //Set where the _player is looking.
        _player.parent().get().setRotation(Quaternion.makeWithAngleAndAxis(_viewAngleX / 500, 0, -1, 0).multiply(Quaternion.makeWithAngleAndAxis(_viewAngleY / 500, -1, 0, 0)));

        this.render();
    }

    private void render() {
        if (Utilities.isHeadlessMode) {
            return;
        }

        this._player.camera().ifPresent(cameraNode -> {
            List<MeshNode> meshNodesSortedByZ = DepthSorter.sortedMeshNodesByZ(_sceneGraph, cameraNode.worldToNodeSpaceTransform());

            _meshBeingLookedAt = _pickerRenderer.selectedNode(meshNodesSortedByZ, cameraNode.worldToNodeSpaceTransform());
            _meshBeingLookedAt.ifPresent(meshNode -> meshNode.eventMeshLookedAt.trigger(this._player, Collections.singletonMap(EventDataKeys.Mesh, meshNode)));

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
        // Start with networking using CLI arguments <_player id> <host> <port>
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
