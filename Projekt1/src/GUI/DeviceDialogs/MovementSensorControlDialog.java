package GUI;

import Devices.MovementSensor;
import Enums.MovementSensorEnums;

import javax.swing.*;
import java.awt.*;

public class MovementSensorControlDialog extends JDialog {
    private MovementSensor sensor;

    private JLabel currentStatusLabel;
    private JLabel simulationStatusLabel;
    private JLabel sensorTypeLabel;
    private JLabel rangeLabel;
    private JLabel movementDetectedLabel;

    private JComboBox<MovementSensorEnums> sensorTypeComboBox;
    private JTextField rangeField;
    private JButton setRangeButton;
    private JButton forceMovementOnButton;
    private JButton forceMovementOffButton;

    public MovementSensorControlDialog(JFrame parent, MovementSensor sensor) {
        super(parent, "Control Movement Sensor: " + sensor.getName(), true);
        this.sensor = sensor;
        setLayout(new GridBagLayout());
        setSize(850, 850);
        setLocationRelativeTo(parent);

        initComponents();
        createLayout();
        updateDisplay(); // Pierwsze odświeżenie

        // Timer do cyklicznego odświeżania statusu, np. co sekundę
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

        sensorTypeLabel = new JLabel();
        sensorTypeLabel.setFont(labelFont);

        rangeLabel = new JLabel();
        rangeLabel.setFont(labelFont);

        movementDetectedLabel = new JLabel();
        movementDetectedLabel.setFont(labelFont);

        // Sensor Type ComboBox
        sensorTypeComboBox = new JComboBox<>(MovementSensorEnums.values());
        sensorTypeComboBox.setSelectedItem(sensor.getMovementSensorType());
        sensorTypeComboBox.setFont(controlFont);
        sensorTypeComboBox.addActionListener(e -> {
            MovementSensorEnums selectedType = (MovementSensorEnums) sensorTypeComboBox.getSelectedItem();
            sensor.setMovementSensorType(selectedType);
            updateDisplay(); // Odśwież, by zaktualizować zasięg
            JOptionPane.showMessageDialog(this, "Sensor type changed to " + selectedType, "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        // Range Field and Button
        rangeField = new JTextField(String.valueOf(sensor.getRange()), 5);
        rangeField.setFont(controlFont);
        setRangeButton = new JButton("Set Range");
        setRangeButton.setFont(controlFont);
        setRangeButton.addActionListener(e -> {
            try {
                int newRange = Integer.parseInt(rangeField.getText());
                if (newRange >= 0) {
                    sensor.setRange(newRange);
                    updateDisplay();
                    JOptionPane.showMessageDialog(this, "Range set to " + newRange + "m.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Range cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for range.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Force Movement Buttons (for manual testing/simulation override)
        forceMovementOnButton = new JButton("Force Movement ON");
        forceMovementOnButton.setFont(controlFont);
        forceMovementOnButton.addActionListener(e -> {
            sensor.forceMovementDetection(true);
            updateDisplay();
        });

        forceMovementOffButton = new JButton("Force Movement OFF");
        forceMovementOffButton.setFont(controlFont);
        forceMovementOffButton.addActionListener(e -> {
            sensor.forceMovementDetection(false);
            updateDisplay();
        });

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose());
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
        gbc.gridy = row++;
        add(movementDetectedLabel, gbc); // Wyświetl status ruchu
        gbc.gridwidth = 1; // Reset gridwidth

        // Sensor Type
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Sensor Type:"), gbc);
        gbc.gridx = 1;
        add(sensorTypeComboBox, gbc);
        row++;

        // Range
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Range (m):"), gbc);
        gbc.gridx = 1;
        JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        rangePanel.add(rangeField);
        rangePanel.add(setRangeButton);
        add(rangePanel, gbc);
        row++;

        // Force Movement Buttons
        gbc.gridx = 0; gbc.gridy = row;
        add(forceMovementOnButton, gbc);
        gbc.gridx = 1;
        add(forceMovementOffButton, gbc);
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
        currentStatusLabel.setText("Power Status: " + sensor.getStatus());
        simulationStatusLabel.setText("Simulation: " + (sensor.isSimulating() ? "Running" : "Stopped"));
        movementDetectedLabel.setText("Movement Detected: " + (sensor.readValue().equals(1) ? "Yes" : "No"));

        // Aktualizacja wartości w kontrolkach
        sensorTypeComboBox.setSelectedItem(sensor.getMovementSensorType());
        rangeField.setText(String.valueOf(sensor.getRange()));

        // Włączanie/wyłączanie kontrolek w zależności od statusu urządzenia
        boolean isSensorOn = sensor.isOn();
        sensorTypeComboBox.setEnabled(isSensorOn);
        rangeField.setEnabled(isSensorOn);
        setRangeButton.setEnabled(isSensorOn);
        forceMovementOnButton.setEnabled(isSensorOn && !sensor.isSimulating()); // Nie zmuszaj, jeśli symulacja działa
        forceMovementOffButton.setEnabled(isSensorOn && !sensor.isSimulating()); // Nie zmuszaj, jeśli symulacja działa

        // Jeśli symulacja działa, przyciski "Force Movement" są wyłączone,
        // a sensor sam generuje wartości.
        if (sensor.isSimulating()) {
            forceMovementOnButton.setToolTipText("Disable simulation to manually control movement.");
            forceMovementOffButton.setToolTipText("Disable simulation to manually control movement.");
        } else {
            forceMovementOnButton.setToolTipText(null);
            forceMovementOffButton.setToolTipText(null);
        }
    }
}