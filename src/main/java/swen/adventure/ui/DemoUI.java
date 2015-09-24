package swen.adventure.ui;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;
import swen.adventure.Utilities;
import swen.adventure.ui.clickable.ClickEvent;
import swen.adventure.ui.clickable.OnClickListener;
import swen.adventure.ui.color.*;
import swen.adventure.ui.color.Color;
import swen.adventure.ui.components.*;
import swen.adventure.ui.components.Button;
import swen.adventure.ui.components.Dialog;
import swen.adventure.ui.components.Frame;
import swen.adventure.ui.components.Panel;

import java.awt.*;

/**
 * Created by danielbraithwt on 9/17/15.
 */
public class DemoUI extends PApplet {


    public static final float VIRTUAL_WIDTH = 1920;
    public static final float VIRTUAL_HEIGHT = 1080;

    public static final float ACTUAL_WIDTH = 1920;
    public static final float ACTUAL_HEIGHT = 1080;

    public static final float SCALEX = ACTUAL_WIDTH / VIRTUAL_WIDTH;
    public static final float SCALEY = ACTUAL_HEIGHT / VIRTUAL_HEIGHT;

    private Frame f;

    // UIComponents for start game menu
    private Panel start;
    private Button startGame;
    private Button quitGame;
    private ImageView background;


    // UIComponents For Actual Screen
    private Panel game;
    private ProgressBar health;
    private Inventory inventory;

    @Override
    public void setup() {
        super.setup();

        PImage im = loadImage(Utilities.pathForResource("background", "png"));

        f = new Frame(this, 0, 0, (int) VIRTUAL_WIDTH, (int) VIRTUAL_HEIGHT);

        ImageView background = new ImageView(this, im, 0, 0, (int) VIRTUAL_WIDTH, (int) VIRTUAL_HEIGHT);
        f.addChild(background);

        Dialog exit = new Dialog(DemoUI.this, "TEST", Dialog.CONFIRM_DIALOG, 300, 300);
        exit.addDialogCloseListener(new Dialog.DialogCloseListener() {
            @Override
            public void onDialogClose(Dialog.DialogCloseEvent e) {
                System.out.println("Test");
            }
        });
        exit.setVisible(false);
        f.addChild(exit);

        start = new Panel(this ,860, 600);
        start.setColor(new Color(255f, 255f, 255f, 0.3f));
        startGame = new Button(this, "Start!", 50, 50, 200, 100);
        startGame.setFont(createFont("Arial", 30, true));
        startGame.setColor(new Color(255, 255, 255, 200));
        startGame.addClickListener(new OnClickListener() {
            @Override
            public void onClick(ClickEvent e) {
                game.setVisible(true);
                start.setVisible(false);
                background.setVisible(false);
                //f.removeChild(start);
                //f.removeChild(background);
            }
        });
        start.addChild(startGame);


        f.addChild(start);


        game = new Panel(this, 0, 0, (int) VIRTUAL_WIDTH, (int) VIRTUAL_HEIGHT);
        game.setVisible(false);

        health = new ProgressBar(this, 100, 100, 1600, 25);
        game.addChild(health);

        inventory = new Inventory(this, 10, 650, 950);
        inventory.setBoxSize(60);
        game.addChild(inventory);

        f.addChild(game);
    }

    @Override
    public void draw() {
        super.draw();

        background(255);

        f.draw(g, SCALEX, SCALEY);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);

        f.mouseClicked(event.getX(), event.getY(), SCALEX, SCALEY);
    }

    public void settings() {
        size((int) ACTUAL_WIDTH, (int) ACTUAL_HEIGHT, P3D);
    }

    public static void main(String[] args) {
        PApplet.main(new String[]{"--present", "swen.adventure.ui.DemoUI"});
    }
}
