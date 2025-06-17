package GUI;

import Enums.RoomType;
import House.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D; // Ważne: import dla RoundRectangle2D
import java.util.function.Consumer;

public class EditRoomDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<RoomType> roomTypeComboBox;
    private JTextField lengthField;
    private JTextField widthField;

    private Room roomToEdit;
    private boolean updated = false;

    // --- Kolory do schematu UI (powtórzone dla samodzielności klasy) ---
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Stalowo-niebieski
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Alice Blue (jasny)
    private static final Color ACCENT_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private static final Color TEXT_COLOR = new Color(40, 40, 40); // Ciemny szary
    private static final Color BORDER_COLOR = new Color(170, 170, 170); // Szary do obramowań

    public EditRoomDialog(JFrame parent, Room roomToEdit) {
        super(parent, "Edit Room: " + roomToEdit.getName(), true);
        this.roomToEdit = roomToEdit;

        // Zwiększamy rozmiar dialogu
        setSize(850, 850); // Większy rozmiar
        setMinimumSize(new Dimension(600, 450)); // Minimalny rozmiar

        setLayout(new BorderLayout(25, 25)); // Zwiększono odstępy
        setLocationRelativeTo(parent);
        getContentPane().setBackground(SECONDARY_COLOR); // Tło dialogu

        initComponents();
        populateFields();
        createLayout();
    }

    private void initComponents() {
        // Ustawienie większej czcionki dla wszystkich komponentów wejściowych
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 20); // Nowocześniejsza czcionka

        nameField = new JTextField(20);
        roomTypeComboBox = new JComboBox<>(RoomType.values());
        lengthField = new JTextField(10);
        widthField = new JTextField(10);

        // Stylowanie pól tekstowych i ComboBoxów
        JTextField[] textFields = {nameField, lengthField, widthField};
        for (JTextField field : textFields) {
            field.setFont(inputFont);
            field.setBackground(Color.WHITE);
            field.setForeground(TEXT_COLOR);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10) // Wewnętrzny padding
            ));
        }

        roomTypeComboBox.setFont(inputFont);
        roomTypeComboBox.setBackground(Color.WHITE);
        roomTypeComboBox.setForeground(TEXT_COLOR);
        roomTypeComboBox.setRenderer(new DefaultListCellRenderer() { // Poprawienie renderera dla wyglądu
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Dodaj padding do elementów listy
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR); // Kolor zaznaczenia
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                return label;
            }
        });
    }

    private void populateFields() {
        nameField.setText(roomToEdit.getName());
        roomTypeComboBox.setSelectedItem(roomToEdit.getRoomType());
        lengthField.setText(String.valueOf(roomToEdit.getLength()));
        widthField.setText(String.valueOf(roomToEdit.getWidth()));
    }

    private void createLayout() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(SECONDARY_COLOR);
        formPanel.setBorder(new EmptyBorder(30, 30, 30, 30)); // Dodatkowy padding wokół panelu formularza

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 15, 18, 15); // Zwiększone marginesy
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Rozciągnij pola w poziomie

        Font labelFont = new Font("Segoe UI", Font.BOLD, 20); // Czcionka dla etykiet
        Font dialogTitleFont = new Font("Segoe UI", Font.BOLD, 34); // Czcionka dla tytułu dialogu

        // Nagłówek dialogu
        JLabel dialogTitle = new JLabel("Edit Room: " + roomToEdit.getName(), SwingConstants.CENTER);
        dialogTitle.setFont(dialogTitleFont);
        dialogTitle.setForeground(PRIMARY_COLOR.darker());
        dialogTitle.setBorder(new EmptyBorder(20, 0, 30, 0)); // Padding na górze i dole

        add(dialogTitle, BorderLayout.NORTH);

        // Helper do dodawania etykiet z odpowiednią czcionką i kolorem
        Consumer<String> addLabel = (text) -> {
            JLabel label = new JLabel(text);
            label.setFont(labelFont);
            label.setForeground(TEXT_COLOR); // Kolor tekstu dla etykiet
            formPanel.add(label, gbc);
        };

        // Etykieta i pole Nazwa Pokoju
        gbc.gridx = 0;
        gbc.gridy = 0;
        addLabel.accept("Room Name:");
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        // Etykieta i ComboBox Typ Pokoju
        gbc.gridx = 0;
        gbc.gridy = 1;
        addLabel.accept("Room Type:");
        gbc.gridx = 1;
        formPanel.add(roomTypeComboBox, gbc);

        // Etykieta i pole Długość
        gbc.gridx = 0;
        gbc.gridy = 2;
        addLabel.accept("Length [meters]:");
        gbc.gridx = 1;
        formPanel.add(lengthField, gbc);

        // Etykieta i pole Szerokość
        gbc.gridx = 0;
        gbc.gridy = 3;
        addLabel.accept("Width [meters]:");
        gbc.gridx = 1;
        formPanel.add(widthField, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20)); // Centrowanie przycisków, większe odstępy
        buttonPanel.setBackground(SECONDARY_COLOR); // Tło panelu przycisków

        // Używamy metody createStyledButton
        JButton saveButton = createStyledButton("Save Changes", new Font("Segoe UI", Font.BOLD, 22), PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        JButton cancelButton = createStyledButton("Cancel", new Font("Segoe UI", Font.BOLD, 22), Color.GRAY, Color.DARK_GRAY.brighter(), Color.WHITE);

        saveButton.addActionListener(e -> {
            if (validateAndApplyChanges()) {
                updated = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> {
            updated = false;
            dispose();
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
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

    private boolean validateAndApplyChanges() {
        String newName = nameField.getText().trim();
        String lengthText = lengthField.getText().trim();
        String widthText = widthField.getText().trim();

        if (newName.isEmpty() || lengthText.isEmpty() || widthText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double newLength = Double.parseDouble(lengthText);
            double newWidth = Double.parseDouble(widthText);

            if (newLength <= 0 || newWidth <= 0) {
                JOptionPane.showMessageDialog(this, "Length and width must be positive numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Apply changes only if they are different
            boolean changed = false;
            if (!roomToEdit.getName().equals(newName)) {
                roomToEdit.setName(newName);
                changed = true;
            }
            if (roomToEdit.getRoomType() != roomTypeComboBox.getSelectedItem()) {
                roomToEdit.setRoomType((RoomType) roomTypeComboBox.getSelectedItem());
                changed = true;
            }
            if (roomToEdit.getLength() != newLength) {
                roomToEdit.setLength(newLength);
                changed = true;
            }
            if (roomToEdit.getWidth() != newWidth) {
                roomToEdit.setWidth(newWidth);
                changed = true;
            }

            // Ustaw `updated` na true tylko jeśli faktycznie nastąpiła jakaś zmiana
            this.updated = changed;
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for length and width.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean isUpdated() {
        return updated;
    }
}