package Devices;

import Enums.Status;
import Interfaces.ObservableDevice;
import Interfaces.Observer;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import GUI.SmartClockControlDialog; // Import the new GUI dialog

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter; // For consistent time formatting in logs
import java.util.ArrayList;
import java.util.List;

public class SmartClock extends SmartDevice
        implements ObservableDevice<LocalTime> {

    private final List<Observer<LocalTime>> observers = new ArrayList<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public SmartClock(String name) {
        super(name, "SmartClock"); // Pass "SmartClock" as deviceType
        // Initial state is OFF by default from SmartDevice constructor, which is appropriate.
        DeviceLogger.logEvent(this, "CREATION", "SmartClock device created.");
    }

    public LocalTime getTime() {
        return LocalTime.now();
    }

    public LocalDate getDate() {
        return LocalDate.now();
    }

    @Override
    public void addObserver(Observer<LocalTime> observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            DeviceLogger.logEvent(this, "OBSERVER_MANAGEMENT", "Observer added: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(Observer<LocalTime> observer) {
        if (observers.remove(observer)) {
            DeviceLogger.logEvent(this, "OBSERVER_MANAGEMENT", "Observer removed: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeAllObservers() {
        if (!observers.isEmpty()) {
            observers.clear();
            DeviceLogger.logEvent(this, "OBSERVER_MANAGEMENT", "All observers removed.");
        }
    }

    @Override
    public int countObservers() {
        return observers.size();
    }

    @Override
    public void notifyObservers() {
        notifyObservers(getTime());
    }

    @Override
    public void notifyObservers(Object time) {
        if (!(time instanceof LocalTime)) {
            DeviceLogger.logEvent(this, "ERROR", "Attempted to notify observers with non-LocalTime object.");
            return;
        }
        for (Observer<LocalTime> observer : observers) {
            observer.update(this, (LocalTime) time);
        }
    }

    @Override
    public void turnOn() throws AlreadyON {
        if (getStatus() == Status.ON) {
            throw new AlreadyON("SmartClock is already ON.");
        }
        setStatus(Status.ON);
        DeviceLogger.logEvent(this, "STATUS_CHANGE", "Turned ON.");
    }

    @Override
    public void turnOff() throws AlreadyOFF {
        if (getStatus() == Status.OFF) {
            throw new AlreadyOFF("SmartClock is already OFF.");
        }
        setStatus(Status.OFF);
        stopSimulation(); // Stop simulation when turning off
        DeviceLogger.logEvent(this, "STATUS_CHANGE", "Turned OFF.");
    }

    @Override
    public boolean isOn() {
        return getStatus() == Status.ON;
    }

    @Override
    public void simulate() throws DeviceDisabled, SimulationInterrupted {
        if (getStatus() == Status.OFF) {
            throw new DeviceDisabled(getName() + " is disabled. Turn it ON to simulate.");
        }

        if (isSimulating()) {
            // No System.out.println here, GUI will handle the message
            return;
        }

        setSimulating(true);
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation started.");

        simulationThread = new Thread(() -> {
            try {
                // Ensure clock is ON for simulation
                if (getStatus() == Status.OFF) {
                    setStatus(Status.ON);
                    DeviceLogger.logEvent(this, "SIMULATION_ADJUSTMENT", "SmartClock turned ON for simulation.");
                }

                while (!Thread.currentThread().isInterrupted() && getStatus() == Status.ON) {
                    LocalTime currentTime = getTime();
                    notifyObservers(currentTime);
                    DeviceLogger.logEvent(this, "TIME_UPDATE", "Current Time: " + currentTime.format(timeFormatter));
                    Thread.sleep(1000); // Update every second
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
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
        return "SmartClock{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", currentTime=" + getTime().format(timeFormatter) +
                ", currentDate=" + getDate() +
                ", observersCount=" + observers.size() +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                '}';
    }

    // --- Removed controlMenu(java.util.Scanner scanner) ---

    // --- NEW GUI method implementation ---
    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        SmartClockControlDialog dialog = new SmartClockControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }
}