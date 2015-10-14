/* Contributor List  */ 
 /* Daniel Braithwaite (braithdani) (300313770) */ 
 package swen.adventure.game;

import swen.adventure.engine.Utilities;

import javax.swing.*;
import java.awt.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

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
        super("Original Adventure Launcher");
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

        JButton startServer = new JButton("Start Server");
        startServer.addActionListener(e -> {
            // Ensure the port is a number
            try {
                int num = Integer.parseInt(serverPortTextField.getText());
            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(null, "Port number must be a number");
                return;
            }

            setVisible(false);

            Utilities.isHeadlessMode = true;

            new MultiPlayerServer(Integer.parseInt(serverPortTextField.getText()), "SceneGraph").run();
        });

        input.add(serverAddressPanel);
        input.add(serverPortPanel);
        input.add(start);
        input.add(startSingle);
        input.add(startServer);

        setLayout(new BorderLayout());

        add(input, BorderLayout.CENTER);
        setMinimumSize(new Dimension(200, 250));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Starts a new game by calling the start game method in
     * the adventure game class. Will also tell user
     * if the server cant be connected to
     *
     * @param n true if game should connect to a server
     */
    private void startGame(boolean n) {
        Optional<String> inputError = validateInputData();

        if (n && inputError.isPresent()) {
            JOptionPane.showMessageDialog(this, inputError.get());
            return;
        }

        String[] args;
        if (n) {
            args = new String[] {nameTextField.getText(), serverAddressTextField.getText(),
                                serverPortTextField.getText()};
        } else {
            args = new String[] {};
        }

        setVisible(false);

        try {
            AdventureGame.startGame(args);
        } catch (InvalidServerConfig e) {
            JOptionPane.showMessageDialog(null, "Could not find server");
            new Thread(() -> setVisible(true)).start();
        }
    }

    /**
     * Makes sure the data the user has entered is valid
     * will give an error message if the data isnt valid
     * @return String optional, if string present error occurred
     */
    private Optional<String> validateInputData() {
        String error = "";

        // Ensure that the name isnt empty
        if (nameTextField.getText().trim().equals("")) {
            error += "Name cant be empty\n";
        }

        // Ensure the port is a number
        try {
            int num = Integer.parseInt(serverPortTextField.getText());
        } catch (NumberFormatException e) {
            error += "Port must be a number\n";
        }

        // Ensure the ip address is valid
        try {
            InetAddress inet = Inet4Address.getByName(serverAddressTextField.getText());
            if (!inet.getHostAddress().equals(serverAddressTextField.getText()) || !(inet instanceof Inet4Address)) {
                error += "IP Address Isnt Valid\n";
            }
        } catch (UnknownHostException e) {
            error += "IP Address Isnt Valid\n";
        }

        return Optional.ofNullable(error.equals("") ? null : error);
    }

    public static void main(String[] args) {
        LauncherGUI l = new LauncherGUI();
    }
}