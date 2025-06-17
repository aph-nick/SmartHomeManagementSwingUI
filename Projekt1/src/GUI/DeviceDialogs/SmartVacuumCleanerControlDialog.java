package GUI;

import Devices.Outlet;
import Devices.SmartVacuumCleaner;
import Enums.SmartVacuumMode;
import Enums.Status;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;

import javax.swing.*;
import java.awt.*;
import java.util.List; // To get a list of available Outlets

public class SmartVacuumCleanerControlDialog extends JDialog {
    private SmartVacuumCleaner vacuum;
    private List<Outlet> allOutlets; // List of all available Outlets in the system

    private JLabel statusLabel;
    private JLabel simulationStatusLabel;
    private JLabel batteryLabel;
    private JLabel modeLabel;
    private JLabel chargingStatusLabel;
    private JLabel connectedOutletLabel;

    private JButton turnOnButton;
    private JButton turnOffButton;
    private JButton startCleaningButton;
    private JButton stopCleaningButton;
    private JComboBox<SmartVacuumMode> modeComboBox;
    private JComboBox<Outlet> outletComboBox; // To select an outlet
    private JButton connectOutletButton;
    private JButton disconnectOutletButton;

    // Constructor without outlet list (less flexible, but works for basic testing)
    public SmartVacuumCleanerControlDialog(JFrame parent, SmartVacuumCleaner vacuum) {
        this(parent, vacuum, new java.util.ArrayList<>()); // Pass an empty list
    }

    // Preferred constructor: takes a list of all outlets
    public SmartVacuumCleanerControlDialog(JFrame parent, SmartVacuumCleaner vacuum, List<Outlet> allOutlets) {
        super(parent, "Control Smart Vacuum Cleaner: " + vacuum.getName(), true);
        this.vacuum = vacuum;
        this.allOutlets = allOutlets;
        setLayout(new GridBagLayout());
        setSize(850, 850);
        setLocationRelativeTo(parent);

        initComponents();
        createLayout();
        updateDisplay(); // Initial display update

        // Timer to refresh the display periodically
        Timer refreshTimer = new Timer(500, e -> updateDisplay());
        refreshTimer.start();
    }

