/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Liam O'Niell (oneilliam) (300312734) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.game;

import swen.adventure.Settings;
import swen.adventure.engine.Event;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.datastorage.EventConnectionParser;
import swen.adventure.engine.datastorage.ParserException;
import swen.adventure.engine.datastorage.SceneGraphParser;
import swen.adventure.engine.datastorage.SceneGraphSerializer;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkServer;
import swen.adventure.engine.network.Server;
import swen.adventure.engine.rendering.maths.Quaternion;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.MeshNode;
import swen.adventure.engine.scenegraph.TransformNode;
import swen.adventure.game.scenenodes.AdventureGameObject;
import swen.adventure.game.scenenodes.Player;
import swen.adventure.game.scenenodes.SpawnNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


/**
 * Created by David Barnett, Student ID 3003123764, on 29/09/15.
 */
public class MultiPlayerServer implements Runnable {

    private final String mapName;
    private final TransformNode root;
    private final Server<String, EventBox> server;

    public MultiPlayerServer(int port, String map) {
        server = new NetworkServer();
        mapName = map;
        try {
            System.out.println("Loading map");
            File sceneGraphFile = new File(Utilities.pathForResource(map, "xml"));
            File sceneGraphSaveFile = new File(Utilities.pathForResource(map + "-savefile", "xml"));
            root = loadSceneGraph(sceneGraphFile);
            System.out.println("Completed loading map");
            if (sceneGraphSaveFile.exists()) {
                System.out.println("loading save file map");
                loadSaveFileSceneGraph(sceneGraphSaveFile);
                System.out.println("completed save file map");
            }
            System.out.println("Setting up event connections");
            // setup event connections
            try {
                List<EventConnectionParser.EventConnection> connections = EventConnectionParser.parseFile(Utilities.readLinesFromFile(Utilities.pathForResource("EventConnections", "event")));
                EventConnectionParser.setupConnections(connections, root);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Completed event connections");
            server.start(port);
            System.out.println("Accepting connections");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private TransformNode loadSceneGraph(File sceneGraphFile) {
        try {
            return SceneGraphParser.parseSceneGraph(sceneGraphFile);
        } catch (FileNotFoundException e) {
            System.err.println("Can't find file: " + sceneGraphFile);
        } catch (ParserException e) {
            System.err.println(e.getMessage());
            e.getCause().printStackTrace();
        }
        fail();
        return null; // dead code
    }

    private TransformNode loadSaveFileSceneGraph(File sceneGraphFile) {
        try {
            return SceneGraphParser.parseSceneGraph(sceneGraphFile, root);
        } catch (FileNotFoundException e) {
            System.err.println("Can't find file: " + sceneGraphFile);
        } catch (ParserException e) {
            System.err.println(e.getMessage());
        }
        fail();
        return null; // dead code
    }

    private void fail() {
        System.exit(1);
    }

    public void run() {
        int eventsCount = 0;
        event: while(server.isRunning()) {
            Optional<EventBox> isEvent = server.poll();
            if (!isEvent.isPresent()) {
                continue;
            }
            EventBox event = isEvent.get();
            System.out.println(String.format("Got: %s source: %s target: %s data: %s", event.eventName, event.sourceId, event.targetId, event.eventData));


            GameObject source = (GameObject)root.nodeWithID(event.sourceId).get();
            try {
                switch (event.eventName) {
                    case "playerConnected":
                        createPlayer(event.targetId);
                        server.sendSnapShot(event.from, root);
                        break;
                    case "playerDisconnected":
                        saveState();
                        continue event; // Do not send this to client
                    case "InteractionPerformed":
                        interactionPerformed(event);
                        break;
                    case "InteractionEnded":
                        interactionEnded(event);
                        break;
                    default:
                        GameObject target = (GameObject) root.nodeWithID(event.targetId).get();
                        Event e = target.eventWithName(event.eventName);
                        e.trigger(source, event.eventData);
                        break;
                }
                server.sendAll(event, event.from);
            } catch (Error ex) {
                System.out.println("Error occurred in Multilayer server: " + ex.toString());
            }

            if (eventsCount >= Settings.EventsTillServerBackup) {
                saveState();
                eventsCount = 0;
            }
            eventsCount++;
        }
        server.stop();
    }

    private void interactionPerformed(EventBox event) {
        Player player = (Player)root.nodeWithID(event.from).get();
        buildInteraction(event).performInteractionWithPlayer(player);
    }

    private void interactionEnded(EventBox event) {
        Player player = (Player)root.nodeWithID(event.from).get();
        buildInteraction(event).interactionEndedByPlayer(player);
    }

    private void saveState() {
        try {
            SceneGraphSerializer.serializeToFile(root, new File(Utilities.pathForResource(mapName + "-savefile", "xml")));
        } catch (FileNotFoundException e) {
            System.err.println("Error while saving server state: " + e);
        }
    }

    private Interaction buildInteraction(EventBox event) {
        AdventureGameObject gameObject = (AdventureGameObject)root.nodeWithID(event.sourceId).get();
        MeshNode meshNode = (MeshNode)root.nodeWithID(event.targetId).get();

        return new Interaction((InteractionType)event.eventData.get(EventDataKeys.InteractionType), gameObject, meshNode);
    }

    private void createPlayer(String playerId) {
        if (root.nodeWithID(playerId).isPresent()) {
            return;
        }
        SpawnNode spawn = (SpawnNode)root.nodeWithID(SpawnNode.ID).get();
        spawn.spawnPlayerWithId(playerId);

        Player newPlayer = (Player)root.nodeWithID(playerId).get();

        newPlayer.eventPlayerMoved.addAction(this, (eventObject, triggeringObject, listener, data) -> {
            if (data.containsKey(EventDataKeys.Location)) {
                eventObject.parent().get().setTranslation((Vector3) data.get(EventDataKeys.Location));
            }
            if (data.containsKey(EventDataKeys.Quaternion)) {
                eventObject.parent().get().setRotation((Quaternion) data.get(EventDataKeys.Quaternion));
            }
        });
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Incorrect number of parameters");
        }

        int port = Integer.parseInt(args[0]);
        String map = args[1];

        new MultiPlayerServer(port, map).run();
    }
}