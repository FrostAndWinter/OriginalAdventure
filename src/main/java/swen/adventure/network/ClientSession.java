package swen.adventure.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;

/**
 * Created by David Barnett, Student ID 3003123764, on 19/09/15.
 */
class ClientSession implements Runnable { // FIXME
    private final Socket client;
    private final OutputStream outputStream;
    private final Queue<Packet> eventQueue;

    private final int id;
    private static int ID_COUNT = 0; // FIXME: better id for connected clients

    public ClientSession(Socket client, Queue<Packet> eventQueue) throws IOException {
        this.client = client;
        this.eventQueue = eventQueue;
        id = ID_COUNT++;
        outputStream = client.getOutputStream();

    }

    public int getId() {
        return id;
    }

    public void send(byte[] data) throws IOException {
        outputStream.write(data);
    }

    public void close() throws IOException {
        this.client.close();
    }

    public boolean isConnected() {
        return client.isConnected();
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
                Optional<Packet> packet = Packet.fromBytes(recv);
                if (packet.isPresent()) {
                    eventQueue.add(packet.get());
                    System.out.println("Client#" + id + " enqueued data length " + packet.get().toBytes().length);
                }


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