    private void initComponents() {
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font controlFont = new Font("Arial", Font.PLAIN, 16);

        statusLabel = new JLabel();
        statusLabel.setFont(labelFont);

        simulationStatusLabel = new JLabel();
        simulationStatusLabel.setFont(labelFont);

        batteryLabel = new JLabel();
        batteryLabel.setFont(labelFont);

        modeLabel = new JLabel();
        modeLabel.setFont(labelFont);

        chargingStatusLabel = new JLabel();
        chargingStatusLabel.setFont(labelFont);

        connectedOutletLabel = new JLabel();
        connectedOutletLabel.setFont(labelFont);

        // Power control buttons
        turnOnButton = new JButton("Turn ON");
        turnOnButton.setFont(controlFont);
        turnOnButton.addActionListener(e -> {
            try {
                vacuum.turnOn();
                updateDisplay();
                JOptionPane.showMessageDialog(this, "Vacuum cleaner turned ON.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (AlreadyON ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        turnOffButton = new JButton("Turn OFF");
        turnOffButton.setFont(controlFont);
        turnOffButton.addActionListener(e -> {
            try {
                vacuum.turnOff();
                updateDisplay();
                JOptionPane.showMessageDialog(this, "Vacuum cleaner turned OFF.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (AlreadyOFF ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cleaning simulation buttons
        startCleaningButton = new JButton("Start Cleaning");
        startCleaningButton.setFont(controlFont);
        startCleaningButton.addActionListener(e -> {
            try {
                vacuum.simulate();
                updateDisplay();
                JOptionPane.showMessageDialog(this, "Vacuum cleaner started cleaning.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (DeviceDisabled | SimulationInterrupted ex) {
                JOptionPane.showMessageDialog(this, "Cleaning failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        stopCleaningButton = new JButton("Stop Cleaning");
        stopCleaningButton.setFont(controlFont);
        stopCleaningButton.addActionListener(e -> {
            vacuum.stopSimulation();
            updateDisplay();
            JOptionPane.showMessageDialog(this, "Cleaning stopped.", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        // Mode selection
        modeComboBox = new JComboBox<>(SmartVacuumMode.values());
        modeComboBox.setFont(controlFont);
        modeComboBox.setSelectedItem(vacuum.getMode());
        modeComboBox.addActionListener(e -> {
            SmartVacuumMode selectedMode = (SmartVacuumMode) modeComboBox.getSelectedItem();
            if (selectedMode != null) {
                vacuum.setMode(selectedMode);
                updateDisplay();
            }
        });

        // Outlet connection controls
        outletComboBox = new JComboBox<>();
        outletComboBox.setFont(controlFont);
        populateOutletComboBox();

        connectOutletButton = new JButton("Connect to Outlet");
        connectOutletButton.setFont(controlFont);
        connectOutletButton.addActionListener(e -> {
            Outlet selectedOutlet = (Outlet) outletComboBox.getSelectedItem();
            if (selectedOutlet != null) {
                try {
                    vacuum.connectToOutlet(selectedOutlet);
                    updateDisplay();
                    JOptionPane.showMessageDialog(this, "Connected to " + selectedOutlet.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) { // Catch broader exceptions for connection issues
                    JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No outlet selected or available.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        disconnectOutletButton = new JButton("Disconnect from Outlet");
        disconnectOutletButton.setFont(controlFont);
        disconnectOutletButton.addActionListener(e -> {
            Outlet currentOutlet = vacuum.getOutlet();
            if (currentOutlet != null) {
                try {
                    vacuum.disconnectFromOutlet(currentOutlet);
                    updateDisplay();
                    JOptionPane.showMessageDialog(this, "Disconnected from " + currentOutlet.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) { // Catch broader exceptions for disconnection issues
                    JOptionPane.showMessageDialog(this, "Disconnection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No outlet is currently connected.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
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
        gbc.gridy = row++;
        add(batteryLabel, gbc);
        gbc.gridy = row++;
        add(chargingStatusLabel, gbc);
        gbc.gridy = row++;
        add(connectedOutletLabel, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Mode selection
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Cleaning Mode:"), gbc);
        gbc.gridx = 1;
        add(modeComboBox, gbc);
        row++;

        // Power control buttons
        gbc.gridx = 0; gbc.gridy = row;
        add(turnOnButton, gbc);
        gbc.gridx = 1;
        add(turnOffButton, gbc);
        row++;

        // Cleaning simulation buttons
        gbc.gridx = 0; gbc.gridy = row;
        add(startCleaningButton, gbc);
        gbc.gridx = 1;
        add(stopCleaningButton, gbc);
        row++;

        // Outlet connection controls
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Connect to:"), gbc);
        gbc.gridx = 1;
        add(outletComboBox, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        add(connectOutletButton, gbc);
        gbc.gridx = 1;
        add(disconnectOutletButton, gbc);
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
        statusLabel.setText("Status: " + vacuum.getStatus());
        simulationStatusLabel.setText("Simulation: " + (vacuum.isSimulating() ? "Running" : "Stopped"));
        batteryLabel.setText("Battery: " + vacuum.getBattery() + "%");
        modeLabel.setText("Mode: " + vacuum.getMode());
        chargingStatusLabel.setText("Charging: " + (vacuum.isCharging() ? "Yes" : "No"));
        connectedOutletLabel.setText("Connected to: " + (vacuum.getOutlet() != null ? vacuum.getOutlet().getName() : "None"));

        // Enable/disable buttons based on vacuum status, battery, and simulation
        boolean isOn = vacuum.isOn();
        boolean isSimulating = vacuum.isSimulating();
        boolean isCharging = vacuum.isCharging();
        boolean hasOutletConnected = (vacuum.getOutlet() != null);
        boolean batteryFull = (vacuum.getBattery() == 100);
        boolean batteryEmpty = (vacuum.getBattery() == 0);

        turnOnButton.setEnabled(!isOn && !isCharging && !batteryEmpty);
        turnOffButton.setEnabled(isOn);

        startCleaningButton.setEnabled(isOn && !isSimulating && !isCharging && !batteryEmpty);
        stopCleaningButton.setEnabled(isSimulating); // Stop works for both cleaning and charging simulations

        modeComboBox.setEnabled(isOn && !isSimulating); // Can change mode only when ON and not simulating

        // Outlet connection buttons
        connectOutletButton.setEnabled(!hasOutletConnected && !isSimulating && allOutlets != null && !allOutlets.isEmpty());
        disconnectOutletButton.setEnabled(hasOutletConnected);

        // Update Outlet ComboBox
        populateOutletComboBox(); // Refresh to reflect current connections
    }

    private void populateOutletComboBox() {
        Outlet currentlySelectedInCombo = (Outlet) outletComboBox.getSelectedItem();
        outletComboBox.removeAllItems(); // Clear existing items

        // Add a "None Selected" option if no outlet is connected
        if (!allOutlets.isEmpty() && vacuum.getOutlet() == null) {
            outletComboBox.addItem(null); // Represents "no selection"
        }

        for (Outlet outlet : allOutlets) {
            // Only add outlets that are not currently connected to THIS vacuum
            // and are not already connected to another device (assuming an outlet can only power one device)
            if (outlet != vacuum.getOutlet() && outlet.getConnectedDevice() == null) {
                outletComboBox.addItem(outlet);
            }
        }

        // If an outlet is currently connected to the vacuum, ensure it's not in the selectable list
        // and its info is reflected via connectedOutletLabel.
        // If the previously selected item is still available and not the connected one, reselect it.
        if (currentlySelectedInCombo != null && currentlySelectedInCombo != vacuum.getOutlet() && outletComboBox.getItemCount() > 0) {
            boolean found = false;
            for (int i = 0; i < outletComboBox.getItemCount(); i++) {
                if (outletComboBox.getItemAt(i) == currentlySelectedInCombo) {
                    outletComboBox.setSelectedItem(currentlySelectedInCombo);
                    found = true;
                    break;
                }
            }
            if (!found && outletComboBox.getItemCount() > 0) {
                outletComboBox.setSelectedIndex(0); // Select first available if previous not found
            }
        } else if (vacuum.getOutlet() == null && outletComboBox.getItemCount() > 0) {
            outletComboBox.setSelectedIndex(0); // If no vacuum outlet, select the first available for connection
        } else if (outletComboBox.getItemCount() > 0) {
            // If vacuum is connected, and the connected outlet is in the list, hide it? No, it shouldn't be in the list.
            outletComboBox.setSelectedIndex(-1); // No selection if vacuum is already connected or no options
        }
    }

    // Custom renderer for Outlet ComboBox to show outlet name
    private static class OutletComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Outlet) {
                Outlet outlet = (Outlet) value;
                label.setText(outlet.getName() + " (" + (outlet.isOn() ? "ON" : "OFF") + ")");
            } else if (value == null) {
                label.setText("-- Select an Outlet --"); // Text for the null option
            }
            return label;
        }
    }
}