package GUI;

import Enums.RoomType;
import House.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

public class AddRoomDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<RoomType> roomTypeComboBox;
    private JTextField lengthField;
    private JTextField widthField;

    private Room newRoom;


    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color ACCENT_COLOR = new Color(100, 149, 237);
    private static final Color TEXT_COLOR = new Color(40, 40, 40);
    private static final Color BORDER_COLOR = new Color(170, 170, 170);

    public AddRoomDialog(JFrame parent) {
        super(parent, "Add New Room", true);

        setSize(850, 850);
        setMinimumSize(new Dimension(600, 450));
        setLayout(new BorderLayout(25, 25));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(SECONDARY_COLOR);

        initComponents();
        createLayout();
    }

    private void initComponents() {

        Font inputFont = new Font("Segoe UI", Font.PLAIN, 20);

        nameField = new JTextField(20);
        roomTypeComboBox = new JComboBox<>(RoomType.values());
        lengthField = new JTextField(10);
        widthField = new JTextField(10);


        JTextField[] textFields = {nameField, lengthField, widthField};
        for (JTextField field : textFields) {
            field.setFont(inputFont);
            field.setBackground(Color.WHITE);
            field.setForeground(TEXT_COLOR);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }

        roomTypeComboBox.setFont(inputFont);
        roomTypeComboBox.setBackground(Color.WHITE);
        roomTypeComboBox.setForeground(TEXT_COLOR);
        roomTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                return label;
            }
        });
    }

    private void createLayout() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(SECONDARY_COLOR);
        formPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 15, 18, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 20);
        Font dialogTitleFont = new Font("Segoe UI", Font.BOLD, 34);

        JLabel dialogTitle = new JLabel("Add a New Room", SwingConstants.CENTER);
        dialogTitle.setFont(dialogTitleFont);
        dialogTitle.setForeground(PRIMARY_COLOR.darker());
        dialogTitle.setBorder(new EmptyBorder(20, 0, 30, 0));

        add(dialogTitle, BorderLayout.NORTH);

        Consumer<String> addLabel = (text) -> {
            JLabel label = new JLabel(text);
            label.setFont(labelFont);
            label.setForeground(TEXT_COLOR);
            formPanel.add(label, gbc);
        };

        gbc.gridx = 0;
        gbc.gridy = 0;
        addLabel.accept("Room Name:");
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        addLabel.accept("Room Type:");
        gbc.gridx = 1;
        formPanel.add(roomTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        addLabel.accept("Length [meters]:");
        gbc.gridx = 1;
        formPanel.add(lengthField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        addLabel.accept("Width [meters]:");
        gbc.gridx = 1;
        formPanel.add(widthField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonPanel.setBackground(SECONDARY_COLOR);

        JButton okButton = createStyledButton("Add Room", new Font("Segoe UI", Font.BOLD, 22), PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        JButton cancelButton = createStyledButton("Cancel", new Font("Segoe UI", Font.BOLD, 22), Color.GRAY, Color.DARK_GRAY.brighter(), Color.WHITE);

        okButton.addActionListener(e -> {
            if (validateAndProcessInput()) {
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            newRoom = null;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
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

    private boolean validateAndProcessInput() {
        String name = nameField.getText().trim();
        String lengthText = lengthField.getText().trim();
        String widthText = widthField.getText().trim();

        if (name.isEmpty() || lengthText.isEmpty() || widthText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double length = Double.parseDouble(lengthText);
            double width = Double.parseDouble(widthText);

            if (length <= 0 || width <= 0) {
                JOptionPane.showMessageDialog(this, "Length and width must be positive numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            RoomType roomType = (RoomType) roomTypeComboBox.getSelectedItem();
            newRoom = new Room(name, roomType, length, width);
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for length and width.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public Room getNewRoom() {
        return newRoom;
    }
}
