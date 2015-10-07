package swen.adventure.engine.network;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * Created by David Barnett, Student ID 3003123764, on 05/10/15.
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

    @Override
    public double getPing() {
        return -1;
    }

    public void add(EventBox box) {
        queue.add(box);
    }
}
