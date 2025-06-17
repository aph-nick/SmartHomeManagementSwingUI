package GUI;

import House.SmartHomeSystem;
import House.House;
import Enums.HouseColor; // Zakładam, że są używane gdzieś indziej
import Enums.HouseType; // Zakładam, że są używane gdzieś indziej
import House.Localisation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;

public class SmartHomeGUI extends JFrame {
    private final SmartHomeSystem system;

    private JPanel mainContentPanel;

    private JButton addHouseButton;
    private JButton removeHouseButton;
    private JButton listHousesButton;
    private JButton manageHouseButton;
    private JButton exitButton;
    private JLabel statusLabel;

    // --- Kolory do schematu UI ---
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Stalowo-niebieski
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Alice Blue (jasny)
    private static final Color ACCENT_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private static final Color TEXT_COLOR = new Color(40, 40, 40); // Ciemny szary
    private static final Color BORDER_COLOR = new Color(170, 170, 170); // Szary do obramowań

    public SmartHomeGUI(SmartHomeSystem system) {
        this.system = system;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Smart Home Manager");
        // Ustawienie większego domyślnego rozmiaru
        setSize(1400, 900); // Zwiększony rozmiar
        setMinimumSize(new Dimension(1000, 700)); // Minimalny rozmiar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Wyśrodkowanie okna

        // Używamy BorderLayout dla głównej ramki
        setLayout(new BorderLayout());
        getContentPane().setBackground(SECONDARY_COLOR); // Ustawienie koloru tła ramki

        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout()); // Upewnij się, że ten panel ma layout
        mainContentPanel.setBackground(SECONDARY_COLOR); // Ustawienie koloru tła

        showMainPanel(); // Na początek pokazujemy panel menu głównego

