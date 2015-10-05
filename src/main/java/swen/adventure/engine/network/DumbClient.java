package swen.adventure.engine.network;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by David Barnett, Student ID 3003123764, on 05/10/15.
 */
public class DumbClient implements Client<EventBox> {
    @Override
    public void connect(String host, int port) throws IOException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public Optional<EventBox> poll() {
        return Optional.empty();
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
}
