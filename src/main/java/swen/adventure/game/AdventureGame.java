package swen.adventure.game;

import processing.opengl.PGraphics2D;
import swen.adventure.Settings;
import swen.adventure.engine.*;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.datastorage.EventConnectionParser;
import swen.adventure.engine.datastorage.ParserManager;
import swen.adventure.engine.datastorage.SceneGraphParser;
import swen.adventure.engine.network.Client;
import swen.adventure.engine.network.DumbClient;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkClient;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.rendering.PickerRenderer;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.game.input.AdventureGameKeyInput;
import swen.adventure.game.input.AdventureGameMouseInput;
import swen.adventure.game.scenenodes.*;
import swen.adventure.game.ui.components.InventoryComponent;
import swen.adventure.game.ui.components.UI;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AdventureGame implements Game {

    private GLRenderer _glRenderer;
    private PickerRenderer _pickerRenderer;
    private PGraphics2D _pGraphics;
    private TransformNode _sceneGraph;
    private final Client<EventBox> _client;

    private UI ui;

    private Player _player;

    private AdventureGameKeyInput _keyInput = new AdventureGameKeyInput();
    private AdventureGameMouseInput _mouseInput = new AdventureGameMouseInput();

    private float _mouseSensitivity = Settings.MouseSensitivity;
    private float _viewAngleX;
    private float _viewAngleY;

    private float virtualUIWidth;
    private float virtualUIHeight;

    private Optional<MeshNode> _meshBeingLookedAt = Optional.empty();

    private EnumMap<Interaction.InteractionType, Interaction> _possibleInteractionsForStep = new EnumMap<>(Interaction.InteractionType.class);
    private EnumMap<Interaction.ActionType, Interaction> _interactionInProgressForActionType = new EnumMap<>(Interaction.ActionType.class);

    public AdventureGame(Client<EventBox> client) {
        _client = client;
    }

    @Override
    public void setup(int width, int height) {
        if (!Utilities.isHeadlessMode) {
            _glRenderer = new GLRenderer(width, height);
            _pickerRenderer = new PickerRenderer();
        }

        virtualUIWidth = width;
        virtualUIHeight = height;

        // Wait for the SNAPSHOT event
        Optional<EventBox> box;
        while (!(box = _client.poll()).isPresent()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        EventBox event = box.get();
        setupSceneGraph(SceneGraphParser.parseSceneGraph(event.eventData.get("scenegraph").toString()), event.targetId);
    }

    private void setupSceneGraph(TransformNode sceneGraph, String playerId) {
        _sceneGraph = sceneGraph;


        SpawnNode spawn = (SpawnNode)_sceneGraph.nodeWithID(SpawnNode.ID).get();
        spawn.spawnPlayerWithId(playerId);

        _player = (Player)_sceneGraph.nodeWithID(playerId).get();
        TransformNode cameraTransform = new TransformNode(playerId + "CameraTranslation",
                _player.parent().get(), false, new Vector3(0, 40, 0), new Quaternion(), Vector3.one);
        _player.setCamera(new CameraNode(playerId + "Camera", cameraTransform));
        BoundingBox boundingBox = new BoundingBox(new Vector3(-30, -60, -10) , new Vector3(30, 60, 10));
        String colliderID = playerId + "Collider";
        CollisionNode collider = (CollisionNode)spawn.nodeWithID(colliderID).orElseGet(() -> new CollisionNode(colliderID, spawn.parent().get(), boundingBox));
        collider.setParent(spawn.parent().get());

        _player.setCollisionNode(collider);


        _keyInput.eventMoveInDirection.addAction(_player, Player.actionMoveInDirection);

        try {
            List<EventConnectionParser.EventConnection> connections = EventConnectionParser.parseFile(Utilities.readLinesFromFile(Utilities.pathForResource("EventConnections", "event")));
            EventConnectionParser.setupConnections(connections, _sceneGraph);
            if (false) {
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        _keyInput.eventMoveInDirection.addAction(this._player, Player.actionMoveInDirection);

        _mouseInput.eventMousePrimaryAction.addAction(this, AdventureGame.primaryActionFired);
        _mouseInput.eventMousePrimaryActionEnded.addAction(this, AdventureGame.primaryActionEnded);
        _mouseInput.eventMouseSecondaryAction.addAction(this, AdventureGame.secondaryActionFired);
        _mouseInput.eventMouseSecondaryActionEnded.addAction(this, AdventureGame.secondaryActionEnded);

        _keyInput.eventPrimaryAction.addAction(this, AdventureGame.primaryActionFired);
        _keyInput.eventPrimaryActionEnded.addAction(this, AdventureGame.primaryActionEnded);
        _keyInput.eventSecondaryAction.addAction(this, AdventureGame.secondaryActionFired);
        _keyInput.eventSecondaryActionEnded.addAction(this, AdventureGame.secondaryActionEnded);

        _keyInput.eventHideShowInventory.addAction(ui.getInventory(), InventoryComponent.actionToggleZoomItem);

        _keyInput.eventHideShowControlls.addAction(ui, UI.actionToggleControlls);

        // get the possible interactions a player can make this step
        Event.EventSet<AdventureGameObject, Player> interactionEvents = (Event.EventSet<AdventureGameObject, Player>) Event.eventSetForName("eventShouldProvideInteraction");
        interactionEvents.addAction(this, (gameObject, player, adventureGame, data) -> {
            Interaction interaction = (Interaction) data.get(EventDataKeys.Interaction);
            _possibleInteractionsForStep.put(interaction.interactionType, interaction);
        });

        this.setupUI((int)virtualUIWidth, (int)virtualUIHeight);
    }

    private static final Action<Input, Input, AdventureGame> primaryActionFired = (eventObject, triggeringObject, adventureGame, data) -> {
        adventureGame.performInteractions(Interaction.ActionType.Primary);
    };

    private static final Action<Input, Input, AdventureGame> secondaryActionFired = (eventObject, triggeringObject, adventureGame, data) -> {
        adventureGame.performInteractions(Interaction.ActionType.Secondary);
    };

    private static final Action<Input, Input, AdventureGame> primaryActionEnded = (eventObject, triggeringObject, adventureGame, data) -> {
        adventureGame.endInteractions(Interaction.ActionType.Primary);
    };

    private static final Action<Input, Input, AdventureGame> secondaryActionEnded = (eventObject, triggeringObject, adventureGame, data) -> {
        adventureGame.endInteractions(Interaction.ActionType.Secondary);
    };

    /**
     * Performs all interactions for the specified action type.
     * @param actionType The action type to perform the interactions for.
     */
    private void performInteractions(Interaction.ActionType actionType) {
        this.endInteractions(actionType);
        List<Interaction.InteractionType> interactionTypes = Interaction.InteractionType.typesForActionType(actionType);

        interactionTypes.stream()
                .map(_possibleInteractionsForStep::get)
                .filter(interaction -> interaction != null)
                .forEach(interaction -> {
                    interaction.performInteractionWithPlayer(_player);
                    _interactionInProgressForActionType.put(actionType, interaction);
                });
    }

    private void endInteractions(Interaction.ActionType actionType) {
        Interaction interaction = _interactionInProgressForActionType.get(actionType);
        if (interaction != null) {
            interaction.interactionEndedByPlayer(_player);
        }
        _interactionInProgressForActionType.put(actionType, null);
    }

    private void setupUI(int width, int height) {
        virtualUIWidth = width;
        virtualUIHeight = height;

        _pGraphics = new PGraphics2D();
        _pGraphics.setPrimary(true);
        _pGraphics.setSize(width, height);

        ui = new UI(width, height, _player);
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
        _possibleInteractionsForStep.clear();

        Optional<EventBox> box;
        while ((box = _client.poll()).isPresent()) {
            EventBox event = box.get();
            SceneNode source = _sceneGraph.nodeWithID(event.sourceId).get();

            if (event.eventName.equals("playerConnected")) {
                SpawnNode spawn = (SpawnNode)source;
                spawn.spawnPlayerWithId(event.targetId);
                continue;
            }

            SceneNode target = _sceneGraph.nodeWithID(event.targetId).get();
            Event e = target.eventWithName(event.eventName);
            e.trigger(source, event.eventData);
        }

        _meshBeingLookedAt.ifPresent(meshNode -> meshNode.eventMeshLookedAt.trigger(this._player, Collections.singletonMap(EventDataKeys.Mesh, meshNode)));

        GameDelegate.pollInput();

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

            _glRenderer.render(meshNodesSortedByZ, _sceneGraph.allNodesOfType(Light.class), cameraNode.worldToNodeSpaceTransform(), cameraNode.fieldOfView(), cameraNode.hdrMaxIntensity());
        });

        ArrayList<String> tips = new ArrayList<>();
        for (Interaction interaction : _possibleInteractionsForStep.values()) {
            Interaction.ActionType actionType = interaction.interactionType.actionType;
            Character character = null;
            switch (actionType) {
                case Primary:
                    character = _keyInput.characterForEvent(_keyInput.eventPrimaryAction);break;
                case Secondary:
                    character = _keyInput.characterForEvent(_keyInput.eventSecondaryAction);
                    break;
            }
            String interactionString = interaction.interactionMessage(_player, character);
            if (interactionString != null) {
                tips.add(interactionString);
            }
        }

        ui.setTooltip(tips);

        ui.drawUI(_pGraphics, _glRenderer);
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
            client = new NetworkClient(args[0] + Math.random());
            try {
                client.connect(args[1], Integer.parseInt(args[2]));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            DumbClient dumbClient = new DumbClient();
            Map<String, Object> data = new HashMap<>();
            try {
                data.put("scenegraph", new String(Files.readAllBytes(new File(Utilities.pathForResource("SceneGraph", "xml")).toPath())));
                dumbClient.add(new EventBox("snapshot", "", "player", null, data));
                client = dumbClient;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        GameDelegate.setGame(new AdventureGame(client));
    }
}
