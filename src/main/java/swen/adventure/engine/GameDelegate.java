/* Contributor List  */ 
 /* Thomas Roughton (roughtthom) (300313924) */ 
 /* Joseph Bennett (bennetjose) (300319773) */ 
 /* David Barnett (barnetdavi) (300313764) */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.engine;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import swen.adventure.engine.animation.AnimationSystem;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * The GameDelegate interacts with a Game object and the LWJGL context.
 */
public class GameDelegate {

    private static final int DefaultWindowWidth = 800;
    private static final int DefaultWindowHeight = 600;

    // We need to strongly reference callback instances.
    private static GLFWErrorCallback _errorCallback;
    private static GLFWKeyCallback _keyCallback;
    private static GLFWMouseButtonCallback _mouseButtonCallback;
    private static GLFWWindowSizeCallback _resizeCallback;
    private static GLFWFramebufferSizeCallback _framebufferSizeCallback;
    private static GLFWWindowFocusCallback _windowFocusCallback;

    // The _window handle
    private static long _window;

    private static int _windowWidth;
    private static int _windowHeight;

    private static boolean _mouseLocked;
    private static double _mousePrevX = 0;
    private static double _mousePrevY = 0;

    private static long _timeLastUpdate;
    private static long _elapsedTime;

    private static Game _game = null;

    /** This constructor should never be used; the class is static only. */
    private GameDelegate() {
    }

    public static void setGame(Game game) {
        _game = game;
        run();
    }

    private static void run() {
        try {
            init();
            _timeLastUpdate = System.currentTimeMillis();
            loop();
        } finally {
            cleanup();

            glfwTerminate();
            _errorCallback.release();
        }
    }

    private static void cleanup() {
        // Release _window and all callbacks except the error callback
        glfwDestroyWindow(_window);
        _keyCallback.release();
        _resizeCallback.release();
        _mouseButtonCallback.release();
        _framebufferSizeCallback.release();
        _windowFocusCallback.release();
    }

    private static void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(_errorCallback = errorCallbackPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( glfwInit() != GL11.GL_TRUE )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our _window
        glfwDefaultWindowHints(); // optional, the current _window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the _window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the _window will be resizable

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_SAMPLES, 0);
        glfwWindowHint(GLFW_SRGB_CAPABLE, GL_TRUE);

        // setup the main _window
        _windowWidth = DefaultWindowWidth;
        _windowHeight = DefaultWindowHeight;

        _window = glfwCreateWindow(_windowWidth, _windowHeight, _game.title(), NULL, NULL);

        if ( _window == NULL )
            throw new RuntimeException("Failed to create the GLFW _window");

        // Get the resolution of the primary monitor
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our _window
        glfwSetWindowPos(
                _window,
                (GLFWvidmode.width(vidmode) - _windowWidth) / 2,
                (GLFWvidmode.height(vidmode) - _windowHeight) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(_window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the _window visible
        glfwShowWindow(_window);
    }

    private static void setupCallbacks() {
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(_window, _keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long _window, int key, int scancode, int action, int mods) {

                // pass off alphabetic key input to the game
                char keyChar = (char) key;
                if (action == GLFW_PRESS) {
                    _game.keyInput().pressKey(keyChar);
                } else if (action == GLFW_RELEASE) {
                    _game.keyInput().releaseKey(keyChar);
                }

                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(_window, GL_TRUE); // we will detect this in our rendering loop
                }
            }
        });

        glfwSetCallback(_window, _resizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(final long _window, final int width, final int height) {
                _windowWidth = width;
                _windowHeight = height;

                _game.setSize(width, height);
            }
        });

        glfwSetCallback(_window, _framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(final long _window, final int width, final int height) {
                glViewport(0, 0, width, height);
                _game.setSizeInPixels(width, height);
            }
        });

        glfwSetCallback(_window, _mouseButtonCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                MouseInput.Button buttonEnum = null;

                switch(button) {
                    case GLFW_MOUSE_BUTTON_LEFT:
                        buttonEnum = MouseInput.Button.Left;
                        break;
                    case GLFW_MOUSE_BUTTON_RIGHT:
                        buttonEnum = MouseInput.Button.Right;
                        break;
                }

                if (buttonEnum != null) {
                    if (action == GLFW_PRESS) {
                        _game.mouseInput().pressButton(buttonEnum);
                    } else if (action == GLFW_RELEASE) {
                        _game.mouseInput().releaseButton(buttonEnum);
                    }
                }
            }
        });

        glfwSetCallback(_window, _windowFocusCallback = new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, int focused) {
                if(focused == GL_FALSE) {
                    unlockMouse();
                }
            }
        });
    }

    public static void pollInput() {
        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();

        _game.mouseInput().checkHeldButtons(button -> {
            int glfwValue = -1;
            switch (button) {
                case Left:
                    glfwValue = GLFW_MOUSE_BUTTON_LEFT;
                    break;
                case Right:
                    glfwValue = GLFW_MOUSE_BUTTON_RIGHT;
                    break;
            }
            return glfwGetMouseButton(_window, glfwValue) == GLFW_PRESS;
        }, _elapsedTime);

        _game.keyInput().checkHeldKeys(character -> glfwGetKey(_window, character) == GLFW_PRESS, _elapsedTime);
    }

    private static void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities(true); // valid for latest build

        GameDelegate.setupCallbacks();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        IntBuffer pixelWidth = BufferUtils.createIntBuffer(1), pixelHeight = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(_window, pixelWidth, pixelHeight);

        _game.setup(_windowWidth, _windowHeight);
        _game.setSizeInPixels(pixelWidth.get(), pixelHeight.get());

        // Run the rendering loop until the user has attempted to close
        // the _window or has pressed the ESCAPE key.
        while (glfwWindowShouldClose(_window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            handleMouseInput();

            AnimationSystem.update();

            long currentTime = System.currentTimeMillis();
            _elapsedTime = currentTime - _timeLastUpdate;
            _timeLastUpdate = currentTime;

            _game.update(_elapsedTime);

            _timeLastUpdate = currentTime;

            glfwSwapBuffers(_window); // swap the color buffers
        }

        // run clean up after main loop
        _game.cleanup();

        System.exit(0);
    }


    private static void unlockMouse() {
        glfwSetInputMode(_window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        _mouseLocked = false;
    }

    private static void lockMouse() {
        // hide mouse cursor and move cursor to centre of window
        glfwSetInputMode(_window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetCursorPos(_window, _windowWidth / 2, _windowHeight / 2);

        _mouseLocked = true;
    }

    private static void handleMouseInput() {
        if (!_mouseLocked && glfwGetMouseButton(_window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            lockMouse();
        }

        if (_mouseLocked) {
            DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

            glfwGetCursorPos(_window, x, y);
            x.rewind();
            y.rewind();

            double currentX = x.get();
            double currentY = y.get();

            double deltaX = currentX - _windowWidth/2;
            double deltaY = currentY - _windowHeight/2;

            _game.onMouseDeltaChange((float) deltaX, (float) deltaY);

            _mousePrevX = currentX;
            _mousePrevY = currentY;

            glfwSetCursorPos(_window, _windowWidth / 2, _windowHeight / 2);
        }
    }

}