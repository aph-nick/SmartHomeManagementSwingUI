package Devices;

import Enums.Status;
import Interfaces.Switchable;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import GUI.OutletControlDialog; // Import nowego dialogu GUI

import javax.swing.*;
import java.util.List;

public class Outlet extends SmartDevice
        implements Switchable {
    private boolean isBeingUsed;
    private SmartDevice connectedDevice;

    // Zaktualizowany konstruktor: dodano deviceType
    public Outlet(String name) {
        super(name, "Outlet"); // Przekazujemy "Outlet" jako deviceType
        this.isBeingUsed = false;
        // Domyślnie Outlet jest włączony, ale nieużywany, dopóki nie podłączymy urządzenia.
        // Jeśli ma być domyślnie wyłączony, zmień Status.ON na Status.OFF.
        setStatus(Status.ON);
    }

    public boolean isBeingUsed() {
        return isBeingUsed;
    }

    // Usunięto rzucanie AlreadyOFF, ponieważ turnOff() już to obsługuje
    public void setBeingUsed(boolean used) {
        if (this.isBeingUsed != used) { // Zmień stan tylko jeśli jest różny
            this.isBeingUsed = used;
            DeviceLogger.logEvent(this, "USAGE_STATUS", "Outlet usage status changed to: " + (used ? "In Use" : "Not In Use"));

            // Jeśli przestaje być używane i było włączone, wyłącz je
            if (!used && getStatus() == Status.ON) {
                try {
                    turnOff(); // Automatyczne wyłączanie po odłączeniu urządzenia
                } catch (AlreadyOFF e) {
                    DeviceLogger.logEvent(this, "ERROR", "Failed to turn off outlet after device removal: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void turnOn() throws AlreadyON{
        // Gniazdko może zostać włączone tylko wtedy, gdy jest używane (tj. podłączone jest urządzenie)
        if(getStatus() == Status.OFF) { // Sprawdzamy tylko status gniazdka
            if (isBeingUsed) {
                setStatus(Status.ON);
                DeviceLogger.logEvent(this, "STATUS_CHANGE", "Turned ON"); // Używamy logEvent
            } else {
                throw new AlreadyON("Cannot turn ON: No device connected to the outlet.");
            }
        } else {
            throw new AlreadyON("The outlet is already ON.");
        }
    }

    @Override
    public void turnOff() throws AlreadyOFF {
        if(getStatus() == Status.ON) {
            setStatus(Status.OFF);
            DeviceLogger.logEvent(this, "STATUS_CHANGE", "Turned OFF"); // Używamy logEvent
            stopSimulation(); // Zatrzymuje symulację po wyłączeniu gniazdka
        } else {
            throw new AlreadyOFF("The outlet is already OFF.");
        }
    }

    @Override
    public boolean isOn() {
        return getStatus() == Status.ON;
    }

    public void connectDevice(SmartDevice connectedDevice) {
        if (this.connectedDevice != null) {
            DeviceLogger.logEvent(this, "CONNECTION_WARNING", "Warning: A device '" + this.connectedDevice.getName() + "' is already connected. Overwriting with '" + connectedDevice.getName() + "'.");
            removeDevice(); // Usuń poprzednie urządzenie przed podłączeniem nowego
        }

        this.connectedDevice = connectedDevice;
        setBeingUsed(true); // Ustaw, że gniazdko jest używane
        DeviceLogger.logEvent(this, "CONNECTION", "Device '" + connectedDevice.getName() + "' connected to outlet '" + getName() + "'.");
        // Gniazdko automatycznie włącza się, jeśli urządzenie jest podłączone (lub próbuje się włączyć)
        try {
            turnOn();
        } catch (AlreadyON e) {
            DeviceLogger.logEvent(this, "CONNECTION_ERROR", "Failed to turn on outlet after connecting device: " + e.getMessage());
        }
    }

    public void removeDevice() {
        if (connectedDevice != null) {
            DeviceLogger.logEvent(this, "CONNECTION", "Device '" + connectedDevice.getName() + "' removed from outlet '" + getName() + "'.");
        } else {
            DeviceLogger.logEvent(this, "CONNECTION_WARNING", "No device was connected to outlet '" + getName() + "' to remove.");
        }

        this.connectedDevice = null;
        setBeingUsed(false); // Ustaw, że gniazdko nie jest już używane
    }

    public SmartDevice getConnectedDevice() {
        return connectedDevice;
    }

    @Override
    public void simulate() throws DeviceDisabled, SimulationInterrupted {
        if (getStatus() == Status.OFF) {
            throw new DeviceDisabled(getName() + " is disabled. Turn it ON to simulate.");
        }

        if (!isBeingUsed) {
            throw new DeviceDisabled(getName() + " is not being used. Connect a device to simulate.");
        }

        if (isSimulating()) {
            return; // Komunikat dla GUI będzie wyświetlony przez wywołującą metodę
        }

        setSimulating(true);
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation started.");

        simulationThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && getStatus() == Status.ON && isBeingUsed) {
                    double voltage = 230 - (Math.random() * 5); // Symulacja niewielkich wahań napięcia
                    DeviceLogger.logEvent(this, "POWER_OUTPUT", String.format("Outlet is providing: %.2fV of power to %s", voltage,
                            connectedDevice != null ? connectedDevice.getName() : "an unknown device"));
                    Thread.sleep(5000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Przywróć status przerwania
                DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation interrupted.");
            } finally {
                setSimulating(false);
                DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation stopped.");
            }
        });

        simulationThread.start();
    }

    @Override
    public void stopSimulation() {
        if (simulationThread != null && simulationThread.isAlive()) {
            simulationThread.interrupt();
        }
        setSimulating(false);
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation stopped.");
    }

    @Override
    public String toString() {
        return "Outlet{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", isBeingUsed=" + (isBeingUsed ? "Yes" : "No") +
                ", connectedDevice=" + (connectedDevice != null ? connectedDevice.getName() : "None") +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                '}';
    }

    // --- Usunięto metodę controlMenu(java.util.Scanner scanner) ---

    // --- Implementacja NOWEJ metody GUI ---
    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        OutletControlDialog dialog = new OutletControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }
}