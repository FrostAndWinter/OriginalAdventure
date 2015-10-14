package swen.adventure.game;

import oracle.jrockit.jfr.JFR;
import swen.adventure.engine.GameDelegate;
import swen.adventure.engine.Utilities;
import swen.adventure.engine.network.Client;
import swen.adventure.engine.network.DumbClient;
import swen.adventure.engine.network.EventBox;
import swen.adventure.engine.network.NetworkClient;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by danielbraithwt on 10/14/15.
 */
public class LauncherGUI extends JFrame {

    private JLabel nameLabel;
    private JTextField nameTextField;

    private JLabel serverAddressLabel;
    private JTextField serverAddressTextField;

    private JLabel serverPortLabel;
    private JTextField serverPortTextField;

    private JButton start;

    public LauncherGUI() {
        super("Origonal Adventure Launcher");
        JPanel input = new JPanel();

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BorderLayout());
        nameLabel = new JLabel("Name");
        namePanel.add(nameLabel, BorderLayout.NORTH);

        nameTextField = new JTextField();
        nameTextField.setPreferredSize(new Dimension(150, 20));
        namePanel.add(nameTextField, BorderLayout.CENTER);

        input.add(namePanel);

        JPanel serverAddressPanel = new JPanel();
        serverAddressPanel.setLayout(new BorderLayout());
        serverAddressLabel = new JLabel("Server Address");
        serverAddressPanel.add(serverAddressLabel, BorderLayout.NORTH);

        serverAddressTextField = new JTextField();
        serverAddressTextField.setPreferredSize(new Dimension(150, 20));
        serverAddressPanel.add(serverAddressTextField, BorderLayout.CENTER);

        JPanel serverPortPanel = new JPanel();
        serverPortPanel.setLayout(new BorderLayout());
        serverPortLabel = new JLabel("Server Port");
        serverPortPanel.add(serverPortLabel, BorderLayout.NORTH);

        serverPortTextField = new JTextField();
        serverPortTextField.setPreferredSize(new Dimension(150, 20));
        serverPortPanel.add(serverPortTextField, BorderLayout.CENTER);

        start = new JButton("Start Game");
        start.addActionListener(e -> {
            startGame(true);
        });

        JButton startSingle = new JButton("Start Single Player");
        startSingle.addActionListener(e -> {
            startGame(false);
        });

        input.add(serverAddressPanel);
        input.add(serverPortPanel);
        input.add(start);
        input.add(startSingle);

        setLayout(new BorderLayout());

        add(input, BorderLayout.CENTER);
        //add(start, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(200, 180));

        setVisible(true);
    }

    public void startGame(boolean n) {
        String[] args;
        if (n) {
            args = new String[] {nameTextField.getText(), serverAddressTextField.getText(),
                                serverPortTextField.getText()};
        } else {
            args = new String[] {};
        }

        setVisible(false);

        AdventureGame.startGame(args);
    }

    public static void main(String[] args) {
        LauncherGUI l = new LauncherGUI();
    }
}
