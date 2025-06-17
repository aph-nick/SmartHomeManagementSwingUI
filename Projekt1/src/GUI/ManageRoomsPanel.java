package GUI;

import Devices.Outlet;
import Enums.RoomType;
import House.Room;
import House.SmartHomeSystem;
import Devices.SmartDevice;
import House.House; // Upewnij się, że masz ten import, jeśli House.House jest oddzielną klasą

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

public class ManageRoomsPanel extends JPanel {
    private final SmartHomeSystem system;
    private final SmartHomeGUI parentFrame;
    private final ManageHousePanel backPanel; // Panel, do którego wracamy
    private final House selectedHouse; // Dom, którego pokojami zarządzamy

    private JLabel houseNameLabel;
    private JButton addRoomButton;
    private JButton editRoomButton;
    private JButton removeRoomButton;
    private JButton listRoomsButton;
    private JButton manageDevicesInRoomButton;
    private JButton backButton;
    private JLabel statusLabel;

    // --- Kolory do schematu UI (powtórzone dla samodzielności klasy) ---
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Stalowo-niebieski
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Alice Blue (jasny)
    private static final Color ACCENT_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private static final Color TEXT_COLOR = new Color(40, 40, 40); // Ciemny szary
    private static final Color BORDER_COLOR = new Color(170, 170, 170); // Szary do obramowań

    // Zaktualizowany konstruktor, aby przyjmować selectedHouse
    public ManageRoomsPanel(SmartHomeSystem system, SmartHomeGUI parentFrame, ManageHousePanel backPanel, House selectedHouse) {
        this.system = system;
        this.parentFrame = parentFrame;
        this.backPanel = backPanel;
        this.selectedHouse = selectedHouse; // Teraz selectedHouse jest przekazywany

        if (selectedHouse == null) {
            JOptionPane.showMessageDialog(this, "No house selected. Returning to main menu.", "Error", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(parentFrame::showMainPanel);
            return;
        }

        initializeUI();
    }

    private void initializeUI() {
        // Zmieniamy na GridBagLayout dla większej elastyczności
        setLayout(new GridBagLayout());
        setBackground(SECONDARY_COLOR); // Ustawienie tła panelu
        setBorder(new EmptyBorder(40, 50, 40, 50)); // Dodatkowy padding wokół panelu

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10); // Większe marginesy między komponentami
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Rozciągnij komponenty w poziomie

        Font titleFont = new Font("Segoe UI", Font.BOLD, 36);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 24);
        Font statusFont = new Font("Segoe UI", Font.ITALIC, 20);

        // House Name Label
        houseNameLabel = new JLabel("Rooms in: " + selectedHouse.getName(), SwingConstants.CENTER);
        houseNameLabel.setFont(titleFont);
        houseNameLabel.setForeground(PRIMARY_COLOR.darker()); // Używamy ciemniejszego primary color

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1; // Zajmuje całą szerokość w pojedynczej kolumnie
        gbc.ipady = 20; // Internal padding vertical
        add(houseNameLabel, gbc);

        gbc.ipady = 10; // Resetuj padding pionowy dla przycisków

