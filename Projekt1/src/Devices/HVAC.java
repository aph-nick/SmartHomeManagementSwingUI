package Devices;

import Enums.HVACEnums;
import Enums.Status;
import Interfaces.ObservableDevice;
import Interfaces.Observer;
import Interfaces.Pluggable;
import Interfaces.Switchable;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import GUI.HVACControlDialog; // Import nowego dialogu GUI

import javax.swing.*;
import java.util.List;

public class HVAC extends SmartDevice
        implements Switchable, Pluggable, Observer<Double> {
    private HVACEnums hvacStatus;
    private final TemperatureSensor temperatureSensor;
    private Outlet outlet; // Musi być zainicjalizowany w konstruktorze lub setOutlet

    // Konstruktor: Dodano deviceType i usunięto Scanner
    public HVAC(String name, TemperatureSensor sensor, Outlet outlet) {
        super(name, "HVAC"); // Przekazujemy "HVAC" jako deviceType
        this.temperatureSensor = sensor;
        if (this.temperatureSensor != null) {
            this.temperatureSensor.addObserver(this); // HVAC obserwuje sensor
        }
        this.outlet = outlet; // Inicjalizacja outletu
        this.hvacStatus = HVACEnums.IDLE; // Domyślny status po włączeniu
    }

    @Override
    public void simulate() throws DeviceDisabled, SimulationInterrupted {
        if (temperatureSensor == null) {
            throw new DeviceDisabled("Cannot simulate HVAC: Temperature Sensor is missing.");
        }

        if (getStatus() == Status.OFF) {
            throw new DeviceDisabled(getName() + " is disabled. Turn it ON to simulate.");
        }

        if (isSimulating()) {
            // Komunikat dla GUI będzie wyświetlony przez wywołującą metodę
            return;
        }

        setSimulating(true);
        simulationThread = new Thread(() -> {
            DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation started.");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    update(); // Aktualizuje status HVAC na podstawie temperatury

                    // Tutaj można dodać logikę symulacji, np. losowe zmiany temperatury
                    // lub symulowanie wpływu HVAC na temperaturę.
                    // Na razie, wystarczy, że update() reaguje na zmiany temperatury.

                    Thread.sleep(5000); // Częstotliwość aktualizacji symulacji
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Przywróć status przerwania
                DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation interrupted.");
                setSimulating(false);
            } finally {
                setSimulating(false); // Zapewnia, że stan symulacji jest poprawny po zakończeniu
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

    public void startVentilation(){
        if(getStatus() == Status.ON) {
            hvacStatus = HVACEnums.VENTILATING;
            DeviceLogger.logStatusChange(this, "Ventilation ON");
        } else {
            DeviceLogger.logEvent(this, "OPERATION_FAILED", "Cannot start ventilation: HVAC is OFF.");
        }
    }

    public void stopVentilation(){
        if(getStatus() == Status.ON) {
            hvacStatus = HVACEnums.IDLE;
            DeviceLogger.logStatusChange(this, "Ventilation OFF (idle)");
        }
    }

    @Override
    public void turnOn() throws AlreadyON {
        if(outlet == null) {
            throw new AlreadyON("Cannot turn on HVAC: Outlet is missing."); // Używamy AlreadyON dla braku outletu
        }
        if (temperatureSensor == null) {
            throw new AlreadyON("Cannot turn on HVAC: No Temperature Sensor connected."); // Używamy AlreadyON dla braku sensora
        }

        if (getStatus() == Status.ON) {
            throw new AlreadyON("HVAC already ON.");
        }

        setStatus(Status.ON);
        hvacStatus = HVACEnums.IDLE;
        DeviceLogger.logStatusChange(this, "Turned ON");
    }

    @Override
    public void turnOff() throws AlreadyOFF {
        if (getStatus() == Status.OFF) {
            throw new AlreadyOFF("HVAC already OFF.");
        }
        setStatus(Status.OFF);
        hvacStatus = HVACEnums.IDLE; // Reset statusu po wyłączeniu
        DeviceLogger.logStatusChange(this, "Turned OFF");
    }

    @Override
    public boolean isOn() {
        return getStatus() == Status.ON;
    }

    public HVACEnums getHvacStatus() {
        return hvacStatus;
    }

    // Metoda update() bez parametrow, uzywana przez wewnetrzne mechanizmy
    @Override
    public void update() {
        // Ta metoda może być użyta do wewnętrznej logiki, jeśli nie ma aktualizacji z zewnątrz
    }

    // Metoda update(ObservableDevice, Object) jest wywoływana przez TemperatureSensor
    @Override
    public void update(ObservableDevice device, Object object) {
        if (!isSimulating() || getStatus() != Status.ON) return; // Tylko jeśli symulacja działa i HVAC jest ON
        if (!(object instanceof Double)) return; // Upewnij się, że to temperatura

        double currentTemp = (Double) object;
        HVACEnums oldHvacStatus = hvacStatus;

        if (currentTemp < 18) {
            if (hvacStatus != HVACEnums.HEATING) {
                hvacStatus = HVACEnums.HEATING;
                DeviceLogger.logStatusChange(this, "Heating ON (temp: " + String.format("%.2f", currentTemp) + "°C)");
            }
        } else if (currentTemp > 22) {
            if (hvacStatus != HVACEnums.COOLING) {
                hvacStatus = HVACEnums.COOLING;
                DeviceLogger.logStatusChange(this, "Cooling ON (temp: " + String.format("%.2f", currentTemp) + "°C)");
            }
        } else {
            if (hvacStatus != HVACEnums.IDLE) {
                hvacStatus = HVACEnums.IDLE;
                DeviceLogger.logStatusChange(this, "Idling (temp: " + String.format("%.2f", currentTemp) + "°C) - temp in range");
            }
        }
        // Możesz dodać logowanie tylko, jeśli status HVAC się zmienił
        // if (oldHvacStatus != hvacStatus) {
        //     DeviceLogger.logStatusChange(this, "HVAC status changed to " + hvacStatus + " (temp: " + currentTemp + "°C)");
        // }
    }

    @Override
    public void connectToOutlet(Outlet outlet) {
        this.outlet = outlet;
        DeviceLogger.logEvent(this, "CONNECTION", "Connected to outlet: " + outlet.getName());
    }

    @Override
    public void disconnectFromOutlet(Outlet outlet) {
        if (this.outlet == outlet) {
            this.outlet = null;
            DeviceLogger.logEvent(this, "CONNECTION", "Disconnected from outlet: " + outlet.getName());
        }
    }

    @Override
    public Outlet getOutlet() {
        return this.outlet;
    }

    @Override
    public String toString() {
        return "HVAC{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", hvacStatus=" + hvacStatus +
                ", connected=" + (outlet != null ? outlet.getName() : "No Outlet") +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                ", sensor=" + (temperatureSensor != null ? temperatureSensor.getName() : "No Sensor") +
                '}';
    }

    // --- NOWA METODA DLA GUI ---
    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        HVACControlDialog dialog = new HVACControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }
}