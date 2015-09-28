package swen.adventure;

import org.lwjgl.Sys;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.opengl.PGraphics2D;
import swen.adventure.ui.color.Color;
import swen.adventure.ui.components.Frame;
import swen.adventure.ui.components.Inventory;
import swen.adventure.ui.components.Panel;
import swen.adventure.ui.components.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.Callbacks.errorCallbackPrint;
import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AdventureGameLWJGL {

    private static final int VIRTUAL_UI_WIDTH = 800;
    private static final int VIRTUAL_UI_HEIGHT = 600;

    // Elements of the UI
    private Frame f;
    private Panel w;
    private ProgressBar health;
    private Inventory inventory;

    // We need to strongly reference callback instances.
    private GLFWErrorCallback _errorCallback;
    private GLFWKeyCallback _keyCallback;
    private GLFWWindowSizeCallback _resizeCallback;
    private GLFWFramebufferSizeCallback _framebufferSizeCallback;

    // The window handle
    private long window;

    private PGraphics2D _pGraphics;
    private AdventureGame _game;

    public void run() {
        System.out.println("Hello LWJGL " + Sys.getVersion() + "!");

        try {
            init();
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
        // Set up the UI elements
        f = new Frame(0, 0, VIRTUAL_UI_WIDTH, VIRTUAL_UI_HEIGHT);

        w = new Panel(0, 0, VIRTUAL_UI_WIDTH, VIRTUAL_UI_HEIGHT);
        w.setColor(new Color(0, 0, 0, 0));

        health = new ProgressBar(100, 100, 30, 30);
        w.addChild(health);

        inventory = new Inventory(5, 275, 500);
        inventory.setBoxSize(50);
        w.addChild(inventory);

        f.addChild(w);

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

        int WIDTH = 800;
        int HEIGHT = 600;

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, _keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop

                if (key == GLFW_KEY_1 && action == GLFW_RELEASE)
                    inventory.setSelectedItem(0);

                if (key == GLFW_KEY_2 && action == GLFW_RELEASE)
                    inventory.setSelectedItem(1);

                if (key == GLFW_KEY_3 && action == GLFW_RELEASE)
                    inventory.setSelectedItem(2);

                if (key == GLFW_KEY_4 && action == GLFW_RELEASE)
                    inventory.setSelectedItem(3);

                if (key == GLFW_KEY_5 && action == GLFW_RELEASE)
                    inventory.setSelectedItem(4);
            }
        });

        glfwSetCallback(window, _resizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(final long window, final int width, final int height) {
                _pGraphics.setSize(width, height);

            }
        });

        glfwSetCallback(window, _framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(final long window, final int width, final int height) {
                glViewport(0, 0, width, height);
                _pGraphics.setPixelDimensions(width, height);
            }
        });

        // Get the resolution of the primary monitor
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (GLFWvidmode.width(vidmode) - WIDTH) / 2,
                (GLFWvidmode.height(vidmode) - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        _pGraphics = new PGraphics2D();
        _pGraphics.setPrimary(true);
        _pGraphics.setSize(WIDTH, HEIGHT);

        _game = new AdventureGame();
    }

    private int vao = -1;

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

        String fontPath = Utilities.pathForResource("AveriaSans-Regular-16", "vlw");
        InputStream input = processing.core.PApplet.createInput(fontPath);
        PFont font = null;
        try {
            font = new PFont(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        _game.setup();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (glfwWindowShouldClose(window) == GL_FALSE ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            _game.update(16);

            if (vao == -1) {
                vao = glGenVertexArrays();
            }
            glBindVertexArray(vao);

            //

            _pGraphics.beginDraw();

            _pGraphics.textFont(font);
            f.drawComponent(_pGraphics, 1, 1);
//            _pGraphics.noStroke();
//            _pGraphics.fill(0, 255, 50, 255);
//
//            _pGraphics.fill(255, 0, 0, 255);
//            _pGraphics.rect(50, 50, 100, 100);
//
//            _pGraphics.noFill();
//            _pGraphics.strokeWeight(10.f);
//            _pGraphics.stroke(255);
//
//            _pGraphics.textFont(font, 16);
//            _pGraphics.text("Test", 100, 50);
//
//            _pGraphics.triangle(50, 50, 80, 200, 60, 400);
            _pGraphics.endDraw();

            glBindVertexArray(0);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void drawUI(PGraphics g) {

    }

    public static void main(String[] args) {
        new AdventureGameLWJGL().run();
    }

}