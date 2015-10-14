/* Contributor List  */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 package swen.adventure.engine.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * Created by David Barnett, Student ID 3003123764, on 05/10/15.
 */

/**
 * A simple implementation of the Client interface with no network connections
 * and allows pushing items into the poll queue
 */
public class DumbClient implements Client<EventBox> {

    private Queue<EventBox> queue;

    public DumbClient() {
        queue = new LinkedList<>();
    }

    @Override
    public void connect(String host, int port) throws IOException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public Optional<EventBox> poll() {
        EventBox box = queue.poll();
        if (box == null) {
            return Optional.empty();
        } else {
            return Optional.of(box);
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean send(EventBox message) {
        return false;
    }

    /**
     * Add the event box to the poll queue
     *
     * @param box event to be added
     */
    public void add(EventBox box) {
        queue.add(box);
    }
}