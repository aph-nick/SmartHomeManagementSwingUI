package GUI;

import Devices.Outlet;
import Devices.SmartDevice; // Potrzebne do ComboBox
import Enums.Status;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;

import javax.swing.*;
import java.awt.*;
import java.util.List; // Jeśli będziesz ładować urządzenia z listy

public class OutletControlDialog extends JDialog {
    private Outlet outlet;

    private JLabel currentStatusLabel;
    private JLabel simulationStatusLabel;
    private JLabel isBeingUsedLabel;
    private JLabel connectedDeviceLabel;

    private JComboBox<SmartDevice> availableDevicesComboBox; // Do podłączania urządzeń
    private JButton connectDeviceButton;
    private JButton disconnectDeviceButton;

    // Zakładamy, że lista wszystkich SmartDevices jest dostępna gdzieś globalnie
    // lub zostanie przekazana do konstruktora (np. z SmartHomeSystem).
    // Na razie, symulujemy pustą listę.
    private List<SmartDevice> allSmartDevices;

    public OutletControlDialog(JFrame parent, Outlet outlet) {
        super(parent, "Control Outlet: " + outlet.getName(), true);
        this.outlet = outlet;
        // W prawdziwej aplikacji, poniższa linia powinna pobierać urządzenia
        // z głównego systemu, np.: SmartHomeSystem.getInstance().getAllDevices();
        this.allSmartDevices = new java.util.ArrayList<>(); // Placeholder

        setLayout(new GridBagLayout());
        setSize(850, 850);
        setLocationRelativeTo(parent);

        initComponents();
        createLayout();
        updateDisplay(); // Pierwsze odświeżenie

        // Timer do cyklicznego odświeżania statusu, np. co sekundę
        Timer refreshTimer = new Timer(1000, e -> updateDisplay());
        refreshTimer.start();
    }

    // Przeciążony konstruktor, jeśli chcesz przekazać listę urządzeń
    public OutletControlDialog(JFrame parent, Outlet outlet, List<SmartDevice> allSmartDevices) {
        this(parent, outlet); // Wywołaj główny konstruktor
        this.allSmartDevices = allSmartDevices;
        populateAvailableDevicesComboBox(); // Wypełnij ComboBox po ustawieniu listy
    }


    private void initComponents() {
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font controlFont = new Font("Arial", Font.PLAIN, 16);

        currentStatusLabel = new JLabel();
        currentStatusLabel.setFont(labelFont);

        simulationStatusLabel = new JLabel();
        simulationStatusLabel.setFont(labelFont);

        isBeingUsedLabel = new JLabel();
        isBeingUsedLabel.setFont(labelFont);

        connectedDeviceLabel = new JLabel();
        connectedDeviceLabel.setFont(labelFont);

        // ComboBox dla dostępnych urządzeń
        availableDevicesComboBox = new JComboBox<>();
        availableDevicesComboBox.setFont(controlFont);
        populateAvailableDevicesComboBox(); // Wypełnij ComboBox

        // Przyciski do podłączania/odłączania
        connectDeviceButton = new JButton("Connect Device");
        connectDeviceButton.setFont(controlFont);
        connectDeviceButton.addActionListener(e -> {
            SmartDevice selectedDevice = (SmartDevice) availableDevicesComboBox.getSelectedItem();
            if (selectedDevice != null) {
                outlet.connectDevice(selectedDevice);
                updateDisplay();
                JOptionPane.showMessageDialog(this, "Device " + selectedDevice.getName() + " connected.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a device to connect.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        disconnectDeviceButton = new JButton("Disconnect Device");
        disconnectDeviceButton.setFont(controlFont);
        disconnectDeviceButton.addActionListener(e -> {
            if (outlet.getConnectedDevice() != null) {
                outlet.removeDevice();
                updateDisplay();
                JOptionPane.showMessageDialog(this, "Device disconnected.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No device is currently connected.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
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
        gbc.gridy = row++;
        add(isBeingUsedLabel, gbc);
        gbc.gridy = row++;
        add(connectedDeviceLabel, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // Device Connection Controls
        gbc.gridx = 0; gbc.gridy = row;
        add(new JLabel("Available Devices:"), gbc);
        gbc.gridx = 1;
        add(availableDevicesComboBox, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        add(connectDeviceButton, gbc);
        gbc.gridx = 1;
        add(disconnectDeviceButton, gbc);
        row++;

        // Close Button
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.addActionListener(e -> dispose());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(closeButton, gbc);
    }

    private void updateDisplay() {
        currentStatusLabel.setText("Power Status: " + outlet.getStatus());
        simulationStatusLabel.setText("Simulation: " + (outlet.isSimulating() ? "Running" : "Stopped"));
        isBeingUsedLabel.setText("Being Used: " + (outlet.isBeingUsed() ? "Yes" : "No"));
        connectedDeviceLabel.setText("Connected Device: " + (outlet.getConnectedDevice() != null ? outlet.getConnectedDevice().getName() : "None"));

        // Aktualizuj ComboBox, jeśli lista urządzeń ulegnie zmianie
        populateAvailableDevicesComboBox();

        // Włączanie/wyłączanie kontrolek w zależności od statusu gniazdka
        boolean isOutletOn = outlet.isOn();
        boolean isOutletBeingUsed = outlet.isBeingUsed();

        connectDeviceButton.setEnabled(outlet.getConnectedDevice() == null && isOutletOn); // Włącz, jeśli nic nie jest podłączone i gniazdko jest ON
        disconnectDeviceButton.setEnabled(outlet.getConnectedDevice() != null); // Włącz, jeśli coś jest podłączone

        // ComboBox jest dostępny tylko wtedy, gdy można podłączyć urządzenie
        availableDevicesComboBox.setEnabled(connectDeviceButton.isEnabled());
    }

    private void populateAvailableDevicesComboBox() {
        // Ta metoda powinna być zintegrowana z głównym systemem Smart Home,
        // aby uzyskać listę wszystkich dostępnych urządzeń, które mogą być podłączone.
        // Na potrzeby tej demonstracji, zakładam, że `allSmartDevices` jest już załadowane.

        // Oczyść ComboBox
        availableDevicesComboBox.removeAllItems();

        // Dodaj tylko te urządzenia, które nie są jeszcze podłączone do tego gniazdka
        // i nie są Outletami (Outlet nie może być podłączony do innego Outleta).
        // A także te, które implementują Pluggable (jeśli masz taki interfejs,
        // w przeciwnym razie każde SmartDevice)
        if (allSmartDevices != null) {
            for (SmartDevice device : allSmartDevices) {
                // Upewnij się, że urządzenie nie jest już podłączone i nie jest samym Outletem
                if (device != outlet.getConnectedDevice() && !(device instanceof Outlet)) {
                    availableDevicesComboBox.addItem(device);
                }
            }
        }

        // Jeśli aktualnie podłączone urządzenie jest null, ustaw pusty wybór lub nic
        if (outlet.getConnectedDevice() == null && availableDevicesComboBox.getItemCount() > 0) {
            availableDevicesComboBox.setSelectedIndex(-1); // Nic nie wybrane
        }
    }
}