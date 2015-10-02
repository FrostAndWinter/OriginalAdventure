package swen.adventure.engine;

/**
 * Created by josephbennett on 2/10/15
 */
public interface Game {
    void setup(int width, int height);

    void setSize(int width, int height);

    void setSizeInPixels(int width, int height);

    void update(long deltaMillis);

    KeyInput keyInput();

    MouseInput mouseInput();

    void onMouseDeltaChange(float deltaX, float deltaY);
}
