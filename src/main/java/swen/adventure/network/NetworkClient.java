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
    private Session session;


    public NetworkClient() {
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

        return null;
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
                queue.add(new String(packet.getPayload()));
                break;
            case SERVER_KILL:
                disconnect();
                break;
            default:
                System.out.println("Unimplemented operation: " + packet.getOperation());
                break;
        }
    }

    @Override
    public String toString() {
        return "NetworkClient";
    }
}
