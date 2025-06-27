package GUI;

import House.SmartHomeSystem;
import House.House;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ManageHousePanel extends JPanel {
    private final SmartHomeSystem system;
    private final SmartHomeGUI parentFrame;
    private final House selectedHouse;

    private JLabel houseNameLabel;
    private JButton manageRoomsButton;
    private JButton backButton;
    private JLabel statusLabel;

    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = new Color(40, 40, 40);
    private static final Color BORDER_COLOR = new Color(170, 170, 170);

    public ManageHousePanel(SmartHomeSystem system, SmartHomeGUI parentFrame) {
        this.system = system;
        this.parentFrame = parentFrame;
        this.selectedHouse = system.getSelectedHouse();

        if (selectedHouse == null) {
            JOptionPane.showMessageDialog(this, "No house selected for management. Returning to main menu.", "Error", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(parentFrame::showMainPanel);
            return;
        }

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(SECONDARY_COLOR);
        setBorder(new EmptyBorder(40, 50, 40, 50));
        setSize(850, 850);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font titleFont = new Font("Segoe UI", Font.BOLD, 40);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 28);
        Font statusFont = new Font("Segoe UI", Font.ITALIC, 24);

        houseNameLabel = new JLabel("Managing House: " + selectedHouse.getName(), SwingConstants.CENTER);
        houseNameLabel.setFont(titleFont);
        houseNameLabel.setForeground(PRIMARY_COLOR.darker());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.ipady = 30;
        add(houseNameLabel, gbc);

        gbc.ipady = 15;

        manageRoomsButton = createStyledButton("1. Manage Rooms", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        backButton = createStyledButton("3. Back to Main Menu", buttonFont, Color.GRAY, Color.DARK_GRAY.brighter(), Color.WHITE);

        gbc.gridy = 1; add(manageRoomsButton, gbc);
        gbc.gridy = 2; add(backButton, gbc);

        statusLabel = new JLabel("Welcome to " + selectedHouse.getName() + " management!", SwingConstants.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(TEXT_COLOR);

        gbc.gridy = 4;
        gbc.ipady = 20;
        add(statusLabel, gbc);

        manageRoomsButton.addActionListener(e -> manageRooms());
        backButton.addActionListener(e -> parentFrame.showMainPanel());
    }

    private void manageRooms() {
        statusLabel.setText("Navigating to room management for: " + selectedHouse.getName());
        parentFrame.showPanel(new ManageRoomsPanel(system, parentFrame, this, selectedHouse));
    }


    public void setStatusMessage(String message) {
        statusLabel.setText(message);
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
