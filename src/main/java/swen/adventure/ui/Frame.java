package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class Frame extends Component {

    private List<Component> children;

    public Frame(PApplet p) {
        super(p);

        children = new ArrayList<>();
    }

    @Override
    public void draw(PGraphics g) {
        for (Component c : children) {
            c.draw(g);
        }
    }

    public void addChild(Component c) {
        children.add(c);
    }

    public void removeChild(Component c) {
        children.remove(c);
    }

    public void mouseClicked(MouseEvent e) {
        for (Component c : children) {
            c.mouseClicked(e);
        }
    }

    @Override
    public boolean withinBounds(int x, int y) {
        return true;
    }
}