        add(mainContentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Metoda do przełączania widoków
    public void showPanel(JPanel panel) {
        mainContentPanel.removeAll();
        // Nie dodajemy BorderLayout.CENTER, bo mainContentPanel już ma BorderLayout
        mainContentPanel.add(panel); // Po prostu dodajemy panel
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // Metoda do tworzenia i wyświetlania głównego panelu menu
    public void showMainPanel() {
        JPanel mainMenuPanel = new JPanel(new GridBagLayout()); // Użyj GridBagLayout dla elastycznego centrowania
        mainMenuPanel.setBackground(SECONDARY_COLOR); // Jasne tło dla menu

        // Dodajemy pusty border dla lepszych marginesów wewnętrznych
        mainMenuPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        Font titleFont = new Font("Arial", Font.BOLD, 48); // Większy tytuł
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 26); // Nowocześniejsza czcionka dla przycisków
        Font statusFont = new Font("Arial", Font.ITALIC, 22);

        // Ustawienia dla GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Każdy komponent w nowym wierszu
        gbc.fill = GridBagConstraints.HORIZONTAL; // Rozciągnij w poziomie
        gbc.insets = new Insets(15, 0, 15, 0); // Odstępy między komponentami
        gbc.weightx = 1.0; // Rozciągaj w poziomie
        gbc.anchor = GridBagConstraints.CENTER; // Wyśrodkuj

        // Tytuł
        JLabel titleLabel = new JLabel("Smart Home Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(PRIMARY_COLOR.darker()); // Ciemniejszy odcień primary color
        gbc.insets = new Insets(0, 0, 40, 0); // Większy odstęp pod tytułem
        mainMenuPanel.add(titleLabel, gbc);
        gbc.insets = new Insets(15, 0, 15, 0); // Przywróć normalne odstępy

        // Tworzymy przyciski używając pomocniczej funkcji dla niestandardowego wyglądu
        addHouseButton = createStyledButton("1. Add House", buttonFont);
        removeHouseButton = createStyledButton("2. Remove House", buttonFont);
        listHousesButton = createStyledButton("3. List Houses", buttonFont);
        manageHouseButton = createStyledButton("4. Manage House", buttonFont);
        exitButton = createStyledButton("5. Exit", buttonFont);

        statusLabel = new JLabel("Welcome to Smart Home Manager!", SwingConstants.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Margines na górze statusu

        addHouseButton.addActionListener(e -> addHouse());
        removeHouseButton.addActionListener(e -> removeHouse());
        listHousesButton.addActionListener(e -> listHouses());
        manageHouseButton.addActionListener(e -> manageHouse());
        exitButton.addActionListener(e -> System.exit(0));

        mainMenuPanel.add(addHouseButton, gbc);
        mainMenuPanel.add(removeHouseButton, gbc);
        mainMenuPanel.add(listHousesButton, gbc);
        mainMenuPanel.add(manageHouseButton, gbc);
        mainMenuPanel.add(exitButton, gbc);

        gbc.weighty = 1.0; // Pchnij statusLabel na dół, zajmując resztę miejsca
        gbc.insets = new Insets(40, 0, 0, 0); // Większy odstęp nad statusem
        mainMenuPanel.add(statusLabel, gbc);

        showPanel(mainMenuPanel);
    }

    // --- Metoda pomocnicza do tworzenia stylizowanych przycisków ---
    private JButton createStyledButton(String text, Font font) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Rysowanie cienia
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 80)); // Cień przy naciśnięciu
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
                } else {
                    g2.setColor(new Color(0, 0, 0, 40)); // Standardowy cień
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 20, 20);
                }

                // Rysowanie tła przycisku
                if (getModel().isRollover()) {
                    g2.setColor(ACCENT_COLOR); // Kolor przy najechaniu
                } else {
                    g2.setColor(PRIMARY_COLOR); // Domyślny kolor
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20); // Zaokrąglone rogi

                // Rysowanie tekstu
                FontMetrics metrics = g2.getFontMetrics(getFont());
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                g2.setColor(Color.WHITE); // Biały tekst
                g2.setFont(getFont());
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            @Override
            public void paintBorder(Graphics g) {
                // Nie rysujemy domyślnego obramowania, aby zaokrąglenie było widoczne
            }

            @Override
            public boolean contains(int x, int y) {
                // Pozwala na klikanie tylko w obszarze zaokrąglonego prostokąta
                Shape shape = new Rectangle2D.Float(0, 0, getWidth() - 20, getHeight() - 20);
                return shape.contains(x, y);
            }
        };

        button.setFont(font);
        button.setForeground(Color.WHITE); // Ustawienie koloru tekstu
        button.setFocusPainted(false); // Usuń obramowanie po kliknięciu
        button.setBorderPainted(false); // Nie maluj domyślnego obramowania
        button.setContentAreaFilled(false); // Nie maluj domyślnego tła
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Kursor dłoni przy najechaniu

        // Dodaj słuchaczy MouseListener do efektu najechania i naciśnięcia
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint(); // Odśwież, aby pokazać kolor najazdu
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint(); // Odśwież, aby wrócić do domyślnego koloru
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.repaint(); // Odśwież, aby pokazać cień naciśnięcia
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.repaint(); // Odśwież po zwolnieniu przycisku
            }
        });

        return button;
    }


    // --- Metody obsługi akcji (bez zmian funkcjonalnych) ---

    private void addHouse() {
        AddHouseDialog dialog = new AddHouseDialog(this);
        dialog.setVisible(true);

        House newHouse = dialog.getNewHouse();
        if (newHouse != null) {
            if (system.addHouse(newHouse)) {
                statusLabel.setText("House '" + newHouse.getName() + "' added successfully!");
            } else {
                statusLabel.setText("Error: House with this name already exists.");
                JOptionPane.showMessageDialog(this, "House with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            statusLabel.setText("Add House operation cancelled or incomplete.");
        }
    }

    private void removeHouse() {
        if (!system.hasHouses()) {
            statusLabel.setText("No houses to remove.");
            JOptionPane.showMessageDialog(this, "No houses to remove.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        java.util.List<House> houses = system.getAllHouses();
        String[] houseNames = houses.stream().map(House::getName).toArray(String[]::new);

        JComboBox<String> houseSelectionComboBox = new JComboBox<>(houseNames);
        houseSelectionComboBox.setFont(new Font("Arial", Font.PLAIN, 18));
        houseSelectionComboBox.setBackground(Color.WHITE); // Białe tło comboboxa
        houseSelectionComboBox.setForeground(TEXT_COLOR); // Kolor tekstu

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(SECONDARY_COLOR); // Tło panelu
        JLabel selectLabel = new JLabel("Select house to remove:");
        selectLabel.setFont(new Font("Arial", Font.BOLD, 20));
        selectLabel.setForeground(TEXT_COLOR);
        panel.add(selectLabel, BorderLayout.NORTH);
        panel.add(houseSelectionComboBox, BorderLayout.CENTER);


        int result = JOptionPane.showConfirmDialog(this, panel, "Remove House", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedHouseName = (String) houseSelectionComboBox.getSelectedItem();
            if (selectedHouseName != null) {
                Optional<House> houseToRemoveOpt = houses.stream()
                        .filter(h -> h.getName().equals(selectedHouseName))
                        .findFirst();

                if (houseToRemoveOpt.isPresent()) {
                    House houseToRemove = houseToRemoveOpt.get();
                    int confirmResult = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to remove '" + selectedHouseName + "'?",
                            "Confirm Removal",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirmResult == JOptionPane.YES_OPTION) {
                        if (system.removeHouse(houseToRemove)) {
                            statusLabel.setText("House '" + selectedHouseName + "' removed successfully!");
                        } else {
                            statusLabel.setText("Error: Could not remove house.");
                            JOptionPane.showMessageDialog(this, "Could not remove house.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        statusLabel.setText("Remove operation cancelled.");
                    }
                }
            }
        }
    }

    private void listHouses() {
        if (!system.hasHouses()) {
            statusLabel.setText("No houses to list.");
            JOptionPane.showMessageDialog(this, "No houses available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("Available Houses:\n\n");
        for (int i = 0; i < system.getAllHouses().size(); i++) {
            House house = system.getAllHouses().get(i);
            sb.append("Name: ").append(house.getName()).append("\n")
                    .append("  Type: ").append(house.getHouseType()).append("\n")
                    .append("  Color: ").append(house.getColor()).append("\n")
                    .append("  Location: ").append(house.getLocalisation().toString()).append("\n")
                    .append("  Rooms: ").append(house.getRoomCount()).append("\n");
            if (!house.getRooms().isEmpty()) {
                sb.append("    Rooms details:\n");
                for (var room : house.getRooms()) {
                    sb.append("      - ").append(room.getName()).append(" (Type: ").append(room.getRoomType()).append(", Area: ").append(room.getArea()).append(")\n");
                }
            }
            sb.append("\n");
        }
        statusLabel.setText("Houses listed.");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        textArea.setEditable(false);
        textArea.setBackground(SECONDARY_COLOR.brighter()); // Jaśniejsze tło dla text area
        textArea.setForeground(TEXT_COLOR);
        textArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR)); // Dodaj obramowanie

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(1000, 600));

        // Zmieniony JOptionPane na dostosowany, aby mieć lepszą kontrolę nad wyglądem
        JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.PLAIN_MESSAGE);
        JDialog dialog = optionPane.createDialog(this, "List of Houses");
        dialog.setResizable(true);
        dialog.setVisible(true);
    }

    private void manageHouse() {
        if (!system.hasHouses()) {
            statusLabel.setText("No houses available to manage.");
            JOptionPane.showMessageDialog(this, "No houses available. Please add a house first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        java.util.List<House> houses = system.getAllHouses();
        String[] houseNames = houses.stream().map(House::getName).toArray(String[]::new);

        JComboBox<String> houseSelectionComboBox = new JComboBox<>(houseNames);
        houseSelectionComboBox.setFont(new Font("Arial", Font.PLAIN, 18));
        houseSelectionComboBox.setBackground(Color.WHITE);
        houseSelectionComboBox.setForeground(TEXT_COLOR);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(SECONDARY_COLOR);
        JLabel selectLabel = new JLabel("Select house to manage:");
        selectLabel.setFont(new Font("Arial", Font.BOLD, 20));
        selectLabel.setForeground(TEXT_COLOR);
        panel.add(selectLabel, BorderLayout.NORTH);
        panel.add(houseSelectionComboBox, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Manage House", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedHouseName = (String) houseSelectionComboBox.getSelectedItem();
            if (selectedHouseName != null) {
                Optional<House> selectedHouseOpt = houses.stream()
                        .filter(h -> h.getName().equals(selectedHouseName))
                        .findFirst();

                if (selectedHouseOpt.isPresent()) {
                    House selectedHouse = selectedHouseOpt.get();
                    system.setSelectedHouse(selectedHouse);
                    statusLabel.setText("Managing house: " + selectedHouse.getName());

                    showPanel(new ManageHousePanel(system, this));
                }
            }
        }
    }
}