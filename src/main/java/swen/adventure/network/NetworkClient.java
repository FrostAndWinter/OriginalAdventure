package swen.adventure.network;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by David Barnett, Student ID 3003123764, on 23/09/15.
 */
public class NetworkClient implements Client, Session.SessionStrategy {

    private final Queue<String> queue;
    private final String id;
    private Session session;
    private double ping = -1;

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
            session.send(new Packet(Packet.Operation.CLIENT_DISCONNECT, new byte[0]));
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
    public Optional<String> poll() {
        if (!isConnected()) {
            throw new RuntimeException("Cannot poll a client that is not connected");
        }

        String event = queue.poll();
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
    public boolean send(String message) {
        if (!isConnected()) {
            throw new RuntimeException("Cannot send with client that is not connected");
        }

        // FIXME: Ping example
        try {
            String nano = Long.toString(System.nanoTime(), 16);
            session.send(new Packet(Packet.Operation.PING, nano.getBytes()));
        } catch(IOException ex) {
            ex.printStackTrace();
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
        try {
        switch (packet.getOperation()) {
            case SERVER_DATA:
                queue.add(new String(packet.getPayload()));
                from.send(new Packet(Packet.Operation.CLIENT_DATA, packet.getPayload()));
                break;
            case SERVER_KILL:
                disconnect();
                break;
            case PING:
                from.send(new Packet(Packet.Operation.PONG, packet.getPayload()));
                break;
            case PONG:
                ping = (System.nanoTime() - Long.parseLong(new String(packet.getPayload()), 16)) / 1000000.0;
                break;
            default:
                System.out.println("Unimplemented Client operation: " + packet.getOperation());
                break;
        }
        } catch (IOException ex) { ex.printStackTrace(); }

    }

    @Override
    public void connected(Session session) {
        try {
            session.send(new Packet(Packet.Operation.CLIENT_CONNECT, id.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnected(Session session) {

    }

    /**
     * Get the result of the last round-trip ping to the client
     *
     * @return ping in milliseconds
     */
    public double getPing() {
        return ping;
    }

    @Override
    public String toString() {
        return "NetworkClient";
    }
}
