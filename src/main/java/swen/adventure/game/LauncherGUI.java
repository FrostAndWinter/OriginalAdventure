package swen.adventure.game;

import oracle.jrockit.jfr.JFR;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Created by danielbraithwt on 10/14/15.
 */
public class LauncherGUI extends JFrame {

    private JLabel nameLabel;
    private JTextField nameTextField;

    private JLabel serverAddressLabel;
    private JTextField serverAddressTextField;

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

        start = new JButton("Start Game");

        input.add(serverAddressPanel);
        input.add(start);

        setLayout(new BorderLayout());

        add(input, BorderLayout.CENTER);
        //add(start, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(200, 180));

        setVisible(true);
    }

    public static void main(String[] args) {
        new LauncherGUI();
    }
}
