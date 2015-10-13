package swen.adventure.engine.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by David Barnett, Student ID 3003123764, on 19/09/15.
 */

/**
 * Package private network session use by both client and server side
 */
class Session implements Runnable { // FIXME
    private final Socket socket;
    private final OutputStream outputStream;
    private final SessionStrategy strategy;


    public Session(Socket socket, SessionStrategy strategy) throws IOException {
        this.socket = socket;
        this.strategy = strategy;
        outputStream = socket.getOutputStream();

        strategy.connected(this);
    }

    public void send(Packet msg) throws IOException {
        outputStream.write(msg.toBytes());
    }

    public void close() throws IOException {
        socket.close();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void run() {
        InputStream input;
        byte[] buffer;
        Packet.Builder builder = new Packet.Builder();
        try {
            input = socket.getInputStream();
            buffer = new byte[1024];
        } catch (IOException ex) {
            System.out.println(strategy + "@" + socket.getLocalSocketAddress() + " input stream error: " + ex);
            return;
        }


        System.out.println(strategy + "@" + socket.getLocalSocketAddress() + " started loop");
        while (!socket.isClosed() && socket.isConnected()) {
            try {
                int len = input.read(buffer);
                if (len == -1) {
                    System.out.println(strategy + "@" + socket.getLocalSocketAddress() + " End of Stream");
                    break;
                }

                // TODO: Make sure packet boundaries are correct
                byte[] recv = Arrays.copyOf(buffer, len);
                builder.append(recv);
                while (builder.isReady()) {
                    strategy.received(this, builder.build());
                    builder = new Packet.Builder().append(builder.overflow());
                }


            } catch (IOException ex) {
                System.out.println(strategy + "@" + socket.getLocalSocketAddress() + " error: " + ex);
                break;
            }
        }

        // Cleanup
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ex) {
                // muffu muffu~
            }
        }
        strategy.disconnected(this);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        if (!socket.equals(session.socket)) return false;
        if (!outputStream.equals(session.outputStream)) return false;
        return strategy.equals(session.strategy);
    }

    @Override
    public int hashCode() {
        int result = socket.hashCode();
        result = 31 * result + outputStream.hashCode();
        result = 31 * result + strategy.hashCode();
        return result;
    }

    public interface SessionStrategy {

        /**
         * Called after session established connection and
         * before the message receive loop.
         *
         * @param session The session that has been established
         */
        void connected(Session session);

        /**
         * The session has closed.
         * Network connections is closed
         *
         * @param session the closed session
         */
        void disconnected(Session session);

        /**
         * Packet is received from the session
         *
         * @param from the session the packet was from
         * @param packet the received packet of information
         */
        void received(Session from, Packet packet);
    }
}
