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
public class NetworkServer implements Server {
    private final Map<Integer, ClientSession> clients;
    private final ServerSocket serverSocket;
    private final Queue<Packet> eventQueue;

    private Thread acceptThread;

    /**
     * Create a network server on the given port
     *
     * @param port application port to be used
     * @throws IOException
     */
    public NetworkServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = new HashMap<>();
        eventQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * @see Server
     */
    @Override
    public void start() {
        if (acceptThread != null) {
            throw new RuntimeException(); // TODO: error message
        }

        // Move accepting clients to a different thread
        acceptThread = new Thread(() -> acceptLoop());
        acceptThread.start();
    }

    /**
     * @see Server
     */
    @Override
    public void stop() {
        try {
            serverSocket.close();
            for (ClientSession session : clients.values()) {
                session.close(); // FIXME: make stopping more graceful than ripping the plug out
            }
        } catch (IOException ex) {

        }
    }

    @Override
    public boolean send(int id, String message) {
        ClientSession session = clients.get(id);
        if (session == null) {
            return false;
        }
        try {
            Packet toSend = new Packet(Packet.Operation.SERVER_DATA, message.getBytes());
            session.send(toSend.toBytes());
            return true;
        } catch (IOException ex) {
            System.out.println("Server: Failed to send to " + id + ": " + ex);
            return false;
        }
    }

    @Override
    public List<Integer> getClientIds() {
        return clients.values().stream().filter(clientSession -> clientSession.isConnected())
                .map(ClientSession::getId).collect(Collectors.toList());
    }

    /**
     *
     * @see Server
     */
    @Override
    public boolean isRunning() {
        return acceptThread != null && serverSocket.isBound() && !serverSocket.isClosed();
    }

    /**
     * @see Server
     */
    @Override
    public synchronized Optional<String> poll() {
        Packet event = eventQueue.poll();
        if (event != null) {
            return Optional.of(new String(event.getPayload()));
        } else {
            return Optional.empty();
        }
    }


    /**
     * loop to accept clients connecting to server then moving the client to separate threads
     */
    private void acceptLoop() {
        while (!serverSocket.isClosed()) {
            try {
                Socket accepted = serverSocket.accept();
                System.out.println("Server accepted client on port: " + accepted.getPort());

                ClientSession session = new ClientSession(accepted, eventQueue);
                Thread clientThread = new Thread(session);
                clientThread.start();
                clients.put(session.getId(), session);

            } catch (IOException ex) {
                System.out.println("Server accept Error: " + ex);
                break;
            }
        }
    }

    // Example usage & live testing
    public static void main(String[] args) {
        try {
            NetworkServer srv = new NetworkServer(1025);

            srv.start();
            while(true) {
                // emulate game-loop
                Optional<String> res = srv.poll();
                if (res.isPresent()) {
                    System.out.println("Polled: " + res.get());
                }

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
