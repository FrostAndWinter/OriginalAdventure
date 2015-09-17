package swen.adventure.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by David Barnett, Student ID 3003123764, on 17/09/15.
 */
public class NetworkServer implements Server {
    private final Map<Integer, ClientSession> clients;
    private final ServerSocket serverSocket;

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
                session.client.close(); // FIXME: make stopping more graceful than ripping the plug out
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
            session.send(message.getBytes());
            return true;
        } catch (IOException ex) {
            System.out.println("Server: Failed to send to " + id + ": " + ex);
            return false;
        }
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
        return Optional.empty();
    }


    /**
     * loop to accept clients connecting to server then moving the client to separate threads
     */
    private void acceptLoop() {
        while (!serverSocket.isClosed()) {
            try {
                Socket accepted = serverSocket.accept();
                System.out.println("Server accepted client on port: " + accepted.getPort());

                ClientSession session = new ClientSession(accepted);
                Thread clientThread = new Thread(session);
                clientThread.start();
                clients.put(session.getId(), session);

            } catch (IOException ex) {
                System.out.println("Server accept Error: " + ex);
                break;
            }
        }
    }

    private static class ClientSession implements Runnable { // FIXME
        private final Socket client;
        private final OutputStream outputStream;

        private final int id;
        private static int ID_COUNT = 0; // FIXME: better id for connected clients

        public ClientSession(Socket client) throws IOException {
            this.client = client;
            id = ID_COUNT++;
            outputStream = client.getOutputStream();
        }

        public int getId() {
            return id;
        }

        public void send(byte[] data) throws IOException {
            outputStream.write(data);
        }

        public void run() {
            InputStream input;
            byte[] buffer;
            try {
                input = client.getInputStream();
                buffer = new byte[client.getReceiveBufferSize()];
            } catch (IOException ex) {
                System.out.println("client#" + id + " input stream error: " + ex);
                return;
            }

            while (!client.isClosed() && client.isConnected()) {
                try {
                    int len = input.read(buffer);
                    if (len == -1) {
                        System.out.println("Client#" + id + " End of Stream");
                        break;
                    }
                    byte[] recv = Arrays.copyOf(buffer, len);

                    // TODO: transform into a useful object
                    String recvStr = new String(recv);

                    System.out.println("Client#" + id + " recv: " + recvStr.length());

                } catch (IOException ex) {
                    System.out.println("Client#" + id + " error: " + ex);
                    break;
                }
            }

            // Cleanup
            if (!client.isClosed()) {
                try {
                    client.close();
                } catch (IOException ex) {
                    // muffu muffu~
                }
            }
        }
    }

    // Example usage & live testing
    public static void main(String[] args) {
        try {
            NetworkServer srv = new NetworkServer(1025);
            srv.start();

            // emulate game-loop
            Optional<String> res = srv.poll();
            if (res.isPresent()) {
                System.out.println(res.get());
            }

            try {
                // Kill server after 10sec to test shutdown
                Thread.sleep(10000);
                srv.stop();
            } catch (InterruptedException ex) {

            }

        } catch (IOException ex) {

        }
    }
}
