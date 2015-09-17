package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Dialog extends Panel {
    public static final int CONFIRM_DIALOG = 0;

    public Dialog(PApplet a, int type, int x, int y) {
        super(a, x, y);

        switch (CONFIRM_DIALOG) {
            case CONFIRM_DIALOG:
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
