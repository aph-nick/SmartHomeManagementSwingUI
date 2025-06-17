package Devices;

import Enums.Status;
import Enums.TemperatureSensorEnums;
import Interfaces.ObservableDevice;
import Interfaces.Observer;
import Interfaces.SensorDevice;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import GUI.TemperatureSensorControlDialog; // Import nowego dialogu GUI

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
// Usunięto import java.util.Scanner

public class TemperatureSensor extends SmartDevice
        implements SensorDevice<Double>, ObservableDevice<Double> {
    private TemperatureSensorEnums sensorStatus;

    private double temperature;
    private double battery;

    // Reguły pozostają, ale ich wykonanie jest teraz monitorowane przez logger
    private final Rule<TemperatureSensor> lowBatteryRule;
    private final Rule<TemperatureSensor> tamperRule;

    private final List<Observer<Double>> observers = new ArrayList<>();

    // Zaktualizowany konstruktor: dodano deviceType
    public TemperatureSensor(String name) {
        super(name, "TemperatureSensor"); // Przekazujemy "TemperatureSensor" jako deviceType
        this.temperature = (Math.random() * 81) - 40; // Początkowa losowa temperatura
        this.battery = 100;
        this.sensorStatus = TemperatureSensorEnums.ACTIVE;

        this.lowBatteryRule = new Rule<>(
                this,
                s -> s.getBattery() <= 20,
                s -> {
                    s.changeSensorStatus(TemperatureSensorEnums.LOW_BATTERY);
                    DeviceLogger.logRuleTriggered(s, "Battery level dropped below 20%. Status changed to LOW_BATTERY.");
                },
                "Low battery status rule"
        );

        this.tamperRule = new Rule<>(
                this,
                // Reguła sabotażu wymaga, aby urządzenie było naładowane i odczytywało ekstremalnie niską temperaturę
                s -> s.getBattery() > 90 && s.readValue() < -30,
                s -> {
                    s.changeSensorStatus(TemperatureSensorEnums.TAMPERED);
                    DeviceLogger.logRuleTriggered(s, "Potential tampering detected (temp < -30C and high battery). Status changed to TAMPERED.");
                },
                "Tamper detection rule"
        );
    }

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) { // Dodany setter do kontroli z GUI/testów
        if (battery >= 0 && battery <= 100) {
            this.battery = battery;
            // Logger nie tutaj, bo settery są często używane wewnętrznie
        }
    }

    @Override
    public Double readValue() {
        return temperature;
    }

    public void setTemperature(double temperature) { // Dodany setter do kontroli z GUI/testów
        this.temperature = temperature;
        // Logger nie tutaj
    }

    @Override
    public String getUnit() {
        return "Celsius"; // Zmieniono na prostszą nazwę jednostki
    }

    public TemperatureSensorEnums getSensorStatus() {
        return sensorStatus;
    }

    public void changeSensorStatus(TemperatureSensorEnums newStatus) {
        if (this.sensorStatus != newStatus) {
            this.sensorStatus = newStatus;
            DeviceLogger.logStatusChange(this, "Sensor status changed to " + newStatus);
        }
    }

    @Override
    public void turnOn() throws AlreadyON{
        if(getStatus() == Status.OFF){
            setStatus(Status.ON);
            DeviceLogger.logStatusChange(this, "Turned ON");
        } else {
            throw new AlreadyON("Temperature sensor already on");
        }
    }

    @Override
    public void turnOff() throws AlreadyOFF{
        if(getStatus() == Status.ON){
            setStatus(Status.OFF);
            DeviceLogger.logStatusChange(this, "Turned OFF");
            stopSimulation(); // Zatrzymuje symulację po wyłączeniu urządzenia
        } else {
            throw new AlreadyOFF("Temperature sensor already off");
        }
    }

    @Override
    public boolean isOn() {
        return getStatus() == Status.ON;
    }

    @Override
    public void addObserver(Observer<Double> observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            DeviceLogger.logEvent(this, "OBSERVER_ADDED", "Observer added: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(Observer<Double> observer) {
        if (observers.remove(observer)) {
            DeviceLogger.logEvent(this, "OBSERVER_REMOVED", "Observer removed: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeAllObservers() {
        observers.clear();
        DeviceLogger.logEvent(this, "OBSERVER_CLEARED", "All observers removed.");
    }

    @Override
    public int countObservers() {
        return observers.size();
    }

    @Override
    public void notifyObservers() {
        // notifyObservers(this.temperature); // Przeciążona metoda, lepiej wywołać konkretną
        notifyObservers(temperature);
    }

    @Override
    public void notifyObservers(Object object) {
        if (!(object instanceof Double)) {
            DeviceLogger.logEvent(this, "OBSERVER_ERROR", "Attempted to notify observers with non-Double object: " + object.getClass().getSimpleName());
            return;
        }
        for (Observer<Double> observer : observers) {
            observer.update(this, (Double) object);
        }
    }

    @Override
    public void simulate() throws DeviceDisabled, SimulationInterrupted {
        if (getStatus() == Status.OFF) {
            throw new DeviceDisabled("Cannot simulate " + getName() + ": device is disabled. Turn it ON first.");
        }

        if (isSimulating()) {
            return; // Komunikat będzie obsłużony przez GUI
        }

        setSimulating(true);
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation started.");

        simulationThread = new Thread(() -> {
            try {
                // Upewnij się, że sensor jest aktywny przy starcie symulacji, jeśli był w innym stanie
                if (sensorStatus != TemperatureSensorEnums.ACTIVE) {
                    changeSensorStatus(TemperatureSensorEnums.ACTIVE);
                }

                while (!Thread.currentThread().isInterrupted() && getStatus() == Status.ON) {
                    // Symulacja losowej temperatury w realistycznym zakresie
                    // Możesz tu użyć bardziej złożonego modelu, np. zależącego od pory dnia
                    this.temperature = (Math.random() * 30) - 5; // Np. od -5 do 25 stopni C
                    notifyObservers(temperature);
                    DeviceLogger.logEvent(this, "TEMPERATURE_READ", "Current Temperature: " + String.format("%.2f °C", temperature));

                    this.battery -= 0.5; // Zmniejszanie baterii
                    if (this.battery < 0) this.battery = 0; // Upewnij się, że nie spada poniżej 0

                    lowBatteryRule.execute(); // Wykonaj regułę niskiej baterii
                    tamperRule.execute();     // Wykonaj regułę sabotażu

                    if (battery <= 0) {
                        autoCharge();
                        DeviceLogger.logEvent(this, "BATTERY_STATUS", "Battery empty. Starting recharge.");
                        Thread.sleep(10000); // Symulacja czasu ładowania
                        DeviceLogger.logEvent(this, "BATTERY_STATUS", "Recharge complete.");
                    }

                    Thread.sleep(5000); // Odczyt co 5 sekund
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
        return "TemperatureSensor{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", temperature=" + String.format("%.2f °C", temperature) +
                ", battery=" + String.format("%.0f%%", battery) +
                ", sensorStatus=" + sensorStatus +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                '}';
    }

    public void autoCharge(){
        if(battery <= 0){ // Ładowanie tylko, gdy bateria jest pusta lub bliska 0
            this.battery = 100;
            DeviceLogger.logEvent(this, "BATTERY_STATUS", "Sensor battery fully recharged.");
            // Po naładowaniu, jeśli status był LOW_BATTERY, przywróć na ACTIVE
            if (sensorStatus == TemperatureSensorEnums.LOW_BATTERY) {
                changeSensorStatus(TemperatureSensorEnums.ACTIVE);
            }
        }
    }

    // --- Usunięto metodę controlMenu(Scanner scanner) ---

    // --- Implementacja NOWEJ metody GUI ---
    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        TemperatureSensorControlDialog dialog = new TemperatureSensorControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }
}