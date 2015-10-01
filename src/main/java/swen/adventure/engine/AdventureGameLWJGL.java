package swen.adventure.engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import swen.adventure.engine.utils.SharedLibraryLoader;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AdventureGameLWJGL {

    private static final int DefaultWindowWidth = 800;
    private static final int DefaultWindowHeight = 600;

    // We need to strongly reference callback instances.
    private GLFWErrorCallback _errorCallback;
    private GLFWKeyCallback _keyCallback;
    private GLFWWindowSizeCallback _resizeCallback;
    private GLFWFramebufferSizeCallback _framebufferSizeCallback;

    // The window handle
    private long window;

    private int windowWidth;
    private int windowHeight;

    boolean mouseLocked;
    double mousePrevX = 0;
    double mousePrevY = 0;

    private long _timeLastUpdate;

    private GameInterface _game;

    public AdventureGameLWJGL(GameInterface game) {
        _game = game;
    }

    public void run() {
        try {
            init();

            _timeLastUpdate = System.currentTimeMillis();

            loop();

            // Release window and window callbacks
            glfwDestroyWindow(window);
            _keyCallback.release();
        } finally {
            // Terminate GLFW and release the GLFWerrorfun
            glfwTerminate();
            _errorCallback.release();
        }
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(_errorCallback = errorCallbackPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( glfwInit() != GL11.GL_TRUE )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_SAMPLES, 8);
        glfwWindowHint(GLFW_SRGB_CAPABLE, GL_TRUE);

        // setup the main window
        windowWidth = DefaultWindowWidth;
        windowHeight = DefaultWindowHeight;

        window = glfwCreateWindow(windowWidth, windowHeight, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, _keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {

                // pass off alphabetic key input to the game
                char keyChar = (char) key;
                if (action == GLFW_PRESS) {
                    _game.keyInput().pressKey(keyChar);
                } else if (action == GLFW_RELEASE) {
                    _game.keyInput().releaseKey(keyChar);
                }

                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, GL_TRUE); // we will detect this in our rendering loop
                }
            }
        });

        glfwSetCallback(window, _resizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(final long window, final int width, final int height) {
                windowWidth = width;
                windowHeight = height;

                _game.setSize(width, height);
            }
        });

        glfwSetCallback(window, _framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(final long window, final int width, final int height) {
                glViewport(0, 0, width, height);
                _game.setSizeInPixels(width, height);
            }
        });

        // Get the resolution of the primary monitor
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (GLFWvidmode.width(vidmode) - windowWidth) / 2,
                (GLFWvidmode.height(vidmode) - windowHeight) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        _game = new AdventureGame();
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        // GL.createCapabilities(true); // valid for latest build
        //GLContext.createFromCurrent(); // use this line instead with the 3.0.0a build

        GLContext.createFromCurrent();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        _game.setup(windowWidth, windowHeight);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            handleMouseInput();

            long currentTime = System.currentTimeMillis();

            _game.update(currentTime - _timeLastUpdate);

            _timeLastUpdate = currentTime;

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }
    private void handleMouseInput() {

        if (!mouseLocked && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            // hide mouse cursor and move cursor to centre of window
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            glfwSetCursorPos(window, windowWidth / 2, windowHeight / 2);

            mouseLocked = true;
        }

        if (mouseLocked) {
            DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

            glfwGetCursorPos(window, x, y);
            x.rewind();
            y.rewind();

            double currentX = x.get();
            double currentY = y.get();

            double deltaX = currentX - windowWidth/2;
            double deltaY = currentY - windowHeight/2;

            _game.onMouseDeltaChange((float) deltaX, (float) deltaY);

            mousePrevX = currentX;
            mousePrevY = currentY;

            glfwSetCursorPos(window, windowWidth / 2, windowHeight / 2);
        }
    }

    public static void main(String[] args) {
        SharedLibraryLoader.load();
        new AdventureGameLWJGL().run();
    }

}