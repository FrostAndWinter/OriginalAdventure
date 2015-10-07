package swen.adventure.game;

import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.SceneGraphParser;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkServer;
import swen.adventure.engine.network.Server;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;
import swen.adventure.game.scenenodes.SpawnNode;

import java.io.File;
import java.io.IOException;
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

            GameObject source = (GameObject)root.nodeWithID(event.sourceId).get();

            if (event.eventName.equals("playerConnected")) {
                server.sendSnapShot(event.from, root);

                SpawnNode spawn = (SpawnNode) source;
                spawn.spawnPlayerWithId(event.targetId);
                server.sendAll(event, event.from);
                continue;
            }

            GameObject target = (GameObject)root.nodeWithID(event.targetId).get();
            Event e = target.eventWithName(event.eventName);
            e.trigger(source, event.eventData);

            server.sendAll(event, event.from);
        }
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
