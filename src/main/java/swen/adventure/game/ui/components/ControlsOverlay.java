package swen.adventure.game.ui.components;

import swen.adventure.engine.ui.color.Color;
import swen.adventure.engine.ui.components.Panel;
import swen.adventure.engine.ui.components.TextBox;
import swen.adventure.engine.ui.layoutmanagers.LinearLayout;

/**
 * Created by danielbraithwt on 10/10/15.
 */
public class ControlsOverlay extends Panel {
    public ControlsOverlay(int x, int y) {
        super(x, y);
        setColor(new Color(0, 0, 0, 100));
        setLayoutManager(new LinearLayout(LinearLayout.LINEAR_LAYOUT_VERTICAL));

        TextBox moveFoward = new TextBox("W - move foward", 0, 0);
        addChild(moveFoward);

        TextBox moveLeft = new TextBox("A - move left", 0, 0);
        addChild(moveLeft);

        TextBox moveBack = new TextBox("S - move backwards", 0, 0);
        addChild(moveBack);

        TextBox moveRight = new TextBox("D - move right", 0, 0);
        addChild(moveRight);

        TextBox placeItem = new TextBox("U - place item", 0, 0);
        addChild(placeItem);

        TextBox takeItem = new TextBox("E - take item", 0, 0);
        addChild(takeItem);
    }
}
