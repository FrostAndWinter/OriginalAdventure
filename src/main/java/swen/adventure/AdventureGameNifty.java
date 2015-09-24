package swen.adventure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.builder.EffectBuilder;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.render.batch.BatchRenderDevice;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglBatchRenderBackendCoreProfileFactory;
import de.lessvoid.nifty.screen.DefaultScreenController;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.lessvoid.nifty.tools.Color;
import de.lessvoid.nifty.tools.SizeValue;

import static org.lwjgl.opengl.GL11.*;


public class AdventureGameNifty {
  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;

  private static AdventureGame _game;

  private static long _timeLastUpdate;

  public static void main(final String[] args) throws Exception {
    SharedLibraryLoader.load();
    initLWJGL();
    initGL();
    _game = new AdventureGame();

    _timeLastUpdate = GetTime();

    _game.setup();

    // blank out cursor.
//    Cursor emptyCursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
//    Mouse.setNativeCursor(emptyCursor);
//
    LwjglInputSystem inputSystem = initInput();

    Nifty nifty = initNifty(inputSystem);
    nifty.loadStyleFile("nifty-default-styles.xml");
    nifty.loadControlFile("nifty-default-controls.xml");
    createIntroScreen(nifty, new MyScreenController());

    nifty.gotoScreen("start");

    renderLoop(nifty);

    shutDown(inputSystem);
  }

  /**
   * Get the time in milliseconds
   *
   * @return The system time in milliseconds
   */
  private static long GetTime() {
    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
  }

  private static LwjglInputSystem initInput() throws Exception {
    LwjglInputSystem inputSystem = new LwjglInputSystem();
    inputSystem.startup();
    return inputSystem;
  }

  private static void initLWJGL() throws Exception {
    DisplayMode currentMode = Display.getDisplayMode();
    DisplayMode[] modes = Display.getAvailableDisplayModes();
    List<DisplayMode> matching = new ArrayList<DisplayMode>();
    for (int i=0; i<modes.length; i++) {
      DisplayMode mode = modes[i];
      if (mode.getWidth() == WIDTH &&
          mode.getHeight() == HEIGHT &&
          mode.getBitsPerPixel() == 32 ) {
        matching.add(mode);
      }
    }

    DisplayMode[] matchingModes = matching.toArray(new DisplayMode[0]);
    boolean found = false;
    for (int i=0; i<matchingModes.length; i++) {
      if (matchingModes[i].getFrequency() == currentMode.getFrequency()) {
        Display.setDisplayMode(matchingModes[i]);
        found = true;
        break;
      }
    }

    if (!found) {
      Arrays.sort(matchingModes, (o1, o2) -> {
        if (o1.getFrequency() > o2.getFrequency()) {
          return 1;
        } else if (o1.getFrequency() < o2.getFrequency()) {
          return -1;
        } else {
          return 0;
        }
      });

      for (int i=0; i<matchingModes.length; i++) {
        Display.setDisplayMode(matchingModes[i]);
        break;
      }
    }

    int x = (currentMode.getWidth() - Display.getDisplayMode().getWidth()) / 2;
    int y = (currentMode.getHeight() - Display.getDisplayMode().getHeight()) / 2;
    Display.setLocation(x, y);
    Display.setFullscreen(false);
    Display.create(new PixelFormat(), new ContextAttribs(3, 3).withProfileCore(true));
    Display.setVSyncEnabled(true);
    Display.setTitle("Hello Nifty");
  }

  private static void initGL() {
    glViewport(0, 0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    glClear(GL11.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL11.GL_BLEND);
    glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
  }

  private static Nifty initNifty(final LwjglInputSystem inputSystem) throws Exception {
    return new Nifty(
        new BatchRenderDevice(LwjglBatchRenderBackendCoreProfileFactory.create()),
        new NullSoundDevice(),
        inputSystem,
        new AccurateTimeProvider());
  }

  private static Screen createIntroScreen(final Nifty nifty, final ScreenController controller) {
    return new ScreenBuilder("start") {{
      controller(controller);
      layer(new LayerBuilder("layer") {{
        childLayoutCenter();
        onStartScreenEffect(new EffectBuilder("fade") {{
          length(500);
          effectParameter("start", "#0");
          effectParameter("end", "#f");
        }});
        onEndScreenEffect(new EffectBuilder("fade") {{
          length(500);
          effectParameter("start", "#f");
          effectParameter("end", "#0");
        }});

        panel(new PanelBuilder() {{
          childLayoutVertical();
          text(new TextBuilder() {{
            text("Nifty 1.4 Core Hello World");
            style("base-font");
            color(Color.WHITE);
            alignCenter();
            valignCenter();
          }});
          panel(new PanelBuilder(){{
            height(SizeValue.px(10));
          }});
          control(new ButtonBuilder("exit", "Pretty Cool Yay!") {{
            alignCenter();
            valignCenter();
          }});
        }});
      }});
    }}.build(nifty);
  }

  private static void renderLoop(final Nifty nifty) {
    boolean done = false;

    while (!Display.isCloseRequested() && !done) {

      Display.update();
      if (nifty.update()) {
        done = true;
      }

      long currentTime = GetTime();

      _game.update(currentTime - _timeLastUpdate);

      _timeLastUpdate = currentTime;

      nifty.render(false);

      int error = GL11.glGetError();
      if (error != GL11.GL_NO_ERROR) {
//        String glerrmsg = GLU.gluErrorString(error);
//        System.err.println(glerrmsg);
      }
    }
  }

  private static void shutDown(final LwjglInputSystem inputSystem) {
    inputSystem.shutdown();
    Display.destroy();
    System.exit(0);
  }

  public static class MyScreenController extends DefaultScreenController {
    @NiftyEventSubscriber(id="exit")
    public void exit(final String id, final ButtonClickedEvent event) {
      nifty.exit();
    }
  }
}