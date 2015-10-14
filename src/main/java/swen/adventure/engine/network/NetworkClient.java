/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 package swen.adventure.engine.network;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by David Barnett, Student ID 3003123764, on 23/09/15.
 */

/**
 * Implementation of Client interface backed by a TCP network using EventBoxes
 *
 * @apiNote Before any network operations can be used you must run <code>connect(host, port)</code>
 *          otherwise Runtime exceptions will be thrown
 */
public class NetworkClient implements Client<EventBox>, Session.SessionStrategy {

    private final Queue<EventBox> queue;
    private final String id;
    private Session session;

    /**
     * Creates a Client ready to connect that has the given ID
     *
     * @param id network ID to use in communicated to server
     */
    public NetworkClient(String id) {
        this.id = id;
        queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void connect(String host, int port) throws IOException {
        session = new Session(new Socket(host, port), this);

        new Thread(session, this.getClass().getSimpleName() + "Thread").start();
    }

    @Override
    public void disconnect() {
        if (!isConnected()) {
            throw new RuntimeException("Cannot disconnect a client that is not connected");
        }

        try {
            session.send(new Packet(Packet.Operation.CLIENT_KICK, new byte[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            session.close();
        } catch (IOException ex) {
            // muffu muffu~
        }
    }

    @Override
    public Optional<EventBox> poll() {
        if (!isConnected()) {
            throw new RuntimeException("Cannot poll a client that is not connected");
        }

        EventBox event = queue.poll();
        if (event != null) {
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    @Override
    public boolean send(EventBox message) {
        if (!isConnected()) {
            throw new RuntimeException("Cannot send with client that is not connected");
        }

        try {
            session.send(new Packet(Packet.Operation.CLIENT_DATA, message.getBytes()));
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void received(Session from, Packet packet) {
        switch (packet.getOperation()) {
            case SERVER_DATA:
                queue.add(EventBox.fromBytes(packet.getPayload()));
                break;
            case CLIENT_KICK:
                disconnect();
                break;
            case SNAPSHOT:
                Map<String, Object> data = new HashMap<>();
                data.put("scenegraph", new String(packet.getPayload()));
                queue.add(new EventBox("snapshot", "root", id, null, data));
                break;
            default:
                System.out.println("Unimplemented Client operation: " + packet.getOperation() + " length:" + packet.getPayload().length);
                break;
        }
    }

    @Override
    public void connected(Session session) {
        try {
            session.send(new Packet(Packet.Operation.CLIENT_CONNECT, id.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnected(Session session) {
        //
    }

    @Override
    public String toString() {
        return "NetworkClient";
    }
}