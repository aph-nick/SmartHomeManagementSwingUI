package GUI;

import Devices.*;
import Enums.MovementSensorEnums;
import Enums.Status;
import House.Room;
import House.SmartHomeSystem;
import Logger.DeviceLogger;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import Interfaces.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ManageDevicesPanel extends JPanel {
    private final SmartHomeSystem system;
    private final SmartHomeGUI parentFrame;
    private final ManageRoomsPanel backPanel;
    private final Room selectedRoom;

    private JLabel roomNameLabel;
    private JButton addDeviceButton;
    private JButton controlDeviceButton;
    private JButton controlAllDevicesButton;
    private JButton removeDeviceButton;
    private JButton listDevicesButton;
    private JButton backButton;
    private JLabel statusLabel;

    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = new Color(40, 40, 40);
    private static final Color BORDER_COLOR = new Color(170, 170, 170);


    public ManageDevicesPanel(SmartHomeSystem system, SmartHomeGUI parentFrame, ManageRoomsPanel backPanel, Room selectedRoom) {
        this.system = system;
        this.parentFrame = parentFrame;
        this.backPanel = backPanel;
        this.selectedRoom = selectedRoom;

        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "No room selected. Returning to room management.", "Error", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> parentFrame.showPanel(backPanel));
            return;
        }

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(SECONDARY_COLOR);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font titleFont = new Font("Segoe UI", Font.BOLD, 36);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 24);
        Font statusFont = new Font("Segoe UI", Font.ITALIC, 20);

        roomNameLabel = new JLabel("Devices in: " + selectedRoom.getName(), SwingConstants.CENTER);
        roomNameLabel.setFont(titleFont);
        roomNameLabel.setForeground(PRIMARY_COLOR.darker());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.ipady = 20;
        add(roomNameLabel, gbc);

        gbc.ipady = 10;

        addDeviceButton = createStyledButton("1. Add Device", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        controlDeviceButton = createStyledButton("2. Control Device", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        controlAllDevicesButton = createStyledButton("3. Control All Devices in Room", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        removeDeviceButton = createStyledButton("4. Remove Device", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        listDevicesButton = createStyledButton("5. List Devices", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        backButton = createStyledButton("6. Back to Room Management", buttonFont, Color.GRAY, Color.DARK_GRAY.brighter(), Color.WHITE);

        gbc.gridy = 1; add(addDeviceButton, gbc);
        gbc.gridy = 2; add(controlDeviceButton, gbc);
        gbc.gridy = 3; add(controlAllDevicesButton, gbc);
        gbc.gridy = 4; add(removeDeviceButton, gbc);
        gbc.gridy = 5; add(listDevicesButton, gbc);
        gbc.gridy = 6; add(backButton, gbc);

        statusLabel = new JLabel("Manage devices for " + selectedRoom.getName(), SwingConstants.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(TEXT_COLOR);

        gbc.gridy = 7;
        gbc.ipady = 15;
        add(statusLabel, gbc);

        addDeviceButton.addActionListener(e -> addDevice());
        controlDeviceButton.addActionListener(e -> controlDevice());
        controlAllDevicesButton.addActionListener(e -> controlAllDevices());
        removeDeviceButton.addActionListener(e -> removeDevice());
        listDevicesButton.addActionListener(e -> listDevices());
        backButton.addActionListener(e -> parentFrame.showPanel(backPanel));
    }

    private void addDevice() {
        String[] deviceTypes = {
                "Ambient Lighting", "HVAC", "Lightbulb", "Movement Sensor",
                "Outlet", "Smart Clock", "Smart Vacuum Cleaner", "Temperature Sensor"
        };

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 18);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 18);

        JComboBox<String> deviceTypeComboBox = new JComboBox<>(deviceTypes);
        deviceTypeComboBox.setFont(dialogInputFont);
        deviceTypeComboBox.setBackground(Color.WHITE);
        deviceTypeComboBox.setForeground(TEXT_COLOR);
        deviceTypeComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JTextField nameField = new JTextField(20);
        nameField.setFont(dialogInputFont);
        nameField.setBackground(Color.WHITE);
        nameField.setForeground(TEXT_COLOR);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SECONDARY_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Device Name:");
        nameLabel.setFont(dialogLabelFont);
        nameLabel.setForeground(TEXT_COLOR);
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel typeLabel = new JLabel("Device Type:");
        typeLabel.setFont(dialogLabelFont);
        typeLabel.setForeground(TEXT_COLOR);
        panel.add(typeLabel, gbc);
        gbc.gridx = 1;
        panel.add(deviceTypeComboBox, gbc);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));


        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Device", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);


        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String selectedType = (String) deviceTypeComboBox.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Device name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Device name cannot be empty.");
                return;
            }

            SmartDevice newDevice = null;
            try {
                switch (selectedType) {
                    case "Ambient Lighting":
                        SmartClock smartClock = pickSmartClock(system.getAllRoomsInSystem());
                        if (smartClock == null) {
                            JOptionPane.showMessageDialog(this, "A Smart Clock is required for Ambient Lighting. Please add one first.", "Missing Dependency", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        newDevice = new AmbientLighting(name, smartClock);
                        break;
                    case "HVAC":
                        TemperatureSensor tempSensor = pickTemperatureSensor(system.getAllRoomsInSystem());
                        if (tempSensor == null) {
                            JOptionPane.showMessageDialog(this, "A Temperature Sensor is required for HVAC. Please add one first.", "Missing Dependency", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        Outlet hvacOutlet = pickOutlet(system.getAllRoomsInSystem());
                        if (hvacOutlet == null) {
                            JOptionPane.showMessageDialog(this, "An Outlet is required for HVAC. Please add one first.", "Missing Dependency", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        newDevice = new HVAC(name, tempSensor, hvacOutlet);
                        break;
                    case "Lightbulb":
                        newDevice = new LightBulb(name);
                        break;
                    case "Movement Sensor":
                        MovementSensorEnums sensorType = selectMovementSensorType();
                        if (sensorType == null) return;
                        newDevice = new MovementSensor(name, sensorType);
                        break;
                    case "Outlet":
                        newDevice = new Outlet(name);
                        break;
                    case "Smart Clock":
                        newDevice = new SmartClock(name);
                        break;
                    case "Smart Vacuum Cleaner":
                        newDevice = new SmartVacuumCleaner(name, null);
                        break;
                    case "Temperature Sensor":
                        newDevice = new TemperatureSensor(name);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Invalid device type selected.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating device: " + ex.getMessage(), "Device Creation Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                return;
            }

            if (newDevice != null) {
                newDevice.setRoom(selectedRoom);
                selectedRoom.addDevice(newDevice);
                statusLabel.setText("Device '" + newDevice.getName() + "' added to room: " + selectedRoom.getName());
                DeviceLogger.logDeviceAdded(newDevice);
            }
        } else {
            statusLabel.setText("Add Device operation cancelled.");
        }
    }


    private TemperatureSensor pickTemperatureSensor(List<Room> allRoomsInHouse) {
        List<TemperatureSensor> tempSensors = allRoomsInHouse.stream()
                .flatMap(r -> r.getDevices().stream())
                .filter(d -> d instanceof TemperatureSensor)
                .map(d -> (TemperatureSensor) d)
                .collect(Collectors.toList());

        if (tempSensors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No Temperature Sensors found in any room.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 16);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                if (value instanceof TemperatureSensor sensor) {
                    label.setText(sensor.getName() + " (Room: " + (sensor.getRoom() != null ? sensor.getRoom().getName() : "Unknown") + ")");
                }
                return label;
            }
        };

        JComboBox<TemperatureSensor> sensorComboBox = new JComboBox<>(tempSensors.toArray(new TemperatureSensor[0]));
        sensorComboBox.setRenderer(renderer);
        sensorComboBox.setFont(dialogInputFont);
        sensorComboBox.setBackground(Color.WHITE);
        sensorComboBox.setForeground(TEXT_COLOR);
        sensorComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select a Temperature Sensor:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(sensorComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Temperature Sensor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        if (result == JOptionPane.OK_OPTION) {
            return (TemperatureSensor) sensorComboBox.getSelectedItem();
        }
        return null;
    }

    private SmartClock pickSmartClock(List<Room> allRoomsInHouse) {
        List<SmartClock> smartClocks = allRoomsInHouse.stream()
                .flatMap(r -> r.getDevices().stream())
                .filter(d -> d instanceof SmartClock)
                .map(d -> (SmartClock) d)
                .collect(Collectors.toList());

        if (smartClocks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No Smart Clocks found in any room.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 16);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                if (value instanceof SmartClock clock) {
                    label.setText(clock.getName() + " (Room: " + (clock.getRoom() != null ? clock.getRoom().getName() : "Unknown") + ")");
                }
                return label;
            }
        };

        JComboBox<SmartClock> clockComboBox = new JComboBox<>(smartClocks.toArray(new SmartClock[0]));
        clockComboBox.setRenderer(renderer);
        clockComboBox.setFont(dialogInputFont);
        clockComboBox.setBackground(Color.WHITE);
        clockComboBox.setForeground(TEXT_COLOR);
        clockComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));


        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select a Smart Clock:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(clockComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Smart Clock", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        if (result == JOptionPane.OK_OPTION) {
            return (SmartClock) clockComboBox.getSelectedItem();
        }
        return null;
    }

    private Outlet pickOutlet(List<Room> allRoomsInHouse) {
        List<Outlet> outlets = allRoomsInHouse.stream()
                .flatMap(r -> r.getDevices().stream())
                .filter(d -> d instanceof Outlet)
                .map(d -> (Outlet) d)
                .filter(o -> o.getConnectedDevice() == null)
                .collect(Collectors.toList());

        if (outlets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available (unconnected) Outlets found in any room.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 16);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                if (value instanceof Outlet outlet) {
                    label.setText(outlet.getName() + " (Room: " + (outlet.getRoom() != null ? outlet.getRoom().getName() : "Unknown") + ", Status: " + (outlet.isOn() ? "ON" : "OFF") + ")");
                }
                return label;
            }
        };

        JComboBox<Outlet> outletComboBox = new JComboBox<>(outlets.toArray(new Outlet[0]));
        outletComboBox.setRenderer(renderer);
        outletComboBox.setFont(dialogInputFont);
        outletComboBox.setBackground(Color.WHITE);
        outletComboBox.setForeground(TEXT_COLOR);
        outletComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select an Outlet:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(outletComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Outlet", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        if (result == JOptionPane.OK_OPTION) {
            return (Outlet) outletComboBox.getSelectedItem();
        }
        return null;
    }

    private MovementSensorEnums selectMovementSensorType() {
        MovementSensorEnums[] types = MovementSensorEnums.values();
        String[] typeNames = java.util.Arrays.stream(types).map(Enum::name).toArray(String[]::new);

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 16);

        JComboBox<String> typeComboBox = new JComboBox<>(typeNames);
        typeComboBox.setFont(dialogInputFont);
        typeComboBox.setBackground(Color.WHITE);
        typeComboBox.setForeground(TEXT_COLOR);
        typeComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select Movement Sensor Type:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(typeComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Sensor Type", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        if (result == JOptionPane.OK_OPTION) {
            String selectedName = (String) typeComboBox.getSelectedItem();
            return MovementSensorEnums.valueOf(selectedName);
        }
        return null;
    }

    private void controlDevice() {
        List<SmartDevice> devices = selectedRoom.getDevices();
        if (devices.isEmpty()) {
            statusLabel.setText("No devices in this room to control.");
            JOptionPane.showMessageDialog(this, "No devices in room '" + selectedRoom.getName() + "'.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 18);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 18);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                if (value instanceof SmartDevice device) {
                    label.setText(device.getName() + " (" + device.getDeviceType() + ")");
                }
                return label;
            }
        };

        JComboBox<SmartDevice> deviceComboBox = new JComboBox<>(devices.toArray(new SmartDevice[0]));
        deviceComboBox.setRenderer(renderer);
        deviceComboBox.setFont(dialogInputFont);
        deviceComboBox.setBackground(Color.WHITE);
        deviceComboBox.setForeground(TEXT_COLOR);
        deviceComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select device to control:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(deviceComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Control Device",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        if (result == JOptionPane.OK_OPTION) {
            SmartDevice deviceToControl = (SmartDevice) deviceComboBox.getSelectedItem();

            if (deviceToControl != null) {
                ControlDeviceDialog controlDialog = new ControlDeviceDialog(parentFrame, deviceToControl, system.getAllDevicesInSystem());
                controlDialog.setVisible(true);

                statusLabel.setText("Opened general control for: " + deviceToControl.getName());
            }
        } else {
            statusLabel.setText("Device selection cancelled.");
        }
    }


    private void controlAllDevices() {
        List<SmartDevice> devices = selectedRoom.getDevices();
        if (devices.isEmpty()) {
            statusLabel.setText("No devices in this room to control.");
            JOptionPane.showMessageDialog(this, "No devices in room '" + selectedRoom.getName() + "'.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 18));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));


        String[] options = {"Turn ON All", "Turn OFF All", "Start Simulation All", "Stop Simulation All", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose action for all devices in " + selectedRoom.getName() + ":",
                "Control All Devices",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        switch (choice) {
            case 0:
                for (SmartDevice device : devices) {
                    try {
                        if (device instanceof Switchable switchableDevice) {
                            switchableDevice.turnOn();
                        } else {
                            device.setStatus(Status.ON);
                            DeviceLogger.logStatusChange(device, "Turned ON (All - direct status)");
                        }
                    } catch (Exception e) {
                        DeviceLogger.logEvent(device, "ERROR", "Failed to turn ON (All): " + e.getMessage());
                        JOptionPane.showMessageDialog(this, "Failed to turn ON " + device.getName() + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                statusLabel.setText("Attempted to turn ON all devices in " + selectedRoom.getName() + ".");
                break;
            case 1:
                for (SmartDevice device : devices) {
                    try {
                        if (device instanceof Switchable switchableDevice) {
                            switchableDevice.turnOff();
                        } else {
                            device.setStatus(Status.OFF);
                            DeviceLogger.logStatusChange(device, "Turned OFF (All - direct status)");
                        }
                    } catch (Exception e) {
                        DeviceLogger.logEvent(device, "ERROR", "Failed to turn OFF (All): " + e.getMessage());
                        JOptionPane.showMessageDialog(this, "Failed to turn OFF " + device.getName() + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                statusLabel.setText("Attempted to turn OFF all devices in " + selectedRoom.getName() + ".");
                break;
            case 2:
                for (SmartDevice device : devices) {
                    try {
                        if (device instanceof Simulated simulatedDevice) {
                            simulatedDevice.startSimulation();
                        } else {
                            JOptionPane.showMessageDialog(this, device.getName() + " does not support simulation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (SimulationInterrupted | DeviceDisabled ex) {
                        DeviceLogger.logEvent(device, "WARNING", "Could not start simulation for " + device.getName() + ": " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Could not start simulation for " + device.getName() + ": " + ex.getMessage(), "Simulation Error", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        DeviceLogger.logEvent(device, "ERROR", "Unexpected error starting simulation for " + device.getName() + ": " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Unexpected error starting simulation for " + device.getName() + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                statusLabel.setText("Attempted to start simulation for all devices in " + selectedRoom.getName() + ".");
                break;
            case 3:
                for (SmartDevice device : devices) {
                    try {
                        if (device instanceof Simulated simulatedDevice) {
                            simulatedDevice.stopSimulation();
                        } else {
                            JOptionPane.showMessageDialog(this, device.getName() + " does not support simulation.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (SimulationInterrupted | DeviceDisabled ex) {
                        DeviceLogger.logEvent(device, "WARNING", "Could not stop simulation for " + device.getName() + ": " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Could not stop simulation for " + device.getName() + ": " + ex.getMessage(), "Simulation Error", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        DeviceLogger.logEvent(device, "ERROR", "Unexpected error stopping simulation for " + device.getName() + ": " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Unexpected error stopping simulation for " + device.getName() + ": " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                statusLabel.setText("Attempted to stop simulation for all devices in " + selectedRoom.getName() + ".");
                break;
            case 4:
            default:
                statusLabel.setText("Control All Devices operation cancelled.");
                break;
        }
    }


    private void removeDevice() {
        List<SmartDevice> devices = selectedRoom.getDevices();
        if (devices.isEmpty()) {
            statusLabel.setText("No devices in this room to remove.");
            JOptionPane.showMessageDialog(this, "No devices in room '" + selectedRoom.getName() + "'.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 18);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 18);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                if (value instanceof SmartDevice device) {
                    label.setText(device.getName() + " (" + device.getDeviceType() + ")");
                }
                return label;
            }
        };

        JComboBox<SmartDevice> deviceComboBox = new JComboBox<>(devices.toArray(new SmartDevice[0]));
        deviceComboBox.setRenderer(renderer);
        deviceComboBox.setFont(dialogInputFont);
        deviceComboBox.setBackground(Color.WHITE);
        deviceComboBox.setForeground(TEXT_COLOR);
        deviceComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select device to remove:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(deviceComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));


        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Remove Device",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);


        if (result == JOptionPane.OK_OPTION) {
            SmartDevice deviceToRemove = (SmartDevice) deviceComboBox.getSelectedItem();
            if (deviceToRemove != null) {
                if (selectedRoom.removeDevice(deviceToRemove)) {
                    statusLabel.setText("Device '" + deviceToRemove.getName() + "' removed from room " + selectedRoom.getName() + ".");
                    DeviceLogger.logDeviceRemoved(deviceToRemove);
                } else {
                    statusLabel.setText("Failed to remove device '" + deviceToRemove.getName() + "'.");
                    JOptionPane.showMessageDialog(this, "Failed to remove device '" + deviceToRemove.getName() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            statusLabel.setText("Remove Device operation cancelled.");
        }
    }

    private void listDevices() {
        List<SmartDevice> devices = selectedRoom.getDevices();
        if (devices.isEmpty()) {
            statusLabel.setText("No devices in this room.");
            JOptionPane.showMessageDialog(this, "No devices in room '" + selectedRoom.getName() + "'.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder deviceList = new StringBuilder("Devices in " + selectedRoom.getName() + ":\n\n");
        for (SmartDevice device : devices) {
            deviceList.append("- ").append(device.getName())
                    .append(" (Type: ").append(device.getDeviceType())
                    .append(", Status: ").append(device.getStatus());

            if (device instanceof Pluggable pluggable) {
                deviceList.append(", Plugged: ").append(pluggable.isPluggedIn() ? "Yes" : "No");
            }
            if (device instanceof PowerConsuming pc) {
                deviceList.append(", Power: ").append(pc.getCurrentPowerConsumption()).append("W");
            }
            if (device instanceof Switchable sw) {
                deviceList.append(", Is On: ").append(sw.isOn() ? "Yes" : "No");
            }
            if (device instanceof Dimmable dim) {
                deviceList.append(", Brightness: ").append(dim.getBrightness()).append("%");
            }
            if (device instanceof TemperatureSensor ts) {
                deviceList.append(", Current Temp: ").append(ts.getCurrentTemperature()).append("°C");
            }
            if (device instanceof HVAC hvac) {
                deviceList.append(", Target Temp: ").append(hvac.getTargetTemperature()).append("°C");
            }
            if (device instanceof MovementSensor ms) {
                deviceList.append(", Sensor Type: ").append(ms.getSensorType());
            }

            deviceList.append(")\n");
        }

        JTextArea textArea = new JTextArea(deviceList.toString());
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textArea.setEditable(false);
        textArea.setBackground(SECONDARY_COLOR);
        textArea.setForeground(TEXT_COLOR);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 18));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        JOptionPane.showMessageDialog(this, scrollPane, "Devices in " + selectedRoom.getName(), JOptionPane.PLAIN_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        statusLabel.setText("Listed devices in " + selectedRoom.getName() + ".");
    }

    private JButton createStyledButton(String text, Font font, Color bgColor, Color hoverColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = 40;

        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, width - 1, height - 1, arc, arc);

        g2d.setColor(SECONDARY_COLOR);
        g2d.fill(roundedRectangle);

        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(roundedRectangle);

        g2d.dispose();
    }
}
