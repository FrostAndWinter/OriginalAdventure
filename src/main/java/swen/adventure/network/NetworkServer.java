package swen.adventure.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Created by David Barnett, Student ID 3003123764, on 17/09/15.
 */
public class NetworkServer implements Server, Session.SessionStrategy {
    private final Map<Integer, Session> clients;
    private final Queue<String> queue;
    private ServerSocket serverSocket;

    private Thread acceptThread;

    /**
     * A network server ready to be started with start()
     *
     */
    public NetworkServer() {
        clients = new HashMap<>();
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
    public boolean send(int id, String message) {
        if (!this.isRunning()) {
            throw new RuntimeException("Cannot send with a server which is not running");
        }
        Session session = clients.get(id);
        if (session == null) {
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
    public List<Integer> getClientIds() {
        if (!this.isRunning()) {
            throw new RuntimeException("Cannot get ids with a server which is not running");
        }
        return clients.keySet().stream().collect(Collectors.toList());
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
    public synchronized Optional<String> poll() {
        if (!this.isRunning()) {
            throw new RuntimeException("Cannot poll a server which is not running");
        }
        String event = queue.poll();
        if (event != null) {
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void received(Session from, Packet packet) {
        switch (packet.getOperation()) {
            case CLIENT_DATA:
                queue.add(new String(packet.getPayload()));
                break;
            default:
                System.out.println("Unimplemented operation: " + packet.getOperation());
                break;
        }
    }

    /**
     * loop to accept clients connecting to server then moving the client to separate threads
     */
    private void acceptLoop() {
        int idCount = 0;
        while (!serverSocket.isClosed()) {
            try {
                Socket accepted = serverSocket.accept();
                System.out.println("Server accepted client on port: " + accepted.getPort());

                int id = idCount++;
                Session session = new Session(accepted, this);
                new Thread(session, this.getClass().getSimpleName() + "Thread#" + id).start();
                clients.put(id, session);

            } catch (IOException ex) {
                System.out.println("Server accept Error: " + ex);
                break;
            }
        }
    }

    // Example usage & live testing
    public static void main(String[] args) {
        try {
            Server srv = new NetworkServer();
            Client cli = new NetworkClient();

            srv.start(1025);
            cli.connect("localhost", 1025);

            while(true) {
                // emulate game-loop
                Optional<String> res = srv.poll();
                if (res.isPresent()) {
                    System.out.println("Polled: " + res.get());
                }

                cli.send("Hello!");

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
}
