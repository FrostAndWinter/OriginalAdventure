package swen.adventure.game;

import swen.adventure.engine.Event;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.datastorage.EventConnectionParser;
import swen.adventure.engine.datastorage.SceneGraphParser;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkServer;
import swen.adventure.engine.network.Server;
import swen.adventure.engine.rendering.maths.BoundingBox;
import swen.adventure.engine.rendering.maths.Vector3;
import swen.adventure.engine.scenegraph.*;
import swen.adventure.game.scenenodes.Player;
import swen.adventure.game.scenenodes.SpawnNode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


/**
 * Created by David Barnett, Student ID 3003123764, on 29/09/15.
 */
public class MultiPlayerServer implements Runnable {

    private final SceneNode root;
    private final Server<String, EventBox> server;

    public MultiPlayerServer(int port, String map) {
        server = new NetworkServer();
        try {
            System.out.println("Loading map");
            root = SceneGraphParser.parseSceneGraph(new File(map));
            System.out.println("Completed loading map");
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

    public void run() {
        while(true) {
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
                        server.sendSnapShot(event.from, root);
                        createPlayer(event.targetId);
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
        }
    }

    private void createPlayer(String playerId) {
        SpawnNode spawn = (SpawnNode)root.nodeWithID(SpawnNode.ID).get();
        spawn.spawnPlayerWithId(playerId);

        // FIXME: Add CollisionNode to player
        Player newPlayer = (Player)root.nodeWithID(playerId).get();
        new MeshNode(playerId + "Mesh", "", "rocket.obj", newPlayer.parent().get());

        BoundingBox boundingBox = new BoundingBox(new Vector3(-30, -60, -10) , new Vector3(30, 60, 10));
        String colliderID = playerId + "Collider";
        CollisionNode collider = (CollisionNode)spawn.nodeWithID(colliderID).orElseGet(() -> new CollisionNode(colliderID, newPlayer.parent().get(), boundingBox, CollisionNode.CollisionFlag.Player));
        collider.setParent(newPlayer.parent().get());

        newPlayer.setCollisionNode(collider);

        newPlayer.eventPlayerMoved.addAction(this, (eventObject, triggeringObject, listener, data) ->
                        eventObject.parent().get().setTranslation((Vector3)data.get(EventDataKeys.Location))
        );

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
