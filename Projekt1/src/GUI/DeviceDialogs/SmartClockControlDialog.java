package GUI;

import Devices.SmartClock;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter; // For displaying time consistently

public class SmartClockControlDialog extends JDialog {
    private SmartClock smartClock;

    private JLabel statusLabel;
    private JLabel simulationStatusLabel;
    private JLabel currentTimeLabel;
    private JLabel currentDateLabel;

    private JButton turnOnButton;
    private JButton turnOffButton;
    private JButton startSimulationButton;
    private JButton stopSimulationButton;

    // Formatter for displaying time
    private final DateTimeFormatter timeDisplayFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateDisplayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public SmartClockControlDialog(JFrame parent, SmartClock smartClock) {
        super(parent, "Control SmartClock: " + smartClock.getName(), true);
        this.smartClock = smartClock;
        setLayout(new GridBagLayout());
        setSize(850, 850);
        setLocationRelativeTo(parent);

        initComponents();
        createLayout();
        updateDisplay(); // Initial display update

        // Timer to refresh the display periodically (e.g., every 500ms)
        Timer refreshTimer = new Timer(500, e -> updateDisplay());
        refreshTimer.start();
    }

    private void initComponents() {
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font timeFont = new Font("Monospaced", Font.BOLD, 24); // Larger font for time
        Font controlFont = new Font("Arial", Font.PLAIN, 16);

        statusLabel = new JLabel();
        statusLabel.setFont(labelFont);

        simulationStatusLabel = new JLabel();
        simulationStatusLabel.setFont(labelFont);

        currentTimeLabel = new JLabel();
        currentTimeLabel.setFont(timeFont);
        currentTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        currentDateLabel = new JLabel();
        currentDateLabel.setFont(labelFont);
        currentDateLabel.setHorizontalAlignment(SwingConstants.CENTER);


        // Power control buttons
        turnOnButton = new JButton("Turn ON");
        turnOnButton.setFont(controlFont);
        turnOnButton.addActionListener(e -> {
            try {
                smartClock.turnOn();
                updateDisplay();
                JOptionPane.showMessageDialog(this, "SmartClock turned ON.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (AlreadyON ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        turnOffButton = new JButton("Turn OFF");
        turnOffButton.setFont(controlFont);
        turnOffButton.addActionListener(e -> {
            try {
                smartClock.turnOff();
                updateDisplay();
                JOptionPane.showMessageDialog(this, "SmartClock turned OFF.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (AlreadyOFF ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Simulation control buttons
        startSimulationButton = new JButton("Start Simulation");
        startSimulationButton.setFont(controlFont);
        startSimulationButton.addActionListener(e -> {
            try {
                smartClock.simulate();
                updateDisplay();
                JOptionPane.showMessageDialog(this, "SmartClock simulation started.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (DeviceDisabled | SimulationInterrupted ex) {
                JOptionPane.showMessageDialog(this, "Simulation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        stopSimulationButton = new JButton("Stop Simulation");
        stopSimulationButton.setFont(controlFont);
        stopSimulationButton.addActionListener(e -> {
            smartClock.stopSimulation();
            updateDisplay();
            JOptionPane.showMessageDialog(this, "SmartClock simulation stopped.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose());
    }

    private void createLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Status labels
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        add(statusLabel, gbc);
        gbc.gridy = row++;
        add(simulationStatusLabel, gbc);

        // Time and Date
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.BOTH; // Fill both horizontally and vertically
        gbc.weightx = 1.0; gbc.weighty = 1.0; // Allow it to expand
        add(currentTimeLabel, gbc);
        gbc.gridy = row++;
        add(currentDateLabel, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill
        gbc.weightx = 0.0; gbc.weighty = 0.0; // Reset weight

        // Power control buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        add(turnOnButton, gbc);
        gbc.gridx = 1;
        add(turnOffButton, gbc);
        row++;

        // Simulation control buttons
        gbc.gridx = 0; gbc.gridy = row;
        add(startSimulationButton, gbc);
        gbc.gridx = 1;
        add(stopSimulationButton, gbc);
        row++;

        // Close Button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(closeButton, gbc);
    }

    private void updateDisplay() {
        // Update status labels
        statusLabel.setText("Status: " + smartClock.getStatus());
        simulationStatusLabel.setText("Simulation: " + (smartClock.isSimulating() ? "Running" : "Stopped"));

        // Update time and date
        currentTimeLabel.setText(smartClock.getTime().format(timeDisplayFormatter));
        currentDateLabel.setText(smartClock.getDate().format(dateDisplayFormatter));

        // Enable/disable buttons based on clock status and simulation
        boolean isOn = smartClock.isOn();
        boolean isSimulating = smartClock.isSimulating();

        turnOnButton.setEnabled(!isOn);
        turnOffButton.setEnabled(isOn);

        startSimulationButton.setEnabled(isOn && !isSimulating);
        stopSimulationButton.setEnabled(isSimulating);
    }
}