        // Przyciski (używamy createStyledButton)
        addRoomButton = createStyledButton("1. Add Room", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        editRoomButton = createStyledButton("2. Edit Room", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        removeRoomButton = createStyledButton("3. Remove Room", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        listRoomsButton = createStyledButton("4. List Rooms", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        manageDevicesInRoomButton = createStyledButton("5. Manage Devices in a Room", buttonFont, PRIMARY_COLOR, ACCENT_COLOR, Color.WHITE);
        backButton = createStyledButton("6. Back to House Management", buttonFont, Color.GRAY, Color.DARK_GRAY.brighter(), Color.WHITE); // Inny kolor dla przycisku Wstecz

        gbc.gridy = 1; add(addRoomButton, gbc);
        gbc.gridy = 2; add(editRoomButton, gbc);
        gbc.gridy = 3; add(removeRoomButton, gbc);
        gbc.gridy = 4; add(listRoomsButton, gbc);
        gbc.gridy = 5; add(manageDevicesInRoomButton, gbc);
        gbc.gridy = 6; add(backButton, gbc);

        // Status Label
        statusLabel = new JLabel("Manage rooms for " + selectedHouse.getName(), SwingConstants.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(TEXT_COLOR);

        gbc.gridy = 7;
        gbc.ipady = 15; // Trochę więcej paddingu dla statusu
        add(statusLabel, gbc);

        // Dodanie słuchaczy zdarzeń
        addRoomButton.addActionListener(e -> addRoom());
        editRoomButton.addActionListener(e -> editRoom());
        removeRoomButton.addActionListener(e -> removeRoom());
        listRoomsButton.addActionListener(e -> listRooms());
        manageDevicesInRoomButton.addActionListener(e -> manageDevicesInRoom());
        backButton.addActionListener(e -> parentFrame.showPanel(backPanel));
    }

    // --- Metody obsługujące akcje ---

    private void addRoom() {
        // Stylowanie dla AddRoomDialog (przykładowe, zakładając istnienie klasy)
        // Musisz upewnić się, że AddRoomDialog ma podobne ustawienia kolorów i czcionek w swojej implementacji
        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 18));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        // Assuming AddRoomDialog exists and its components are styled internally
        AddRoomDialog dialog = new AddRoomDialog(parentFrame);
        dialog.setVisible(true);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        Room newRoom = dialog.getNewRoom();
        if (newRoom != null) {
            selectedHouse.addRoom(newRoom);
            statusLabel.setText("Room '" + newRoom.getName() + "' added successfully!");
        } else {
            statusLabel.setText("Add Room operation cancelled or incomplete.");
        }
    }

    private void editRoom() {
        List<Room> rooms = selectedHouse.getRooms();
        if (rooms.isEmpty()) {
            statusLabel.setText("No rooms to edit.");
            JOptionPane.showMessageDialog(this, "No rooms to edit in " + selectedHouse.getName() + ".", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 18);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 18);

        // Użyj DefaultListCellRenderer, aby ostylować elementy w ComboBox
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Dodaj padding
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR); // Kolor zaznaczenia
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                return label;
            }
        };

        JComboBox<String> roomComboBox = new JComboBox<>(rooms.stream().map(Room::getName).toArray(String[]::new));
        roomComboBox.setRenderer(renderer);
        roomComboBox.setFont(dialogInputFont);
        roomComboBox.setBackground(Color.WHITE);
        roomComboBox.setForeground(TEXT_COLOR);
        roomComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select room to edit:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(roomComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Room",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);


        if (result == JOptionPane.OK_OPTION) {
            String selectedRoomName = (String) roomComboBox.getSelectedItem();
            Optional<Room> roomToEditOpt = rooms.stream()
                    .filter(r -> r.getName().equals(selectedRoomName))
                    .findFirst();

            if (roomToEditOpt.isPresent()) {
                Room roomToEdit = roomToEditOpt.get();
                // Assuming EditRoomDialog exists and its components are styled internally
                EditRoomDialog dialog = new EditRoomDialog(parentFrame, roomToEdit);
                dialog.setVisible(true);

                if (dialog.isUpdated()) {
                    statusLabel.setText("Room '" + roomToEdit.getName() + "' updated successfully!");
                } else {
                    statusLabel.setText("Room editing cancelled or no changes made.");
                }
            }
        } else {
            statusLabel.setText("Room selection cancelled.");
        }
    }

