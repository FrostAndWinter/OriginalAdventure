package swen.adventure.game.ui.components;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.opengl.PGL;
import swen.adventure.engine.Action;
import swen.adventure.engine.Event;
import swen.adventure.engine.Input;
import swen.adventure.engine.animation.AnimableProperty;
import swen.adventure.engine.animation.Animation;
import swen.adventure.engine.animation.AnimationCurve;
import swen.adventure.engine.rendering.GLForwardRenderer;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.components.Frame;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.ui.components.Reticule;
import swen.adventure.game.scenenodes.Player;
import swen.adventure.game.scenenodes.Region;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by danielbraithwt on 10/10/15.
 *
 * Stores all the UI components to move creating the UI out of our main class.
 */
public class UI extends Frame {
    public static final Action<Input, Input, UI> actionToggleControlls = (input, ignored, ui, data) -> {
        ui.setControlsOverlayVisibility(!ui.getControlsOverlayVisibility());
    };

    private AnimableProperty roomNameOpacity = new AnimableProperty(0.f);

    private Panel _container;
    private InventoryComponent _inventory;
    private ControlsOverlay _controlsOverlay;
    private ArrayList<String> _tooltips;
    private Reticule _reticule;
    private String roomName;

    public UI(int w, int h, Player p) {
        super(0, 0, w, h);

        Event.EventSet<Region, Player> regionEnteredEvents = (Event.EventSet<Region, Player>)Event.eventSetForName("RegionEntered");
        regionEnteredEvents.addAction(this, (region, player, listener, data) -> {
            if (player.equals(p)) {
                listener.setRoomName(region.regionName);
            }
        });

        roomName = "";

        _tooltips = new ArrayList<>();

        _container = new Panel(x,y,w,h);
        _container.setColor(new Color(0, 0, 0, 0));

        // Create the reticule so the user can see where they are looking
        int size = 5;
        _reticule = new Reticule(width/2, height/2, size);
        _container.addChild(_reticule);

        // Set up the inventory
        _inventory = new InventoryComponent(p.inventory(), 275, 500);
        _inventory.setBoxSize(50);
        _container.addChild(_inventory);

        _controlsOverlay = new ControlsOverlay(0,0);
        _controlsOverlay.setVisible(false);
        _container.addChild(_controlsOverlay);

        addChild(_container);
    }

    /**
     * Sets the name of the room the to display on the
     * UI and starts an animation to fade it out
     *
     * @param name name of room to show
     */
    public void setRoomName(String name) {
        roomName = name;

        roomNameOpacity.stopAnimating();
        roomNameOpacity.setValue(1.f);
        new Animation(roomNameOpacity, AnimationCurve.Linear, 2.f, 0.f);
    }

    /**
     * List of strings that will be displayed just under the reticule
     * in the center of the screen.
     * @param tips list of strings
     */
    public void setTooltip(ArrayList<String> tips) {
        removeTooltip();

        if (tips == null) {
            return;
        }

        _tooltips.addAll(tips);
    }

    /**
     * Clears all the tooltips
     */
    public void removeTooltip() {
        _tooltips.clear();
    }

    /**
     * Changes the visibility of the overlay which displays
     * all the game keyboard controls
     * @param b true if we want to display the overlay
     */
    public void setControlsOverlayVisibility(boolean b) {
        _controlsOverlay.setVisible(b);
    }

    /**
     * @return true if the controls overlay is visible
     */
    public boolean getControlsOverlayVisibility() {
        return _controlsOverlay.getVisible();
    }

    public InventoryComponent getInventory() {
        return _inventory;
    }

    /**
     * Handles drawing the UI
     * @param pg pGraphics to draw all the UI components to
     * @param gr GLRenderer to draw the inventory 3D objects to
     */
    public void drawUI(PGraphics pg, GLRenderer gr) {
        float scaleX = pg.width / (float) width;
        float scaleY = pg.height / (float) height;
        float scale = Math.min(scaleX, scaleY);

        float dw = (pg.width - (scale * width))/2;
        float dh = (pg.height - (scale * height))/2;

        pg.beginDraw();

        pg.noStroke();
        pg.translate(dw, dh);

        // Draw the components
        draw(pg, scale, scale);

        // Draw the room name
        pg.fill(255, 255, 255, 255 * roomNameOpacity.value());
        pg.text(roomName, (width * scale) / 2.0f - pg.textWidth(roomName)/2.0f, 20 + pg.textDescent() + pg.textAscent());

        // Draw the tool tips
        pg.fill(255);
        float tooltipX = width/2f;
        float tooltipY = height/2f + _reticule.getWidth(pg)* 2;

        float h = pg.textAscent() + pg.textDescent();
        for (String s : _tooltips) {
            float w = pg.textWidth(s);

            pg.text(s, tooltipX * scale - w/2, tooltipY * scale);
            tooltipY += h;
        }

        pg.endDraw();

        _inventory.drawItems(gr, scale, scale, dw, dh, pg.width, pg.height);
    }
}