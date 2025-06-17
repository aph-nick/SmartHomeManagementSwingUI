package Devices;

import Enums.MovementSensorEnums;
import Enums.Status;
import Interfaces.SensorDevice;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import GUI.MovementSensorControlDialog; // Import nowego dialogu GUI

import javax.swing.*;
import java.util.List;
// Usunięto import java.util.Scanner

public class MovementSensor extends SmartDevice
        implements SensorDevice<Integer> { // Zmieniono na SensorDevice<Integer> dla klarowności
    private MovementSensorEnums movementSensorType;
    private int range;
    private int movementDetected; // 0 for no movement, 1 for movement

    // Zaktualizowany konstruktor: dodano deviceType
    public MovementSensor(String name, MovementSensorEnums movementSensorType) {
        super(name, "MovementSensor"); // Przekazujemy "MovementSensor" jako deviceType
        this.movementSensorType = movementSensorType;

        // Ustawienie domyślnego zasięgu w zależności od typu sensora
        setInitialRange(movementSensorType);
        this.movementDetected = 0; // Domyślnie brak ruchu
    }

    private void setInitialRange(MovementSensorEnums type) {
        switch (type) {
            case PIR:
                this.range = 5; // PIRy mają zazwyczaj większy zasięg detekcji niż radar czy ultradźwięki w kontekście ruchu
                break;
            case RADAR:
                this.range = 3; // Radar może być precyzyjniejszy, ale często o mniejszym zasięgu dla drobnych ruchów
                break;
            case ULTRASONIC:
                this.range = 2; // Ultradźwięki są dobre na krótsze dystanse
                break;
            default:
                this.range = 0; // Domyślna wartość w przypadku nieznanego typu (choć enum powinien to wykluczyć)
                DeviceLogger.logEvent(this, "ERROR", "Unknown sensor type encountered: " + movementSensorType);
                break;
        }
        DeviceLogger.logEvent(this, "INFO", "Initial range set to " + this.range + "m for " + movementSensorType + " sensor.");
    }

    @Override
    public Integer readValue() { // Zmieniono na Integer, bo Object jest zbyt ogólne
        return movementDetected;
    }

    @Override
    public String getUnit() {
        return "Movement (1=Yes, 0=No)";
    }

    @Override
    public void turnOn() throws AlreadyON {
        if(getStatus() == Status.OFF){
            setStatus(Status.ON);
            DeviceLogger.logEvent(this, "STATUS_CHANGE", "Turned ON"); // Używamy logEvent
        } else {
            throw new AlreadyON("Movement sensor is already ON.");
        }
    }

    @Override
    public void turnOff() throws AlreadyOFF {
        if(getStatus() == Status.ON){
            setStatus(Status.OFF);
            DeviceLogger.logEvent(this, "STATUS_CHANGE", "Turned OFF"); // Używamy logEvent
            stopSimulation(); // Zatrzymuje symulację po wyłączeniu urządzenia
            this.movementDetected = 0; // Resetuje status wykrycia ruchu
        } else {
            throw new AlreadyOFF("Movement sensor is already OFF.");
        }
    }

    @Override
    public boolean isOn() {
        return getStatus() == Status.ON;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        if (range >= 0) { // Zapewniamy, że zasięg jest nieujemny
            this.range = range;
            DeviceLogger.logEvent(this, "SETTING_CHANGE", "Range set to " + range + "m.");
        } else {
            DeviceLogger.logEvent(this, "ERROR", "Attempted to set negative range: " + range);
        }
    }

    public MovementSensorEnums getMovementSensorType() {
        return movementSensorType;
    }

    public void setMovementSensorType(MovementSensorEnums movementSensorType) {
        if (this.movementSensorType != movementSensorType) {
            this.movementSensorType = movementSensorType;
            // Możesz zaktualizować zasięg na podstawie nowego typu, jeśli chcesz
            setInitialRange(movementSensorType); // Aktualizuje zasięg do domyślnego dla nowego typu
            DeviceLogger.logEvent(this, "SETTING_CHANGE", "Sensor type set to " + movementSensorType + " (range updated).");
        }
    }

    // Metoda do symulowania wykrycia ruchu, którą GUI może wywołać
    public void forceMovementDetection(boolean detected) {
        this.movementDetected = detected ? 1 : 0;
        DeviceLogger.logEvent(this, "SIMULATION_CONTROL", "Movement detection " + (detected ? "FORCED ON" : "FORCED OFF"));
    }


    @Override
    public void simulate() throws DeviceDisabled, SimulationInterrupted {
        if (getStatus() == Status.OFF) {
            throw new DeviceDisabled(getName() + " is disabled. Turn it ON to simulate.");
        }

        if (isSimulating()) {
            return; // Komunikat dla GUI będzie wyświetlony przez wywołującą metodę
        }

        setSimulating(true);
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation started.");

        simulationThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && getStatus() == Status.ON) {
                    // Symuluj wykrycie ruchu z prawdopodobieństwem 50%
                    movementDetected = Math.random() < 0.5 ? 1 : 0;
                    DeviceLogger.logEvent(this, "MOVEMENT_DETECTED", "Movement detected: " + (movementDetected == 1 ? "Yes" : "No"));
                    Thread.sleep(3000); // Odczyt co 3 sekundy
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
        return "MovementSensor{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", sensorType=" + movementSensorType +
                ", range=" + range + "m" +
                ", movementDetected=" + (movementDetected == 1 ? "Yes" : "No") +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                '}';
    }

    // --- Usunięto metodę controlMenu(Scanner scanner) ---

    // --- Implementacja NOWEJ metody GUI ---
    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        MovementSensorControlDialog dialog = new MovementSensorControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }
}