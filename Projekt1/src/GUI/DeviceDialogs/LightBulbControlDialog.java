package GUI;

import Devices.LightBulb;

import javax.swing.*;
import java.awt.*;

public class LightBulbControlDialog extends JDialog {
    private LightBulb lightBulb;

    private JLabel currentStatusLabel;
    private JLabel simulationStatusLabel;
    private JPanel colorPreviewPanel;

    private JSlider hueSlider;
    private JSlider saturationSlider;
    private JSlider valueSlider;

    private JLabel hueValueLabel;
    private JLabel saturationValueLabel;
    private JLabel valueValueLabel;

    public LightBulbControlDialog(JFrame parent, LightBulb lightBulb) {
        super(parent, "Control Light Bulb: " + lightBulb.getName(), true);
        this.lightBulb = lightBulb;
        setLayout(new GridBagLayout());
        setSize(850, 850);
        setLocationRelativeTo(parent);

        initComponents();
        createLayout();
        updateDisplay();

        // Timer do cyklicznego odświeżania statusu, np. co sekundę
        Timer refreshTimer = new Timer(1000, e -> updateDisplay());
        refreshTimer.start();
    }

    private void initComponents() {
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font valueFont = new Font("Arial", Font.PLAIN, 16);

        currentStatusLabel = new JLabel();
        currentStatusLabel.setFont(labelFont);

        simulationStatusLabel = new JLabel();
        simulationStatusLabel.setFont(labelFont);

        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setPreferredSize(new Dimension(150, 50));
        colorPreviewPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        // Hue Slider (0-360)
        hueSlider = new JSlider(JSlider.HORIZONTAL, 0, 360, (int) lightBulb.getHue());
        hueSlider.setMajorTickSpacing(60);
        hueSlider.setMinorTickSpacing(10);
        hueSlider.setPaintTicks(true);
        hueSlider.setPaintLabels(true);
        hueSlider.setFont(valueFont);
        hueValueLabel = new JLabel(String.format("Hue: %.0f°", lightBulb.getHue()));
        hueValueLabel.setFont(valueFont);

        hueSlider.addChangeListener(e -> {
            float newHue = hueSlider.getValue();
            lightBulb.setHue(newHue);
            hueValueLabel.setText(String.format("Hue: %.0f°", newHue));
            updateColorPreview();
        });

        // Saturation Slider (0-100, mapped to 0-1.0f)
        saturationSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (lightBulb.getSaturation() * 100));
        saturationSlider.setMajorTickSpacing(25);
        saturationSlider.setMinorTickSpacing(5);
        saturationSlider.setPaintTicks(true);
        saturationSlider.setPaintLabels(true);
        saturationSlider.setFont(valueFont);
        saturationValueLabel = new JLabel(String.format("Saturation: %.0f%%", lightBulb.getSaturation() * 100));
        saturationValueLabel.setFont(valueFont);

        saturationSlider.addChangeListener(e -> {
            float newSaturation = saturationSlider.getValue() / 100.0f;
            lightBulb.setSaturation(newSaturation);
            saturationValueLabel.setText(String.format("Saturation: %.0f%%", newSaturation * 100));
            updateColorPreview();
        });

        // Value Slider (Brightness) (0-100, mapped to 0-1.0f)
        valueSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (lightBulb.getValue() * 100));
        valueSlider.setMajorTickSpacing(25);
        valueSlider.setMinorTickSpacing(5);
        valueSlider.setPaintTicks(true);
        valueSlider.setPaintLabels(true);
        valueSlider.setFont(valueFont);
        valueValueLabel = new JLabel(String.format("Brightness: %.0f%%", lightBulb.getValue() * 100));
        valueValueLabel.setFont(valueFont);

        valueSlider.addChangeListener(e -> {
            float newValue = valueSlider.getValue() / 100.0f;
            lightBulb.setValue(newValue);
            valueValueLabel.setText(String.format("Brightness: %.0f%%", newValue * 100));
            updateColorPreview();
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
        gbc.gridwidth = 1; // Reset gridwidth

        // Color Preview
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        add(colorPreviewPanel, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; // Reset

        // Hue Control
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Hue:"), gbc);
        gbc.gridx = 1;
        add(hueValueLabel, gbc);
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        add(hueSlider, gbc);
        row++;
        gbc.gridwidth = 1; // Reset

        // Saturation Control
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Saturation:"), gbc);
        gbc.gridx = 1;
        add(saturationValueLabel, gbc);
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        add(saturationSlider, gbc);
        row++;
        gbc.gridwidth = 1; // Reset

        // Brightness (Value) Control
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Brightness:"), gbc);
        gbc.gridx = 1;
        add(valueValueLabel, gbc);
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        add(valueSlider, gbc);
        row++;
        gbc.gridwidth = 1; // Reset

        // Close Button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(closeButton, gbc);
    }

    private void updateDisplay() {
        currentStatusLabel.setText("Status: " + lightBulb.getStatus());
        simulationStatusLabel.setText("Simulation: " + (lightBulb.isSimulating() ? "Running" : "Stopped"));
        updateColorPreview();

        // Upewnij się, że suwaki odzwierciedlają aktualny stan urządzenia
        // bez wywoływania listenerów, co mogłoby spowodować pętlę.
        hueSlider.removeChangeListener(hueSlider.getChangeListeners()[0]); // Tymczasowo usuń listener
        hueSlider.setValue((int) lightBulb.getHue());
        hueSlider.addChangeListener(e -> { // Dodaj listener z powrotem
            float newHue = hueSlider.getValue();
            lightBulb.setHue(newHue);
            hueValueLabel.setText(String.format("Hue: %.0f°", newHue));
            updateColorPreview();
        });
        hueValueLabel.setText(String.format("Hue: %.0f°", lightBulb.getHue()));

        saturationSlider.removeChangeListener(saturationSlider.getChangeListeners()[0]);
        saturationSlider.setValue((int) (lightBulb.getSaturation() * 100));
        saturationSlider.addChangeListener(e -> {
            float newSaturation = saturationSlider.getValue() / 100.0f;
            lightBulb.setSaturation(newSaturation);
            saturationValueLabel.setText(String.format("Saturation: %.0f%%", newSaturation * 100));
            updateColorPreview();
        });
        saturationValueLabel.setText(String.format("Saturation: %.0f%%", lightBulb.getSaturation() * 100));

        valueSlider.removeChangeListener(valueSlider.getChangeListeners()[0]);
        valueSlider.setValue((int) (lightBulb.getValue() * 100));
        valueSlider.addChangeListener(e -> {
            float newValue = valueSlider.getValue() / 100.0f;
            lightBulb.setValue(newValue);
            valueValueLabel.setText(String.format("Brightness: %.0f%%", newValue * 100));
            updateColorPreview();
        });
        valueValueLabel.setText(String.format("Brightness: %.0f%%", lightBulb.getValue() * 100));

        // Włącz/wyłącz suwaki w zależności od statusu żarówki
        boolean isLightOn = lightBulb.isOn();
        hueSlider.setEnabled(isLightOn);
        saturationSlider.setEnabled(isLightOn);
        valueSlider.setEnabled(isLightOn);
    }

    private void updateColorPreview() {
        if (lightBulb.isOn()) {
            colorPreviewPanel.setBackground(lightBulb.getRGBColor());
        } else {
            colorPreviewPanel.setBackground(Color.BLACK); // Gdy wyłączona, kolor czarny
        }
    }
}