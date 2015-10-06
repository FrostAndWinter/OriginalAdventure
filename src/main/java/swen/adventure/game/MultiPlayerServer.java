package swen.adventure.game;

import swen.adventure.engine.Event;
import swen.adventure.engine.datastorage.SceneGraphParser;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkServer;
import swen.adventure.engine.network.Server;
import swen.adventure.engine.scenegraph.GameObject;
import swen.adventure.engine.scenegraph.SceneNode;

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
            root = SceneGraphParser.parseSceneGraph(new File(map));
            server.start(port);
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
