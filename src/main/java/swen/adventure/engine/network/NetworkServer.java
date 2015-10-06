package swen.adventure.engine.network;

import swen.adventure.engine.datastorage.SceneGraphSerializer;
import swen.adventure.engine.scenegraph.SceneNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Created by David Barnett, Student ID 3003123764, on 17/09/15.
 */
public class NetworkServer implements Server<String, EventBox>, Session.SessionStrategy {
    private final Map<String, Session> clients;
    private final Map<String, String> netIdToGameObj;
    private final Queue<EventBox> queue;
    private ServerSocket serverSocket;
    private Thread acceptThread;

    /**
     * A network server ready to be started with start()
     *
     */
    public NetworkServer() {
        clients = new ConcurrentHashMap<>();
        netIdToGameObj = new ConcurrentHashMap<>();
        queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * @see Server
     */
    @Override
    public void start(int port) throws IOException {
        if (acceptThread != null) {
            throw new RuntimeException(); // TODO: error message
        }

        serverSocket = new ServerSocket(port);

        // Move accepting clients to a different thread
        acceptThread = new Thread(() -> acceptLoop());
        acceptThread.start();
    }

    /**
     * @see Server
     */
    @Override
    public void stop() {
        if (!this.isRunning()) {
            throw new RuntimeException("Cannot stop a server which is not running");
        }
        try {
            serverSocket.close();
            for (Session session : clients.values()) {
                session.close(); // FIXME: make stopping more graceful than ripping the plug out
            }
        } catch (IOException ex) {

        }
    }

    @Override
    public boolean send(String id, EventBox message) {
        if (!this.isRunning()) {
            throw new RuntimeException("Cannot send with a server which is not running");
        }
        Session session = clients.get(id);
        if (session == null) {
            return false;
        }
        if (!session.isConnected()) {
            this.clients.remove(id);
            return false;
        }

        try {
            session.send(new Packet(Packet.Operation.SERVER_DATA, message.getBytes()));
            return true;
        } catch (IOException ex) {
            System.out.println("Server: Failed to send to " + id + ": " + ex);
            return false;
        }
    }

    @Override
    public boolean sendSnapShot(String id, SceneNode root) {
        Session session = clients.get(id);
        if (session == null) {
            return false;
        }

        try {
            ByteArrayOutputStream bio = new ByteArrayOutputStream();
            SceneGraphSerializer.serializeToStream(root, bio);
            session.send(new Packet(Packet.Operation.SNAPSHOT, bio.toByteArray()));
            return true;
        } catch (IOException ex) {
            System.out.println("Server: Failed to send to " + id + ": " + ex);
            return false;
        }
    }

    @Override
    public void sendAll(EventBox message, String... exclude) {
        List<String> ex = Arrays.asList(exclude);
        this.getClientIds().stream().filter(id -> !ex.contains(id)).forEach(id -> send(id, message));
    }

    @Override
    public List<String> getClientIds() {
        if (!this.isRunning()) {
            throw new RuntimeException("Cannot get ids with a server which is not running");
        }
        return clients.keySet().stream().filter(id -> clients.get(id).isConnected()).collect(Collectors.toList());
    }

    /**
     *
     * @see Server
     */
    @Override
    public boolean isRunning() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    /**
     * @see Server
     */
    @Override
    public synchronized Optional<EventBox> poll() {
        if (!this.isRunning()) {
            throw new RuntimeException("Cannot poll a server which is not running");
        }
        EventBox event = queue.poll();
        if (event != null) {
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void received(Session from, Packet packet) {
        try {
        switch (packet.getOperation()) {
            case CLIENT_CONNECT:
                String id = new String(packet.getPayload());
                if (clients.containsKey(id)) {
                    from.send(new Packet(Packet.Operation.CLIENT_KICK));
                    break;
                }
                clients.put(id, from);
                System.out.println("Client connected id:" + id);
                queue.add(new EventBox("playerConnected", id, "spawnPoint", id, Collections.emptyMap()));
                break;
            case CLIENT_DATA:
                queue.add(EventBox.fromBytes(packet.getPayload()));
                break;
            case PING:
                from.send(new Packet(Packet.Operation.PONG, packet.getPayload()));
                break;
            default:
                System.out.println("Unimplemented Server operation: " + packet.getOperation());
                break;
        }
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @Override
    public void connected(Session session) {
        //
    }

    @Override
    public void disconnected(Session session) {
        // Remove disconnected session from clients list
        String id = null;
        for (String i : clients.keySet()) {
            if (clients.get(i).equals(session)) {
                id = i;
                break;
            }
        }
        if (id != null) {
            System.out.println("Client disconnected id:" + id);
            clients.remove(id);
        }

        // TODO: inform other users of this disconnect
    }

    /**
     * loop to accept clients connecting to server then moving the client to separate threads
     */
    private void acceptLoop() {
        while (!serverSocket.isClosed()) {
            try {
                Socket accepted = serverSocket.accept();
                System.out.println("Server accepted client on port: " + accepted.getPort());

                Session session = new Session(accepted, this);
                new Thread(session, this.getClass().getSimpleName() + "Thread#" + accepted.getPort()).start();

            } catch (IOException ex) {
                System.out.println("Server accept Error: " + ex);
                break;
            }
        }
    }

    // Example usage & live testing
    public static void main(String[] args) {
        try {
            Server<String, EventBox> srv = new NetworkServer();
            NetworkClient cli = new NetworkClient("JohnDoe");
            int i = 0;
            srv.start(1025);
            cli.connect("localhost", 1025);

            while(true) {
                // emulate game-loop
                Optional<EventBox> res = srv.poll();
                if (res.isPresent()) {
                    System.out.println("srv Polled: " + res.get());
                }

                srv.sendAll(new EventBox("hey", "it's", "cool", "guy", Collections.EMPTY_MAP));

                try {
                    // Kill server after 10sec to test shutdown
                    Thread.sleep(16);
                    //srv.stop();
                } catch (InterruptedException ex) {

                }
            }

        } catch (IOException ex) {
            System.out.println("Err: " + ex);
        }
    }

    @Override
    public String toString() {
        return "NetworkServer";
    }
}
