package swen.adventure.network;

import java.util.Optional;

/**
 * Created by David Barnett, Student ID 3003123764, on 17/09/15.
 */
public interface Server {

    /**
     * Start the server accepting clients and messages
     */
    void start();

    /**
     * Stop serving all clients
     */
    void stop();

    /**
     * Get message received from a client
     *
     * @return The value is present when the server has messages queued from the clients to be processed. Otherwise
     * empty.
     */
     Optional<String> poll(); // FIXME: replace String with an encapsulated data-type

    /**
     * Check if the server is running
     *
     * @return returns true if the server is open and accepting requests
     */
    boolean isRunning();

    /**
     * Send the message to the client with the given id
     *
     * @param id of the client to be sent to
     * @param message contents to be sent
     * @return returns true if the message was successfully sent, otherwise false
     */
    boolean send(int id, String message); // FIXME: replace String with an encapsulate data-type
}
