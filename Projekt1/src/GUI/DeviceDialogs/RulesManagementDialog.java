package GUI;

import Devices.Rule; // Pamiętaj o nowym pakiecie
import Devices.RuleManager; // Pamiętaj o nowym pakiecie
import Devices.SmartDevice;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RulesManagementDialog extends JDialog {
    private DefaultListModel<Rule<? extends SmartDevice>> rulesListModel;
    private JList<Rule<? extends SmartDevice>> rulesJList;
    private JTextArea ruleDetailsTextArea;
    private JButton toggleRuleButton;

    public RulesManagementDialog(JDialog parent) { // Teraz bez konkretnego urządzenia
        super(parent, "Manage All Rules", true); // Tytuł odzwierciedla zarządzanie wszystkimi regułami
        setLayout(new BorderLayout(10, 10));
        setSize(850, 850);
        setLocationRelativeTo(parent);

        initComponents();
        populateRulesList();
        add(createMainPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        // Timer do cyklicznego odświeżania listy i statusów (np. gdy reguła zostanie uruchomiona)
        Timer refreshTimer = new Timer(2000, e -> populateRulesList());
        refreshTimer.start();
    }

    private void initComponents() {
        rulesListModel = new DefaultListModel<>();
        rulesJList = new JList<>(rulesListModel);
        rulesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rulesJList.setFont(new Font("Arial", Font.PLAIN, 14));
        rulesJList.setCellRenderer(new RuleListCellRenderer()); // Użyj customowego renderera
        rulesJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedRuleDetails();
                updateToggleButtonState();
            }
        });

        ruleDetailsTextArea = new JTextArea();
        ruleDetailsTextArea.setEditable(false);
        ruleDetailsTextArea.setLineWrap(true);
        ruleDetailsTextArea.setWrapStyleWord(true);
        ruleDetailsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ruleDetailsTextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        toggleRuleButton = new JButton("Toggle Rule (Enable/Disable)");
        toggleRuleButton.setFont(new Font("Arial", Font.BOLD, 16));
        toggleRuleButton.addActionListener(e -> {
            Rule<? extends SmartDevice> selectedRule = rulesJList.getSelectedValue();
            if (selectedRule != null) {
                selectedRule.setEnabled(!selectedRule.isEnabled());
                // Odśwież listę, aby zaktualizować status wizualny
                rulesJList.repaint();
                updateToggleButtonState();
            }
        });
        toggleRuleButton.setEnabled(false); // Domyślnie wyłączony
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JScrollPane listScrollPane = new JScrollPane(rulesJList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("All Registered Rules"));

        JScrollPane detailsScrollPane = new JScrollPane(ruleDetailsTextArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Rule Details"));

        mainPanel.add(listScrollPane);
        mainPanel.add(detailsScrollPane);

        return mainPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(toggleRuleButton);
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        return buttonPanel;
    }

    private void populateRulesList() {
        rulesListModel.clear();
        List<Rule<? extends SmartDevice>> allSystemRules = RuleManager.getInstance().getAllRules();
        if (allSystemRules.isEmpty()) {
            rulesListModel.addElement(new Rule(null, d -> false, d -> {}, "No rules defined in the system.")); // Dummy rule
        } else {
            for (Rule<? extends SmartDevice> rule : allSystemRules) {
                rulesListModel.addElement(rule);
            }
        }
        // Utrzymaj selekcję, jeśli to możliwe
        if (!rulesListModel.isEmpty() && rulesJList.getSelectedIndex() == -1) {
            rulesJList.setSelectedIndex(0); // Zaznacz pierwszy element domyślnie
        }
        displaySelectedRuleDetails(); // Odśwież szczegóły
        updateToggleButtonState(); // Zaktualizuj stan przycisku
    }

    private void displaySelectedRuleDetails() {
        Rule<? extends SmartDevice> selectedRule = rulesJList.getSelectedValue();
        if (selectedRule != null && selectedRule.getDevice() != null) { // Sprawdź, czy nie jest to dummy rule
            StringBuilder details = new StringBuilder();
            details.append("Description: ").append(selectedRule.getDescription()).append("\n\n");
            details.append("Status: ").append(selectedRule.isEnabled() ? "Enabled" : "Disabled").append("\n\n");
            details.append("Applies to Device: ").append(selectedRule.getDevice().getName()).append("\n");
            details.append("Device Type: ").append(selectedRule.getDevice().getDeviceType()).append("\n\n");
            details.append("Condition (Predicate):\n");
            details.append("  (Logic defined in code, e.g., 'battery <= 20%')\n\n");
            details.append("Action (Consumer):\n");
            details.append("  (Logic defined in code, e.g., 'change status to LOW_BATTERY')\n\n");

            ruleDetailsTextArea.setText(details.toString());
        } else {
            ruleDetailsTextArea.setText("Select a rule to see its details.");
        }
    }

    private void updateToggleButtonState() {
        Rule<? extends SmartDevice> selectedRule = rulesJList.getSelectedValue();
        toggleRuleButton.setEnabled(selectedRule != null && selectedRule.getDevice() != null); // Wyłącz dla dummy rule
        if (selectedRule != null && selectedRule.getDevice() != null) {
            toggleRuleButton.setText(selectedRule.isEnabled() ? "Disable Rule" : "Enable Rule");
        } else {
            toggleRuleButton.setText("Toggle Rule (Enable/Disable)");
        }
    }

    // Custom Cell Renderer, aby wyświetlać status reguły w liście
    private static class RuleListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Rule) {
                Rule<?> rule = (Rule<?>) value;
                label.setText((index + 1) + ". " + rule.getDescription() + " " + (rule.isEnabled() ? "[Enabled]" : "[Disabled]"));
                if (rule.isEnabled()) {
                    label.setForeground(Color.BLACK);
                } else {
                    label.setForeground(Color.GRAY); // Szary, jeśli reguła jest wyłączona
                }
            }
            return label;
        }
    }
}