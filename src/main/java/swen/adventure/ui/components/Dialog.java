package swen.adventure.ui.components;

import processing.core.PApplet;
import swen.adventure.ui.LayoutManagers.LayoutManager;
import swen.adventure.ui.clickable.ClickEvent;
import swen.adventure.ui.clickable.OnClickListener;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Dialog extends Panel {
    public static final int CONFIRM_DIALOG = 0;

    public Dialog(PApplet a, String info, int type, int x, int y) {
        super(a, x, y);

        switch (CONFIRM_DIALOG) {
            case CONFIRM_DIALOG:
                TextBox t = new TextBox(a, info, 0, 0);
                super.addChild(t);

                Button okay = new Button(applet, "Okay", 100, 100);

                okay.addClickListener(new OnClickListener() {
                    @Override
                    public void onClick(ClickEvent e) {
                        setVisible(false);
                    }
                });

                addChild(okay);
        }
    }
}
