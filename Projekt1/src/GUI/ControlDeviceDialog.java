package GUI;

import Devices.SmartDevice;
import Enums.Status;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import Logger.DeviceLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ControlDeviceDialog extends JDialog {
    private SmartDevice device;
    private JLabel deviceNameLabel;
    private JLabel statusLabel;
    private JButton togglePowerButton;
    private JButton simulateButton;
    private JButton stopSimulateButton;
    private JButton openDeviceMenuButton;

    private List<SmartDevice> allSystemDevices;

    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = new Color(40, 40, 40);
    private static final Color BORDER_COLOR = new Color(170, 170, 170);

    public ControlDeviceDialog(JFrame parent, SmartDevice device, List<SmartDevice> allSystemDevices) {
        super(parent, "Control Device: " + device.getName(), true);
        this.device = device;
        this.allSystemDevices = allSystemDevices;

        setSize(850, 850);
        setMinimumSize(new Dimension(500, 400));

        setLayout(new BorderLayout(20, 20));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(SECONDARY_COLOR);

        initComponents();
        createLayout();
        updateStatusDisplay();
    }

    private void initComponents() {
        Font titleFont = new Font("Segoe UI", Font.BOLD, 30);
        Font statusFont = new Font("Segoe UI", Font.PLAIN, 20);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 20);

        deviceNameLabel = new JLabel("Device: " + device.getName(), SwingConstants.CENTER);
        deviceNameLabel.setFont(titleFont);
        deviceNameLabel.setForeground(PRIMARY_COLOR.darker());

        statusLabel = new JLabel("Status: " + device.getStatus() + " | ID: " + device.getId(), SwingConstants.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(TEXT_COLOR);

        togglePowerButton = createStyledButton(device.isOn() ? "Turn OFF" : "Turn ON", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        simulateButton = createStyledButton("Start Simulation", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        stopSimulateButton = createStyledButton("Stop Simulation", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        openDeviceMenuButton = createStyledButton("Open Device Specific Menu", buttonFont, PRIMARY_COLOR.darker(), ACCENT_COLOR.darker(), Color.WHITE);

        togglePowerButton.addActionListener(e -> togglePower());
        simulateButton.addActionListener(e -> startSimulation());
        stopSimulateButton.addActionListener(e -> stopSimulation());
        openDeviceMenuButton.addActionListener(e -> openDeviceSpecificMenu());
    }

    private void createLayout() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SECONDARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        headerPanel.add(deviceNameLabel, BorderLayout.NORTH);
        headerPanel.add(statusLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        buttonPanel.setBackground(SECONDARY_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        buttonPanel.add(togglePowerButton);
        buttonPanel.add(simulateButton);
        buttonPanel.add(stopSimulateButton);
        buttonPanel.add(openDeviceMenuButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private void updateStatusDisplay() {
        statusLabel.setText("Status: " + device.getStatus() + " | ID: " + device.getId());
        togglePowerButton.setText(device.isOn() ? "Turn OFF" : "Turn ON");
    }

    private void togglePower() {
        device.setStatus(device.isOn() ? Status.OFF : Status.ON);
        updateStatusDisplay();
        DeviceLogger.logStatusChange(device, "Turned " + (device.isOn() ? "ON" : "OFF"));
    }

    private void startSimulation() {
        try {
            device.simulate();
            updateStatusDisplay();
            DeviceLogger.logEvent(device, "SIMULATION_STATUS", "Simulation started");
            JOptionPane.showMessageDialog(this, "Simulation started for " + device.getName(), "Simulation", JOptionPane.INFORMATION_MESSAGE);
        } catch (DeviceDisabled e) {
            JOptionPane.showMessageDialog(this, "Device is disabled and cannot be simulated: " + e.getMessage(), "Simulation Error", JOptionPane.ERROR_MESSAGE);
        } catch (SimulationInterrupted e) {
            JOptionPane.showMessageDialog(this, "Simulation interrupted for " + device.getName() + ": " + e.getMessage(), "Simulation Interrupted", JOptionPane.WARNING_MESSAGE);
        } catch (UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(this, "This device does not support simulation.", "Simulation Not Supported", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void stopSimulation() {
        try {
            device.stopSimulation();
            updateStatusDisplay();
            DeviceLogger.logEvent(device, "SIMULATION_STATUS", "Simulation stopped");
            JOptionPane.showMessageDialog(this, "Simulation stopped for " + device.getName(), "Simulation", JOptionPane.INFORMATION_MESSAGE);
        } catch (UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(this, "This device does not support simulation.", "Simulation Not Supported", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openDeviceSpecificMenu() {
        device.showDeviceSpecificGUI((JFrame) getParent(), allSystemDevices);
        updateStatusDisplay();
    }

    private JButton createStyledButton(String text, Font font, Color normalBg, Color hoverBg, Color textCol) {
        JButton button = new JButton(text) {
            private final int CORNER_RADIUS = 20;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int width = getWidth();
                int height = getHeight();

                int shadowOffset = 3;
                Color shadowColor = new Color(0, 0, 0, 40);
                if (getModel().isPressed()) {
                    shadowColor = new Color(0, 0, 0, 80);
                    shadowOffset = 2;
                }
                g2.setColor(shadowColor);
                g2.fillRoundRect(shadowOffset, shadowOffset, width - shadowOffset, height - shadowOffset, CORNER_RADIUS, CORNER_RADIUS);

                if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                } else {
                    g2.setColor(normalBg);
                }
                g2.fillRoundRect(0, 0, width - 1, height - 1, CORNER_RADIUS, CORNER_RADIUS);

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
