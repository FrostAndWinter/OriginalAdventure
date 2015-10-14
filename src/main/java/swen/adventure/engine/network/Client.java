/* Contributor List  */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine.network;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by David Barnett, Student ID 3003123764, on 20/09/15.
 */

/**
 * Interface that describes how to interact with the client
 *
 * @param <M> The message type to be sent.
 */
public interface Client<M> {


    /**
     * Connects to given server at host and port
     *
     * @param host a string IP address or hostname
     * @param port port on the server to connect to
     * @throws IOException
     */
    void connect(String host, int port) throws IOException;

    /**
     *  Disconnects from server
     */
    void disconnect();

    /**
     * Get message received from a client
     *
     * @return The value is present when the server has messages queued from the clients to be processed. Otherwise
     * empty.
     */
    Optional<M> poll();

    /**
     * Check if connected to a server
     *
     * @return returns true if the client is connected
     */
    boolean isConnected();

    /**
     * Send the message to the server
     *
     * @param message contents to be sent
     * @return returns true if the message was successfully sent, otherwise false
     */
    boolean send(M message);
}