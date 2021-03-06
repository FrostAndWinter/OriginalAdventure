/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.game;

import processing.opengl.PGraphics2D;
import swen.adventure.Settings;
import swen.adventure.engine.*;
import swen.adventure.engine.datastorage.EventConnectionParser;
import swen.adventure.engine.datastorage.ParserException;
import swen.adventure.engine.datastorage.SceneGraphParser;
import swen.adventure.engine.network.Client;
import swen.adventure.engine.network.DumbClient;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkClient;
import swen.adventure.engine.rendering.GLDeferredRenderer;
import swen.adventure.engine.rendering.GLForwardRenderer;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.rendering.PickerRenderer;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.game.input.AdventureGameKeyInput;
import swen.adventure.game.input.AdventureGameMouseInput;
import swen.adventure.game.scenenodes.AdventureGameObject;
import swen.adventure.game.scenenodes.Inventory;
import swen.adventure.game.scenenodes.Player;
import swen.adventure.game.scenenodes.SpawnNode;
import swen.adventure.game.ui.components.InventoryComponent;
import swen.adventure.game.ui.components.UI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class AdventureGame implements Game {

    private GLForwardRenderer _forwardRenderer;
    private GLRenderer _mainRenderer;
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
    private boolean _viewAngleUpdated = false;

    private float virtualUIWidth;
    private float virtualUIHeight;

    private EnumMap<InteractionType, Interaction> _possibleInteractionsForStep = new EnumMap<>(InteractionType.class);
    private EnumMap<Interaction.ActionType, Interaction> _interactionInProgressForActionType = new EnumMap<>(Interaction.ActionType.class);

    public AdventureGame(Client<EventBox> client) {
        _client = client;
    }

    @Override
    public String title() {
        return "Original Adventure";
    }

    @Override
    public void setup(int width, int height) {
        if (!Utilities.isHeadlessMode) {
            _forwardRenderer = new GLForwardRenderer(width, height);
            _mainRenderer = Settings.DeferredShading ? new GLDeferredRenderer(width, height) : _forwardRenderer;
            _pickerRenderer = new PickerRenderer();
        }

        File sceneGraphFile = new File(Utilities.pathForResource("SceneGraph", "xml"));
        _sceneGraph = loadSceneGraphFromFile(sceneGraphFile);

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
        setupSceneGraph(loadSceneGraphFromString(event.eventData.get("scenegraph").toString(), _sceneGraph), event.targetId);
    }

    private TransformNode loadSceneGraphFromFile(File sceneGraphFile) {
        try {
            return SceneGraphParser.parseSceneGraph(sceneGraphFile);
        } catch (FileNotFoundException e) {
            System.err.println("Can't find file " + sceneGraphFile);
        } catch (ParserException e) {
            System.err.println("Error while parsing the scene graph.");
            e.printStackTrace();
            e.getCause().printStackTrace();
        }

        fail();
        return null; // dead code
    }

    private TransformNode loadSceneGraphFromString(String xml, TransformNode existingGraph) {
        try {
            return SceneGraphParser.parseSceneGraph(xml, existingGraph);
        } catch (ParserException e) {
            System.err.println(e.getMessage());
        }

        fail();
        return null; // dead code
    }

    private void fail() {
        System.exit(1);
    }

    private void setupSceneGraph(TransformNode sceneGraph, String playerId) {
        _sceneGraph = sceneGraph;

        // Fix to get single player to run
        if (!(_client instanceof NetworkClient)) {
            createPlayer(playerId);
        }
        setPlayer(playerId);

        Event.EventSet playerMovedSet = Event.eventSetForName("PlayerMoved");
        playerMovedSet.addAction(this, MovePlayer);

        try {
            List<EventConnectionParser.EventConnection> connections = EventConnectionParser.parseFile(Utilities.readLinesFromFile(Utilities.pathForResource("EventConnections", "event")));
            EventConnectionParser.setupConnections(connections, _sceneGraph);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get the possible interactions a player can make this step
        Event.EventSet<AdventureGameObject, Player> interactionEvents = (Event.EventSet<AdventureGameObject, Player>) Event.eventSetForName("ShouldProvideInteraction");
        interactionEvents.addAction(this, (gameObject, player, adventureGame, data) -> {
            Interaction interaction = (Interaction) data.get(EventDataKeys.Interaction);
            _possibleInteractionsForStep.put(interaction.interactionType, interaction);
        });

        this.setupUI((int) virtualUIWidth, (int) virtualUIHeight);
    }


    private static final Action<Player, Player, AdventureGame> MovePlayer = (eventObject, triggeringObject, listener, data) -> {
        if (data.containsKey(EventDataKeys.Networked)) {
            if (data.containsKey(EventDataKeys.Location)) {
                eventObject.parent().get().setTranslation((Vector3) data.get(EventDataKeys.Location));
            }
            if (data.containsKey(EventDataKeys.Quaternion)) {
                eventObject.parent().get().setRotation((Quaternion) data.get(EventDataKeys.Quaternion));
            }
        } else {
            listener._client.send(new EventBox("PlayerMoved", triggeringObject, eventObject, listener._player, data));
        }
    };


    private void sendInteraction(Interaction interaction) {
        Map<String, Object> data = new HashMap<>();
        data.put(EventDataKeys.InteractionType, interaction.interactionType);
        _client.send(new EventBox("InteractionPerformed",
                interaction.gameObject.id,
                interaction.meshNode.id,
                _player.id,
                data));
    }

    private void sendEndInteraction(Interaction interaction) {
        Map<String, Object> data = new HashMap<>();
        data.put(EventDataKeys.InteractionType, interaction.interactionType);
        _client.send(new EventBox("InteractionEnded",
                interaction.gameObject.id,
                interaction.meshNode.id,
                _player.id,
                data));
    }

    private void sendPlayerSelectSlot(int slot) {
        Map<String, Object> data = new HashMap<>();
        data.put(EventDataKeys.Slot, slot);
        _client.send(new EventBox("PlayerSlotSelected",
                _player.id,
                _player.id,
                _player.id,
                data));
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

    private void createPlayer(String playerId) {
        SpawnNode spawn = (SpawnNode)_sceneGraph.nodeWithID(SpawnNode.ID).get();
        spawn.spawnPlayerWithId(playerId);

        Player newPlayer = (Player)_sceneGraph.nodeWithID(playerId).get();

        newPlayer.eventPlayerMoved.addAction(this, MovePlayer);
    }

    private void setPlayer(String playerId) {
        _player = (Player)_sceneGraph.nodeWithID(playerId).get();
        _player.mesh().ifPresent(meshNode -> meshNode.setEnabled(false));
    }

    /**
     * Performs all interactions for the specified action type.
     * @param actionType The action type to perform the interactions for.
     */
    private void performInteractions(Interaction.ActionType actionType) {
        this.endInteractions(actionType);
        List<InteractionType> interactionTypes = InteractionType.typesForActionType(actionType);

        interactionTypes.stream()
                .map(_possibleInteractionsForStep::get)
                .filter(interaction -> interaction != null)
                .forEach(interaction -> {
                    interaction.performInteractionWithPlayer(_player);
                    sendInteraction(interaction);
                    _interactionInProgressForActionType.put(actionType, interaction);
                });
    }

    private void endInteractions(Interaction.ActionType actionType) {
        Interaction interaction = _interactionInProgressForActionType.get(actionType);
        if (interaction != null) {
            interaction.interactionEndedByPlayer(_player);
            sendEndInteraction(interaction);
        }
        _interactionInProgressForActionType.put(actionType, null);
    }

    private void checkForInteractionsToEndBasedOnDistance() {
        for (Interaction.ActionType actionType : Interaction.ActionType.values()) {
            Interaction interaction = _interactionInProgressForActionType.get(actionType);
            if (interaction != null) {
                if (!Interaction.playerCanInteractWithObject(_player, interaction.gameObject)) {
                    this.endInteractions(actionType);
                }
            }
        }
    }

    private void setupUI(int width, int height) {
        virtualUIWidth = width;
        virtualUIHeight = height;

        _pGraphics = new PGraphics2D();
        _pGraphics.setPrimary(true);
        _pGraphics.setSize(width, height);

        ui = new UI(width, height, _player);

        _keyInput.eventMoveInDirection.addAction(this._player, Player.actionMoveInDirection);

        _mouseInput.eventMousePrimaryAction.addAction(this, AdventureGame.primaryActionFired);
        _mouseInput.eventMousePrimaryActionEnded.addAction(this, AdventureGame.primaryActionEnded);
        _mouseInput.eventMouseSecondaryAction.addAction(this, AdventureGame.secondaryActionFired);
        _mouseInput.eventMouseSecondaryActionEnded.addAction(this, AdventureGame.secondaryActionEnded);

        _keyInput.eventPrimaryAction.addAction(this, AdventureGame.primaryActionFired);
        _keyInput.eventPrimaryActionEnded.addAction(this, AdventureGame.primaryActionEnded);
        _keyInput.eventSecondaryAction.addAction(this, AdventureGame.secondaryActionFired);
        _keyInput.eventSecondaryActionEnded.addAction(this, AdventureGame.secondaryActionEnded);

        _keyInput.eventMoveInDirection.addAction(_player, Player.actionMoveInDirection);

        _keyInput.eventSelectInventorySlot1.addAction(_player.inventory(), Inventory.actionSelectSlot1);
        _keyInput.eventSelectInventorySlot2.addAction(_player.inventory(), Inventory.actionSelectSlot2);
        _keyInput.eventSelectInventorySlot3.addAction(_player.inventory(), Inventory.actionSelectSlot3);
        _keyInput.eventSelectInventorySlot4.addAction(_player.inventory(), Inventory.actionSelectSlot4);
        _keyInput.eventSelectInventorySlot5.addAction(_player.inventory(), Inventory.actionSelectSlot5);

        _keyInput.eventSelectInventorySlot1.addAction(_player.inventory(), sendSlotToNetwork);
        _keyInput.eventSelectInventorySlot2.addAction(_player.inventory(), sendSlotToNetwork);
        _keyInput.eventSelectInventorySlot3.addAction(_player.inventory(), sendSlotToNetwork);
        _keyInput.eventSelectInventorySlot4.addAction(_player.inventory(), sendSlotToNetwork);
        _keyInput.eventSelectInventorySlot5.addAction(_player.inventory(), sendSlotToNetwork);

        _keyInput.eventHideShowInventory.addAction(ui.getInventory(), InventoryComponent.actionToggleZoomItem);

        _keyInput.eventHideShowControls.addAction(ui, UI.actionToggleControlls);
    }

    public final Action<Input, Input, Inventory> sendSlotToNetwork =
            (eventObject, triggeringObject, inventory, data) -> {
                sendPlayerSelectSlot(_player.inventory().selectedSlot());
            };


        @Override
    public void setSize(int width, int height) {
        if (_mainRenderer != null) {
            _mainRenderer.setSize(width, height);
        }
        if (_pGraphics != null) {
            _pGraphics.setSize(width, height);
        }
        _forwardRenderer.setSize(width, height);
    }

    @Override
    public void setSizeInPixels(int width, int height) {
        _pGraphics.setPixelDimensions(width, height);
        _mainRenderer.setSizeInPixels(width, height);
        _forwardRenderer.setSizeInPixels(width, height);
    }

    @Override
    public void update(long deltaMillis) {

        final Optional<MeshNode> meshBeingLookedAt = _pickerRenderer.selectedNode();
        _possibleInteractionsForStep.clear();

        this.checkForInteractionsToEndBasedOnDistance();

        Optional<EventBox> box;
        while ((box = _client.poll()).isPresent()) {
            EventBox event = box.get();
            event.eventData.put(EventDataKeys.Networked, true);
            SceneNode source = _sceneGraph.nodeWithID(event.sourceId).get();

            if (event.eventName.equals("playerConnected")) {
                createPlayer(event.targetId);
                continue;
            }

            if (event.eventName.equals("InteractionPerformed")) {
                AdventureGameObject gameObject = (AdventureGameObject)_sceneGraph.nodeWithID(event.sourceId).get();
                MeshNode meshNode = (MeshNode)_sceneGraph.nodeWithID(event.targetId).get();
                Player player = (Player)_sceneGraph.nodeWithID(event.from).get();

                Interaction interaction = new Interaction((InteractionType) event.eventData.get(EventDataKeys.InteractionType), gameObject, meshNode);

                interaction.performInteractionWithPlayer(player);
                continue;
            }

            if (event.eventName.equals("InteractionEnded")) {
                AdventureGameObject gameObject = (AdventureGameObject) _sceneGraph.nodeWithID(event.sourceId).get();
                MeshNode meshNode = (MeshNode) _sceneGraph.nodeWithID(event.targetId).get();
                Player player = (Player) _sceneGraph.nodeWithID(event.from).get();

                Interaction interaction = new Interaction((InteractionType) event.eventData.get(EventDataKeys.InteractionType), gameObject, meshNode);

                interaction.interactionEndedByPlayer(player);
                continue;
            }

            SceneNode target = _sceneGraph.nodeWithID(event.targetId).get();
            Event e = target.eventWithName(event.eventName);
            e.trigger(source, event.eventData);
        }

        meshBeingLookedAt.ifPresent(meshNode -> meshNode.eventMeshLookedAt.trigger(this._player, Collections.singletonMap(EventDataKeys.Mesh, meshNode)));

        GameDelegate.pollInput();

        //Set where the _player is looking.
        if (_viewAngleUpdated) {
            _player.setLookDirection(_viewAngleX, _viewAngleY);
            _viewAngleUpdated = false;
        }
        this.render();
    }

    private void render() {
        if (Utilities.isHeadlessMode) {
            return;
        }

        CameraNode camera = _player.camera();
        List<MeshNode> meshNodesSortedByZ = DepthSorter.sortedMeshNodesByZ(_sceneGraph, camera.worldToNodeSpaceTransform());

        _pickerRenderer.render(meshNodesSortedByZ, camera.worldToNodeSpaceTransform());
        _mainRenderer.render(meshNodesSortedByZ, _sceneGraph.allNodesOfType(Light.class), camera.worldToNodeSpaceTransform(), camera.fieldOfView(), camera.hdrMaxIntensity());


        ArrayList<String> tips = new ArrayList<>();
        for (Interaction interaction : _possibleInteractionsForStep.values()) {
            Interaction.ActionType actionType = interaction.interactionType.actionType;
            Character character = null;
            switch (actionType) {
                case Primary:
                    character = _keyInput.characterForEvent(_keyInput.eventPrimaryAction);
                    break;
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

        ui.drawUI(_pGraphics, _forwardRenderer);
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
        float oldVX = _viewAngleX, oldVY = _viewAngleY;
        _viewAngleX = (_viewAngleX + deltaX / _mouseSensitivity) % (float)(2 * Math.PI);
        _viewAngleY = (_viewAngleY + deltaY / _mouseSensitivity) % (float)(2 * Math.PI);
        _viewAngleUpdated = oldVX != _viewAngleX || oldVY != _viewAngleY;
    }

    @Override
    public void cleanup() {
        _client.disconnect();
    }



    public static void startGame(String[] args) {
        // Start with networking using CLI arguments <_player id> <host> <port>
        Client<EventBox> client;
        if (args.length == 3) {
            client = new NetworkClient(args[0]);
            try {
                client.connect(args[1], Integer.parseInt(args[2]));
            } catch (IOException e) {
                throw new InvalidServerConfig();
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

    public static void main(String[] args) {
        startGame(args);
    }
}