package GUI;

import Enums.HouseColor;
import Enums.HouseType;
import House.Localisation;
import House.House;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class AddHouseDialog extends JDialog {
    private JTextField nameField;
    private JTextField xField;
    private JTextField yField;
    private JTextField zField;
    private JComboBox<HouseType> houseTypeComboBox;
    private JComboBox<HouseColor> houseColorComboBox;

    private House newHouse;


    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); 
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); 
    private static final Color ACCENT_COLOR = new Color(100, 149, 237); 
    private static final Color TEXT_COLOR = new Color(40, 40, 40); 
    private static final Color BORDER_COLOR = new Color(170, 170, 170); 

    public AddHouseDialog(JFrame parent) {
        super(parent, "Add New House", true);
        setSize(850, 850);
        setMinimumSize(new Dimension(700, 500));
        setLayout(new BorderLayout(25, 25));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(SECONDARY_COLOR);

        initComponents();
        createLayout();
    }

    private void initComponents() {
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 22);

        nameField = new JTextField(20);
        xField = new JTextField(10);
        yField = new JTextField(10);
        zField = new JTextField(10);

        houseTypeComboBox = new JComboBox<>(HouseType.values());
        houseColorComboBox = new JComboBox<>(HouseColor.values());

        JTextField[] textFields = {nameField, xField, yField, zField};
        for (JTextField field : textFields) {
            field.setFont(inputFont);
            field.setBackground(Color.WHITE);
            field.setForeground(TEXT_COLOR);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, 45));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 
        }

        JComboBox[] comboBoxes = {houseTypeComboBox, houseColorComboBox};
        for (JComboBox comboBox : comboBoxes) {
            comboBox.setFont(inputFont);
            comboBox.setBackground(Color.WHITE);
            comboBox.setForeground(TEXT_COLOR);
            comboBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 45));
            comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 
            comboBox.setRenderer(new DefaultListCellRenderer() {
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
    }

    private void createLayout() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(SECONDARY_COLOR);
        formPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; 

        Font labelFont = new Font("Segoe UI", Font.BOLD, 22);
        Font dialogTitleFont = new Font("Segoe UI", Font.BOLD, 36);

        JLabel dialogTitle = new JLabel("Add a New Smart House", SwingConstants.CENTER);
        dialogTitle.setFont(dialogTitleFont);
        dialogTitle.setForeground(PRIMARY_COLOR.darker());
        dialogTitle.setBorder(new EmptyBorder(20, 0, 30, 0));

        add(dialogTitle, BorderLayout.NORTH);

        int row = 0;

        // House Name
        gbc.gridx = 0; 
        gbc.gridy = row; 
        gbc.weighty = 0.0; 
        gbc.anchor = GridBagConstraints.WEST; 
        JLabel nameLabel = new JLabel("House Name:");
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(TEXT_COLOR);
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1; 
        gbc.gridy = row; 
        gbc.weighty = 0.0; 
        formPanel.add(nameField, gbc);
        row++;

        // Location X
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel xLabel = new JLabel("Location X:");
        xLabel.setFont(labelFont);
        xLabel.setForeground(TEXT_COLOR);
        formPanel.add(xLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        formPanel.add(xField, gbc);
        row++;

        // Location Y
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel yLabel = new JLabel("Location Y:");
        yLabel.setFont(labelFont);
        yLabel.setForeground(TEXT_COLOR);
        formPanel.add(yLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        formPanel.add(yField, gbc);
        row++;

        // Location Z
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel zLabel = new JLabel("Location Z:");
        zLabel.setFont(labelFont);
        zLabel.setForeground(TEXT_COLOR);
        formPanel.add(zLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        formPanel.add(zField, gbc);
        row++;

        // House Type
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel typeLabel = new JLabel("House Type:");
        typeLabel.setFont(labelFont);
        typeLabel.setForeground(TEXT_COLOR);
        formPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        formPanel.add(houseTypeComboBox, gbc);
        row++;

        // House Color
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel colorLabel = new JLabel("House Color:");
        colorLabel.setFont(labelFont);
        colorLabel.setForeground(TEXT_COLOR);
        formPanel.add(colorLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weighty = 0.0;
        formPanel.add(houseColorComboBox, gbc);
        row++;

        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2; 
        gbc.weighty = 1.0; 
        formPanel.add(Box.createVerticalGlue(), gbc); 

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonPanel.setBackground(SECONDARY_COLOR);

        JButton okButton = createStyledButton("Add House", new Font("Segoe UI", Font.BOLD, 22), PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        JButton cancelButton = createStyledButton("Cancel", new Font("Segoe UI", Font.BOLD, 22), Color.GRAY, Color.DARK_GRAY.brighter(), Color.WHITE);

        okButton.addActionListener(e -> {
            if (validateAndProcessInput()) {
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            newHouse = null;
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
        String xText = xField.getText().trim();
        String yText = yField.getText().trim();
        String zText = zField.getText().trim();

        if (name.isEmpty() || xText.isEmpty() || yText.isEmpty() || zText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double x = Double.parseDouble(xText);
            double y = Double.parseDouble(yText);
            double z = Double.parseDouble(zText);

            HouseType type = (HouseType) houseTypeComboBox.getSelectedItem();
            HouseColor color = (HouseColor) houseColorComboBox.getSelectedItem();

            Localisation localisation = new Localisation(x, y, z);
            newHouse = new House(name, localisation, type, color);
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for X, Y, Z coordinates.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public House getNewHouse() {
        return newHouse;
    }
}
