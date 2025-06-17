package GUI;

import Devices.TemperatureSensor;
import Enums.Status;
import Enums.TemperatureSensorEnums;

import javax.swing.*;
import java.awt.*;

public class TemperatureSensorControlDialog extends JDialog {
    private TemperatureSensor sensor;

    private JLabel currentStatusLabel;
    private JLabel currentTemperatureLabel;
    private JLabel batteryLevelLabel;
    private JLabel sensorModeLabel;
    private JLabel simulationStatusLabel;

    private JButton autoChargeButton; // Przycisk do ręcznego wymuszenia ładowania/resetu

    public TemperatureSensorControlDialog(JFrame parent, TemperatureSensor sensor) {
        super(parent, "Control Temperature Sensor: " + sensor.getName(), true);
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
        Font valueFont = new Font("Arial", Font.PLAIN, 16);

        currentStatusLabel = new JLabel();
        currentStatusLabel.setFont(labelFont);

        currentTemperatureLabel = new JLabel();
        currentTemperatureLabel.setFont(labelFont);

        batteryLevelLabel = new JLabel();
        batteryLevelLabel.setFont(labelFont);

        sensorModeLabel = new JLabel();
        sensorModeLabel.setFont(labelFont);

        simulationStatusLabel = new JLabel();
        simulationStatusLabel.setFont(labelFont);

        autoChargeButton = new JButton("Force Recharge / Reset Status");
        autoChargeButton.setFont(valueFont);
        autoChargeButton.addActionListener(e -> {
            sensor.autoCharge();
            // Możesz również zresetować status na ACTIVE, jeśli to ma sens po ręcznym naładowaniu
            if (sensor.getSensorStatus() != TemperatureSensorEnums.ACTIVE && sensor.getStatus() == Status.ON) {
                sensor.changeSensorStatus(TemperatureSensorEnums.ACTIVE);
            }
            updateDisplay();
            JOptionPane.showMessageDialog(this, "Temperature sensor battery recharged and status reset (if applicable).", "Recharge", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void createLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Statusy
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        add(currentStatusLabel, gbc);

        gbc.gridy = row++;
        add(currentTemperatureLabel, gbc);

        gbc.gridy = row++;
        add(batteryLevelLabel, gbc);

        gbc.gridy = row++;
        add(sensorModeLabel, gbc);

        gbc.gridy = row++;
        add(simulationStatusLabel, gbc);

        // Przycisk akcji
        gbc.gridy = row++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Wyśrodkuj przycisk
        add(autoChargeButton, gbc);

        // Przycisk zamknij
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose());
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(closeButton, gbc);
    }

    private void updateDisplay() {
        currentStatusLabel.setText("Power Status: " + sensor.getStatus());
        currentTemperatureLabel.setText(String.format("Temperature: %.2f °C", sensor.readValue()));
        batteryLevelLabel.setText(String.format("Battery: %.0f%%", sensor.getBattery()));
        sensorModeLabel.setText("Sensor Status: " + sensor.getSensorStatus());
        simulationStatusLabel.setText("Simulation: " + (sensor.isSimulating() ? "Running" : "Stopped"));

        // Warunkowe włączanie/wyłączanie przycisku ładowania
        autoChargeButton.setEnabled(sensor.getBattery() <= 99.0); // Włączony, jeśli bateria nie jest pełna
    }
}