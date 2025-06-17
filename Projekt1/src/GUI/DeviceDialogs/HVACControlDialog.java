package GUI;

import Devices.HVAC;

import javax.swing.*;
import java.awt.*;

public class HVACControlDialog extends JDialog {
    private HVAC hvac;

    private JLabel currentStatusLabel;
    private JLabel hvacModeLabel;
    private JLabel simulationStatusLabel;
    private JLabel outletStatusLabel;
    // Usunięto JLabel temperatureSensorLabel; - nie jest potrzebny, bo temp sensor jest obserwowany przez HVAC
    // Nie ma potrzeby dodawania tutaj przycisków turnOn/turnOff/simulate/stopSimulate,
    // ponieważ są one już w głównym ControlDeviceDialog.

    private JButton startVentilationButton;
    private JButton stopVentilationButton;

    public HVACControlDialog(JFrame parent, HVAC hvac) {
        super(parent, "Control HVAC: " + hvac.getName(), true);
        this.hvac = hvac;
        setLayout(new GridBagLayout());
        setSize(850, 850); // Dostosowany rozmiar
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

        hvacModeLabel = new JLabel();
        hvacModeLabel.setFont(labelFont);

        simulationStatusLabel = new JLabel();
        simulationStatusLabel.setFont(labelFont);

        outletStatusLabel = new JLabel();
        outletStatusLabel.setFont(labelFont);

        startVentilationButton = new JButton("Start Ventilation");
        startVentilationButton.setFont(valueFont);
        startVentilationButton.addActionListener(e -> {
            hvac.startVentilation();
            updateDisplay();
            JOptionPane.showMessageDialog(this, "HVAC ventilation started.", "Ventilation", JOptionPane.INFORMATION_MESSAGE);
        });

        stopVentilationButton = new JButton("Stop Ventilation");
        stopVentilationButton.setFont(valueFont);
        stopVentilationButton.addActionListener(e -> {
            hvac.stopVentilation();
            updateDisplay();
            JOptionPane.showMessageDialog(this, "HVAC ventilation stopped.", "Ventilation", JOptionPane.INFORMATION_MESSAGE);
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
        add(hvacModeLabel, gbc);

        gbc.gridy = row++;
        add(simulationStatusLabel, gbc);

        gbc.gridy = row++;
        add(outletStatusLabel, gbc);

        // Przyciski
        gbc.gridy = row++;
        gbc.gridx = 0; gbc.gridwidth = 1;
        add(startVentilationButton, gbc);
        gbc.gridx = 1;
        add(stopVentilationButton, gbc);

        // Przycisk zamknij
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose());
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(closeButton, gbc);
    }

    private void updateDisplay() {
        currentStatusLabel.setText("Power Status: " + hvac.getStatus());
        hvacModeLabel.setText("HVAC Mode: " + hvac.getHvacStatus());
        simulationStatusLabel.setText("Simulation: " + (hvac.isSimulating() ? "Running" : "Stopped"));
        outletStatusLabel.setText("Connected to Outlet: " + (hvac.getOutlet() != null ? hvac.getOutlet().getName() : "None"));
        // temperatureSensorLabel nie jest już aktualizowany, bo nie ma gettera
        // Jeśli chcesz wyświetlać aktualną temperaturę w HVAC Control Dialog, musiałbyś
        // dodać do HVAC pole na aktualnie odczytaną temperaturę z sensora
        // i udostępnić ją przez getter, lub zrobić HVAC obserwatorem własnego sensora temperatury
        // i przechowywać ostatnią wartość.
    }
}