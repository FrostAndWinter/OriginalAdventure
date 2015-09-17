package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

/**
 * Created by danielbraithwt on 9/15/15.
 */
public class UI extends PApplet {

    private Frame f;
    private Button b;

    @Override
    public void setup() {
        super.setup();

        f = new Frame(this);
        b = new Button(this, "TEST", 50, 50);

        b.addClickListener(new OnClickListener() {
            @Override
            public void onClick(ClickEvent e) {
                System.out.println("Clicked");
            }
        });

        f.addChild(b);
    }

    @Override
    public void draw() {
        super.draw();

        background(0);

        PGraphics g = getGraphics();
        f.draw(g);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);

        f.mouseClicked(event);
    }

    public void settings() {
        size(800, 600, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"--present", "swen.adventure.ui.UI"});
    }
}
