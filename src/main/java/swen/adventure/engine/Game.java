package swen.adventure.engine;

/**
 * Created by josephbennett on 2/10/15
 */
public interface Game {

    /**
     * @return The name to display in the title bar.
     */
    String title();

    /**
     * Set up the game with the specified width and height.
     * @param width the width of the window.
     * @param height the height of the window.
     */
    void setup(int width, int height);

    /**
     * Notifies that the size of the window has changed.
     * @param width The new width.
     * @param height The new height.
     */
    void setSize(int width, int height);

    /**
     * Notifies that the pixel dimensions of the server have changed.
     * @param width The new width in pixels.
     * @param height The new height in pixels.
     */
    void setSizeInPixels(int width, int height);

    /**
     * Called every frame to update the game.
     * @param deltaMillis The elapsed time in milliseconds.
     */
    void update(long deltaMillis);

    /**
     * @return The instance of KeyInput that the GameDelegate should pass events to.
     */
    KeyInput keyInput();

    /**
     * @return The instance of MouseInput that the GameDelegate should pass mouse press/held/released events to.
     */
    MouseInput mouseInput();

    /**
     * Called when the mouse moves.
     * @param deltaX The x delta movement.
     * @param deltaY The y delta movement.
     */
    void onMouseDeltaChange(float deltaX, float deltaY);

    /**
     * Called when the game should close.
     */
    void cleanup();
}
