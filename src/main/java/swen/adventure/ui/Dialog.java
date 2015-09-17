package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Dialog extends UIComponent {
    public static final int CONFIRM_DIALOG = 0;

    private List<UIComponent> children;

    private int x;
    private int y;
    private int width;
    private int height;

    public Dialog(PApplet a, int type) {
        super(a);

        children = new ArrayList<>();

        x = 0;
        y = 0;
        width = 500;
        height = 500;

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

    @Override
    public void drawComponent(PGraphics g) {
        g.color(100, 100, 100);
        g.fill(100, 100, 100);
        g.rect(x, y, width, height);

        for (UIComponent c : children) {
            c.draw(g);
        }
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return (x > this.x && y > this.y) && (x < this.x + this.width && y < this.y + this.height);
    }

    @Override
    protected void componentClicked(MouseEvent e) {
        for (UIComponent c : children) {
            c.mouseClicked(e);
        }
    }

    public void addChild(UIComponent c) {
        children.add(c);
    }

    public void removeChild(UIComponent c) {
        children.remove(c);
    }
}
