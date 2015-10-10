package swen.adventure.game.ui.components;

import processing.core.PGraphics;
import swen.adventure.engine.rendering.GLRenderer;
import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.components.Frame;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.ui.components.Reticule;
import swen.adventure.game.scenenodes.Inventory;
import swen.adventure.game.scenenodes.Player;

/**
 * Created by danielbraithwt on 10/10/15.
 */
public class UI extends Frame {
    Panel _container;
    InventoryComponent _inventory;
    ControlsOverlay _controlsOverlay;

    public UI(int w, int h, Player p) {
        super(0, 0, w, h);

        _container = new Panel(x,y,w,h);
        _container.setColor(new Color(0,0,0,0));

        // Create the reticule so the user can see where they are looking
        int size = 5;
        Reticule reticule = new Reticule(width/2, height/2, size);
        _container.addChild(reticule);

        // Set up the inventory
        _inventory = new InventoryComponent(p.inventory(), 275, 500);
        _inventory.setBoxSize(50);
        _container.addChild(_inventory);

        _controlsOverlay = new ControlsOverlay(0,0);
        _controlsOverlay.setVisible(false);

        addChild(_container);
    }

    public void setControlsOverlayVisibility(boolean b) {
        _controlsOverlay.setVisible(b);
    }

    public InventoryComponent getInventory() {
        return _inventory;
    }

    public void drawUI(PGraphics pg, GLRenderer gr) {
        float scaleX = pg.width / width;
        float scaleY = pg.height / height;
        float scale = Math.min(scaleX, scaleY);

        float dw = (pg.width - (scale * width))/2;
        float dh = (pg.height - (scale * height))/2;

        pg.beginDraw();
        pg.noStroke();
        pg.translate(dw, dh);

        draw(pg, scale, scale);

        pg.endDraw();
        
        _inventory.drawItems(gr, scale, scale, dw, dh, pg.width, pg.height);
    }
}
