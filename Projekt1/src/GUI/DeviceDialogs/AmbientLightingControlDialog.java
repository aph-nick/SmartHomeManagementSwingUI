package GUI;

import Devices.AmbientLighting;
import Enums.AmbientColors;
import Enums.AmbientIntensity;
import Logger.DeviceLogger;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class AmbientLightingControlDialog extends JDialog {
    private AmbientLighting ambientLighting;

    private JComboBox<AmbientColors> colorComboBox;
    private JComboBox<AmbientIntensity> intensityComboBox;
    private JTextField onTimeField;
    private JTextField offTimeField;
    private JLabel currentStatusLabel;
    private JLabel simulationStatusLabel;

    // Zmienne instancji dla przycisków
    private JButton setOnTimeButton;
    private JButton setOffTimeButton;
    private JButton closeButton; // Również deklarujemy, by była spójność

    public AmbientLightingControlDialog(JFrame parent, AmbientLighting ambientLighting) {
        super(parent, "Control Ambient Lighting: " + ambientLighting.getName(), true);
        this.ambientLighting = ambientLighting;
        setLayout(new GridBagLayout());
        setSize(850, 850);
        setLocationRelativeTo(parent);

        initComponents();
        createLayout();
        updateDisplay();

        // Timer do odświeżania statusu
        Timer refreshTimer = new Timer(1000, e -> updateDisplay());
        refreshTimer.start();
    }

    private void initComponents() {
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font controlFont = new Font("Arial", Font.PLAIN, 16);

        currentStatusLabel = new JLabel();
        currentStatusLabel.setFont(labelFont);

        simulationStatusLabel = new JLabel();
        simulationStatusLabel.setFont(labelFont);

        // Ambient Color
        JLabel colorLabel = new JLabel("Ambient Color:");
        colorLabel.setFont(labelFont);
        colorComboBox = new JComboBox<>(AmbientColors.values());
        colorComboBox.setSelectedItem(ambientLighting.getAmbientColor());
        colorComboBox.setFont(controlFont);
        colorComboBox.addActionListener(e -> {
            AmbientColors selectedColor = (AmbientColors) colorComboBox.getSelectedItem();
            ambientLighting.setAmbientColor(selectedColor);
            DeviceLogger.logStatusChange(ambientLighting, "Color set to " + selectedColor);
        });

        // Ambient Intensity
        JLabel intensityLabel = new JLabel("Ambient Intensity:");
        intensityLabel.setFont(labelFont);
        intensityComboBox = new JComboBox<>(AmbientIntensity.values());
        intensityComboBox.setSelectedItem(ambientLighting.getAmbientIntensity());
        intensityComboBox.setFont(controlFont);
        intensityComboBox.addActionListener(e -> {
            AmbientIntensity selectedIntensity = (AmbientIntensity) intensityComboBox.getSelectedItem();
            ambientLighting.setAmbientIntensity(selectedIntensity);
            DeviceLogger.logStatusChange(ambientLighting, "Intensity set to " + selectedIntensity);
        });

        // On Time
        JLabel onTimeLabel = new JLabel("ON Time (HH:mm):");
        onTimeLabel.setFont(labelFont);
        onTimeField = new JTextField(ambientLighting.getOnTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 8);
        onTimeField.setFont(controlFont);
        setOnTimeButton = new JButton("Set ON Time"); // Inicjalizacja tutaj
        setOnTimeButton.setFont(controlFont);
        setOnTimeButton.addActionListener(e -> { // Dodanie ActionListener tutaj
            try {
                LocalTime newTime = LocalTime.parse(onTimeField.getText());
                ambientLighting.setOnTime(newTime);
                DeviceLogger.logEvent(ambientLighting, "TIME_SET", "ON time set to " + newTime);
                JOptionPane.showMessageDialog(this, "ON time set to " + newTime, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Off Time
        JLabel offTimeLabel = new JLabel("OFF Time (HH:mm):");
        offTimeLabel.setFont(labelFont);
        offTimeField = new JTextField(ambientLighting.getOffTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), 8);
        offTimeField.setFont(controlFont);
        setOffTimeButton = new JButton("Set OFF Time"); // Inicjalizacja tutaj
        setOffTimeButton.setFont(controlFont);
        setOffTimeButton.addActionListener(e -> { // Dodanie ActionListener tutaj
            try {
                LocalTime newTime = LocalTime.parse(offTimeField.getText());
                ambientLighting.setOffTime(newTime);
                DeviceLogger.logEvent(ambientLighting, "TIME_SET", "OFF time set to " + newTime);
                JOptionPane.showMessageDialog(this, "OFF time set to " + newTime, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:mm", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Close button
        closeButton = new JButton("Close"); // Inicjalizacja tutaj
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose()); // Dodanie ActionListener tutaj
    }

    private void createLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Status Labels
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        add(currentStatusLabel, gbc);
        gbc.gridy = row++;
        add(simulationStatusLabel, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Color
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Ambient Color:"), gbc);
        gbc.gridx = 1;
        add(colorComboBox, gbc);
        row++;

        // Intensity
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Ambient Intensity:"), gbc);
        gbc.gridx = 1;
        add(intensityComboBox, gbc);
        row++;

        // On Time
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("ON Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        JPanel onTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        onTimePanel.add(onTimeField);
        onTimePanel.add(setOnTimeButton); // Dodajemy już zainicjalizowany przycisk
        add(onTimePanel, gbc);
        row++;

        // Off Time
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("OFF Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        JPanel offTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        offTimePanel.add(offTimeField);
        offTimePanel.add(setOffTimeButton); // Dodajemy już zainicjalizowany przycisk
        add(offTimePanel, gbc);
        row++;

        // Close Button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(closeButton, gbc); // Dodajemy już zainicjalizowany przycisk
    }

    private void updateDisplay() {
        currentStatusLabel.setText("Status: " + ambientLighting.getStatus());
        simulationStatusLabel.setText("Simulation: " + (ambientLighting.isSimulating() ? "Running" : "Stopped"));
        colorComboBox.setSelectedItem(ambientLighting.getAmbientColor());
        intensityComboBox.setSelectedItem(ambientLighting.getAmbientIntensity());
        onTimeField.setText(ambientLighting.getOnTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        offTimeField.setText(ambientLighting.getOffTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }
}