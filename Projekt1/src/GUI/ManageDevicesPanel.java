package GUI;

import Devices.*;
import Enums.MovementSensorEnums;
import Enums.Status;
import House.Room;
import House.SmartHomeSystem;
import Logger.DeviceLogger;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import Interfaces.*; // Upewnij się, że masz ten import, jeśli korzystasz z interfejsów Switchable, Pluggable itp.

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

    // Komponenty GUI
    private JLabel roomNameLabel;
    private JButton addDeviceButton;
    private JButton controlDeviceButton;
    private JButton controlAllDevicesButton;
    private JButton removeDeviceButton;
    private JButton listDevicesButton;
    private JButton backButton;
    private JLabel statusLabel;

    // --- Kolory do schematu UI (powtórzone dla samodzielności klasy) ---
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Stalowo-niebieski
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Alice Blue (jasny)
    private static final Color ACCENT_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private static final Color TEXT_COLOR = new Color(40, 40, 40); // Ciemny szary
    private static final Color BORDER_COLOR = new Color(170, 170, 170); // Szary do obramowań


    public ManageDevicesPanel(SmartHomeSystem system, SmartHomeGUI parentFrame, ManageRoomsPanel backPanel, Room selectedRoom) {
        this.system = system;
        this.parentFrame = parentFrame;
        this.backPanel = backPanel;
        this.selectedRoom = selectedRoom;

        // Upewnij się, że ta klasa jest w stanie obsłużyć brak wybranego pokoju
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "No room selected. Returning to room management.", "Error", JOptionPane.ERROR_MESSAGE);
            // Wywołaj showPanel na EDT
            SwingUtilities.invokeLater(() -> parentFrame.showPanel(backPanel));
            return;
        }

        initializeUI();
    }

    private void initializeUI() {
        // Zmieniamy na GridBagLayout dla większej elastyczności
        setLayout(new GridBagLayout());
        setBackground(SECONDARY_COLOR); // Ustawienie tła panelu
        setBorder(new EmptyBorder(40, 50, 40, 50)); // Dodatkowy padding wokół panelu

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10); // Większe marginesy między komponentami
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Rozciągnij komponenty w poziomie

        Font titleFont = new Font("Segoe UI", Font.BOLD, 36);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 24);
        Font statusFont = new Font("Segoe UI", Font.ITALIC, 20);

        // Room Name Label
        roomNameLabel = new JLabel("Devices in: " + selectedRoom.getName(), SwingConstants.CENTER);
        roomNameLabel.setFont(titleFont);
        roomNameLabel.setForeground(PRIMARY_COLOR.darker()); // Używamy ciemniejszego primary color

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1; // Zajmuje całą szerokość w pojedynczej kolumnie
        gbc.ipady = 20; // Internal padding vertical
        add(roomNameLabel, gbc);

        gbc.ipady = 10; // Resetuj padding pionowy dla przycisków

        // Przyciski (używamy createStyledButton)
        addDeviceButton = createStyledButton("1. Add Device", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        controlDeviceButton = createStyledButton("2. Control Device", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        controlAllDevicesButton = createStyledButton("3. Control All Devices in Room", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        removeDeviceButton = createStyledButton("4. Remove Device", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        listDevicesButton = createStyledButton("5. List Devices", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        backButton = createStyledButton("6. Back to Room Management", buttonFont, Color.GRAY, Color.DARK_GRAY.brighter(), Color.WHITE); // Inny kolor dla przycisku Wstecz

        gbc.gridy = 1; add(addDeviceButton, gbc);
        gbc.gridy = 2; add(controlDeviceButton, gbc);
        gbc.gridy = 3; add(controlAllDevicesButton, gbc);
        gbc.gridy = 4; add(removeDeviceButton, gbc);
        gbc.gridy = 5; add(listDevicesButton, gbc);
        gbc.gridy = 6; add(backButton, gbc);

        // Status Label
        statusLabel = new JLabel("Manage devices for " + selectedRoom.getName(), SwingConstants.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(TEXT_COLOR);

        gbc.gridy = 7;
        gbc.ipady = 15; // Trochę więcej paddingu dla statusu
        add(statusLabel, gbc);

        // Dodanie słuchaczy zdarzeń
        addDeviceButton.addActionListener(e -> addDevice());
        controlDeviceButton.addActionListener(e -> controlDevice());
        controlAllDevicesButton.addActionListener(e -> controlAllDevices());
        removeDeviceButton.addActionListener(e -> removeDevice());
        listDevicesButton.addActionListener(e -> listDevices());
        backButton.addActionListener(e -> parentFrame.showPanel(backPanel));
    }

    // --- Metody obsługujące akcje ---

    private void addDevice() {
        String[] deviceTypes = {
                "Ambient Lighting", "HVAC", "Lightbulb", "Movement Sensor",
                "Outlet", "Smart Clock", "Smart Vacuum Cleaner", "Temperature Sensor"
        };

        // Stylowanie komponentów w dialogu
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
        panel.setBackground(SECONDARY_COLOR); // Tło panelu dialogu
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

        // Ustawienie opcji dla JOptionPane dla lepszego wyglądu
        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));


        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Device", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Przywróć domyślne style JOptionPane, jeśli to konieczne, choć dla prostych dialogów może to być pomijalne
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
                        newDevice = new SmartVacuumCleaner(name, null); // Outlet jest wybierany później
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


    // Helper methods for picking specific devices from ANY room in the house
    // (Stylizacja tych dialogów również musi być spójna)
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
                label.setFont(dialogInputFont); // Ustaw czcionkę
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Dodaj padding
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR); // Kolor zaznaczenia
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
        sensorComboBox.setFont(dialogInputFont); // Ustaw czcionkę dla comboboxa
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

        // Tymczasowe style dla JOptionPane
        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(this, panel, "Select Temperature Sensor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        // Przywróć domyślne style
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
                .filter(o -> o.getConnectedDevice() == null) // Tylko wolne gniazdka
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

        // Stylowanie przycisków w JOptionPane
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

        // Przywracanie domyślnych stylów
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        switch (choice) {
            case 0: // Turn ON All
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
            case 1: // Turn OFF All
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
            case 2: // Start Simulation All
                for (SmartDevice device : devices) {
                    try {
                        device.simulate();
                        DeviceLogger.logEvent(device, "SIMULATION_STATUS", "Simulation started");
                    } catch (DeviceDisabled e) {
                        DeviceLogger.logEvent(device, "SIMULATION_ERROR", "Device disabled: " + e.getMessage());
                    } catch (SimulationInterrupted e) {
                        DeviceLogger.logEvent(device, "SIMULATION_ERROR", "Simulation interrupted: " + e.getMessage());
                    } catch (UnsupportedOperationException e) {
                        DeviceLogger.logEvent(device, "SIMULATION_WARNING", "Simulation not supported for this device type.");
                    } catch (Exception e) {
                        DeviceLogger.logEvent(device, "SIMULATION_ERROR", "Error starting simulation: " + e.getMessage());
                    }
                }
                statusLabel.setText("Simulation started for all supported devices in " + selectedRoom.getName() + ".");
                JOptionPane.showMessageDialog(this, "Check log for individual simulation statuses.", "Simulation Info", JOptionPane.INFORMATION_MESSAGE);
                break;
            case 3: // Stop Simulation All
                for (SmartDevice device : devices) {
                    try {
                        device.stopSimulation();
                        DeviceLogger.logEvent(device, "SIMULATION_STATUS", "Simulation stopped");
                    } catch (UnsupportedOperationException e) {
                        DeviceLogger.logEvent(device, "SIMULATION_WARNING", "Stop simulation not supported for this device type.");
                    } catch (Exception e) {
                        DeviceLogger.logEvent(device, "SIMULATION_ERROR", "Error stopping simulation: " + e.getMessage());
                    }
                }
                statusLabel.setText("Simulation stopped for all supported devices in " + selectedRoom.getName() + ".");
                JOptionPane.showMessageDialog(this, "Check log for individual simulation statuses.", "Simulation Info", JOptionPane.INFORMATION_MESSAGE);
                break;
            case 4: // Cancel
            case JOptionPane.CLOSED_OPTION:
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
                // Konfirmacja usunięcia
                int confirmResult = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to remove device '" + deviceToRemove.getName() + "'?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmResult == JOptionPane.YES_OPTION) {
                    // Odłącz urządzenie od gniazdka, jeśli jest podłączone
                    if (deviceToRemove instanceof Pluggable pluggableDevice) {
                        Outlet connectedOutlet = pluggableDevice.getOutlet();
                        if (connectedOutlet != null) {
                            pluggableDevice.disconnectFromOutlet(connectedOutlet);
                            DeviceLogger.logEvent(deviceToRemove, "CONNECTION", "Disconnected from outlet during removal.");
                        }
                    }
                    // Zatrzymaj symulację przed usunięciem
                    try {
                        deviceToRemove.stopSimulation();
                    } catch (UnsupportedOperationException e) {
                        // Ignoruj, jeśli urządzenie nie obsługuje symulacji
                    }

                    selectedRoom.removeDevice(deviceToRemove);
                    statusLabel.setText("Device '" + deviceToRemove.getName() + "' removed successfully from room '" + selectedRoom.getName() + "'.");
                    DeviceLogger.logEvent(deviceToRemove, "DEVICE_REMOVED", "Device has been removed.");
                } else {
                    statusLabel.setText("Device removal cancelled.");
                }
            }
        } else {
            statusLabel.setText("Device selection cancelled.");
        }
    }


    private void listDevices() {
        List<SmartDevice> devices = selectedRoom.getDevices();
        if (devices.isEmpty()) {
            statusLabel.setText("No devices in room '" + selectedRoom.getName() + "'.");
            JOptionPane.showMessageDialog(this, "No devices in room '" + selectedRoom.getName() + "'.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("Devices in room '" + selectedRoom.getName() + "':\n\n");
        for (int i = 0; i < devices.size(); i++) {
            SmartDevice device = devices.get(i);
            sb.append((i + 1)).append(". Name: ").append(device.getName()).append("\n")
                    .append("   Type: ").append(device.getDeviceType()).append("\n")
                    .append("   ID: ").append(device.getId()).append("\n")
                    .append("   Status: ").append(device.isOn() ? "On" : "Off").append("\n");

            sb.append("\n");
        }
        statusLabel.setText("Devices listed for room '" + selectedRoom.getName() + "'.");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        textArea.setEditable(false);
        textArea.setBackground(SECONDARY_COLOR.brighter()); // Jasne tło dla text area
        textArea.setForeground(TEXT_COLOR); // Ciemny tekst

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(850, 850));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1)); // Delikatne obramowanie

        // Stylowanie JOptionPane dla wyświetlania listy
        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        JOptionPane.showMessageDialog(this, scrollPane, "Devices in " + selectedRoom.getName(), JOptionPane.PLAIN_MESSAGE);

        // Przywracanie domyślnych stylów
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);
    }

    // --- Metoda pomocnicza do tworzenia stylizowanych przycisków ---
    // (Przeniesiona z innych klas GUI, aby zapewnić spójność)
    private JButton createStyledButton(String text, Font font, Color normalBg, Color hoverBg, Color textCol) {
        JButton button = new JButton(text) {
            private final int CORNER_RADIUS = 20; // Stały promień zaokrąglenia

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int width = getWidth();
                int height = getHeight();

                // Rysowanie cienia
                int shadowOffset = 3;
                Color shadowColor = new Color(0, 0, 0, 40);
                if (getModel().isPressed()) {
                    shadowColor = new Color(0, 0, 0, 80);
                    shadowOffset = 2;
                }
                g2.setColor(shadowColor);
                g2.fillRoundRect(shadowOffset, shadowOffset, width - shadowOffset, height - shadowOffset, CORNER_RADIUS, CORNER_RADIUS);

                // Rysowanie tła przycisku
                if (getModel().isRollover()) {
                    g2.setColor(hoverBg); // Kolor przy najechaniu
                } else {
                    g2.setColor(normalBg); // Domyślny kolor
                }
                g2.fillRoundRect(0, 0, width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS);

                // Rysowanie tekstu
                FontMetrics metrics = g2.getFontMetrics(getFont());
                int x = (width - metrics.stringWidth(getText())) / 2;
                int y = ((height - metrics.getHeight()) / 2) + metrics.getAscent();
                g2.setColor(textCol);
                g2.setFont(getFont());
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            @Override
            public void paintBorder(Graphics g) {
                // Nie rysujemy domyślnego obramowania
            }

            @Override
            public boolean contains(int x, int y) {
                Shape shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS);
                return shape.contains(x, y);
            }
        };

        button.setFont(font);
        button.setForeground(textCol);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }
}