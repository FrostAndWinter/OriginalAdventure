package swen.adventure.game.ui.components;

import processing.core.PGraphics;
import processing.opengl.PGL;
import swen.adventure.engine.Action;
import swen.adventure.engine.Input;
import swen.adventure.engine.rendering.GLForwardRenderer;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.components.Frame;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.ui.components.Reticule;
import swen.adventure.game.scenenodes.Player;

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

    private Panel _container;
    private InventoryComponent _inventory;
    private ControlsOverlay _controlsOverlay;
    private ArrayList<String> _tooltips;
    private Reticule _reticule;

    public UI(int w, int h, Player p) {
        super(0, 0, w, h);

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