    private void removeRoom() {
        List<Room> rooms = selectedHouse.getRooms();
        if (rooms.isEmpty()) {
            statusLabel.setText("No rooms to remove.");
            JOptionPane.showMessageDialog(this, "No rooms to remove in " + selectedHouse.getName() + ".", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 18);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 18);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Dodaj padding
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR); // Kolor zaznaczenia
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                return label;
            }
        };

        JComboBox<String> roomComboBox = new JComboBox<>(rooms.stream().map(Room::getName).toArray(String[]::new));
        roomComboBox.setRenderer(renderer);
        roomComboBox.setFont(dialogInputFont);
        roomComboBox.setBackground(Color.WHITE);
        roomComboBox.setForeground(TEXT_COLOR);
        roomComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select room to remove:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(roomComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Remove Room",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);


        if (result == JOptionPane.OK_OPTION) {
            String selectedRoomName = (String) roomComboBox.getSelectedItem();
            Optional<Room> roomToRemoveOpt = rooms.stream()
                    .filter(r -> r.getName().equals(selectedRoomName))
                    .findFirst();

            if (roomToRemoveOpt.isPresent()) {
                Room roomToRemove = roomToRemoveOpt.get();
                int confirmResult = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to remove room '" + selectedRoomName + "'?",
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirmResult == JOptionPane.YES_OPTION) {
                    // Przed usunięciem pokoju, upewnij się, że wszystkie urządzenia w nim są odłączone/zatrzymane.
                    // To jest kluczowe dla integralności danych i symulacji.
                    for (SmartDevice device : roomToRemove.getDevices()) {
                        try {
                            if (device instanceof Interfaces.Pluggable pluggableDevice) {
                                Outlet connectedOutlet = pluggableDevice.getOutlet();
                                if (connectedOutlet != null) {
                                    pluggableDevice.disconnectFromOutlet(connectedOutlet);
                                    Logger.DeviceLogger.logEvent(device, "CONNECTION", "Disconnected from outlet during room removal.");
                                }
                            }
                            device.stopSimulation();
                        } catch (UnsupportedOperationException e) {
                            // Ignoruj, jeśli urządzenie nie obsługuje symulacji/odłączania
                        } catch (Exception e) {
                            Logger.DeviceLogger.logEvent(device, "ERROR", "Failed to clean up device " + device.getName() + " during room removal: " + e.getMessage());
                        }
                    }
                    roomToRemove.getDevices().clear(); // Usuń wszystkie urządzenia z pokoju przed usunięciem pokoju

                    selectedHouse.removeRoom(roomToRemove);
                    statusLabel.setText("Room '" + selectedRoomName + "' removed successfully!");
                } else {
                    statusLabel.setText("Room removal cancelled.");
                }
            }
        } else {
            statusLabel.setText("Room selection cancelled.");
        }
    }

    private void listRooms() {
        List<Room> rooms = selectedHouse.getRooms();
        if (rooms.isEmpty()) {
            statusLabel.setText("No rooms in " + selectedHouse.getName() + ".");
            // Ustawianie stylów JOptionPane dla komunikatu
            UIManager.put("OptionPane.background", SECONDARY_COLOR);
            UIManager.put("Panel.background", SECONDARY_COLOR);
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

            JOptionPane.showMessageDialog(this, "No rooms available in " + selectedHouse.getName() + ".", "Info", JOptionPane.INFORMATION_MESSAGE);

            // Przywracanie domyślnych stylów
            UIManager.put("OptionPane.background", null);
            UIManager.put("Panel.background", null);
            UIManager.put("OptionPane.messageFont", null);
            UIManager.put("OptionPane.buttonFont", null);
            return;
        }

        StringBuilder sb = new StringBuilder("Rooms in " + selectedHouse.getName() + ":\n\n");
        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            sb.append((i + 1)).append(". Name: ").append(room.getName()).append("\n")
                    .append("   Type: ").append(room.getRoomType()).append("\n")
                    .append("   Area: ").append(String.format("%.2f", room.getArea())).append(" sq. meters\n")
                    .append("   Devices: ").append(room.getDevices().size()).append("\n");
            if (!room.getDevices().isEmpty()) {
                sb.append("     Devices in room:\n");
                for (SmartDevice device : room.getDevices()) {
                    sb.append("       - ").append(device.getName()).append(" (ID: ").append(device.getId()).append(", On: ").append(device.isOn()).append(")\n");
                }
            }
            sb.append("\n");
        }
        statusLabel.setText("Rooms listed for " + selectedHouse.getName() + ".");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16)); // Czcionka dla listy
        textArea.setEditable(false);
        textArea.setBackground(SECONDARY_COLOR.brighter()); // Jasne tło dla text area
        textArea.setForeground(TEXT_COLOR); // Ciemny tekst

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(850, 850)); // Większy rozmiar okna listy
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1)); // Delikatne obramowanie

        // Stylowanie JOptionPane dla wyświetlania listy
        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 16)); // Czcionka dla wiadomości w oknie
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16)); // Czcionka dla przycisków w oknie

        JOptionPane.showMessageDialog(this, scrollPane, "Rooms in " + selectedHouse.getName(), JOptionPane.PLAIN_MESSAGE);

        // Przywracanie domyślnych stylów
        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);
    }


    // Ta metoda zastąpi starą listDevicesInRoom i będzie przechodzić do ManageDevicesPanel
    private void manageDevicesInRoom() {
        List<Room> rooms = selectedHouse.getRooms();
        if (rooms.isEmpty()) {
            statusLabel.setText("No rooms available to manage devices in.");
            // Ustawianie stylów JOptionPane dla komunikatu
            UIManager.put("OptionPane.background", SECONDARY_COLOR);
            UIManager.put("Panel.background", SECONDARY_COLOR);
            UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 18));
            UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

            JOptionPane.showMessageDialog(this, "No rooms available. Please add a room first.", "Info", JOptionPane.INFORMATION_MESSAGE);

            // Przywracanie domyślnych stylów
            UIManager.put("OptionPane.background", null);
            UIManager.put("Panel.background", null);
            UIManager.put("OptionPane.messageFont", null);
            UIManager.put("OptionPane.buttonFont", null);
            return;
        }

        Font dialogInputFont = new Font("Segoe UI", Font.PLAIN, 18);
        Font dialogLabelFont = new Font("Segoe UI", Font.BOLD, 18);

        // Użyj DefaultListCellRenderer, aby ostylować elementy w ComboBox
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(dialogInputFont);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Dodaj padding
                if (isSelected) {
                    label.setBackground(ACCENT_COLOR); // Kolor zaznaczenia
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT_COLOR);
                }
                return label;
            }
        };

        JComboBox<String> roomComboBox = new JComboBox<>(rooms.stream().map(Room::getName).toArray(String[]::new));
        roomComboBox.setRenderer(renderer);
        roomComboBox.setFont(dialogInputFont);
        roomComboBox.setBackground(Color.WHITE);
        roomComboBox.setForeground(TEXT_COLOR);
        roomComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(SECONDARY_COLOR);
        JLabel label = new JLabel("Select room to manage devices in:");
        label.setFont(dialogLabelFont);
        label.setForeground(TEXT_COLOR);
        panel.add(label, BorderLayout.NORTH);
        panel.add(roomComboBox, BorderLayout.CENTER);

        UIManager.put("OptionPane.background", SECONDARY_COLOR);
        UIManager.put("Panel.background", SECONDARY_COLOR);
        UIManager.put("OptionPane.messageFont", dialogInputFont);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 16));

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Manage Devices in Room",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        UIManager.put("OptionPane.background", null);
        UIManager.put("Panel.background", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        if (result == JOptionPane.OK_OPTION) {
            String selectedRoomName = (String) roomComboBox.getSelectedItem();
            Optional<Room> selectedRoomOpt = rooms.stream()
                    .filter(r -> r.getName().equals(selectedRoomName))
                    .findFirst();

            if (selectedRoomOpt.isPresent()) {
                Room roomForDevices = selectedRoomOpt.get();
                statusLabel.setText("Navigating to device management for room: " + roomForDevices.getName());
                // Przełączamy się na ManageDevicesPanel
                parentFrame.showPanel(new ManageDevicesPanel(system, parentFrame, this, roomForDevices));
            }
        } else {
            statusLabel.setText("Room selection cancelled.");
        }
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
}