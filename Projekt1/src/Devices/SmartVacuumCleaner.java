package Devices;

import Enums.FloorGarbage;
import Enums.SmartVacuumMode;
import Enums.Status;
import Interfaces.Pluggable;
import Interfaces.Switchable;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import GUI.SmartVacuumCleanerControlDialog; // Import the new GUI dialog

import javax.swing.*;
import java.util.List;

public class SmartVacuumCleaner extends SmartDevice
        implements Switchable, Pluggable {

    private int battery;
    private boolean isCharging;
    private SmartVacuumMode mode;
    private Outlet connectedOutlet; // Renamed for clarity, from 'outlet'

    public SmartVacuumCleaner(String name, Outlet outlet) { // Constructor now correctly takes an Outlet
        super(name, "SmartVacuumCleaner"); // Pass "SmartVacuumCleaner" as deviceType
        this.battery = 100;
        this.isCharging = false;
        this.mode = SmartVacuumMode.ECO;
        // The outlet passed in constructor should be immediately connected
        // For a more robust design, consider if the vacuum always starts connected.
        // For now, let's assume it doesn't automatically connect here.
        // The GUI will handle initial connection.
        this.connectedOutlet = null; // No outlet initially connected upon creation.
        DeviceLogger.logEvent(this, "CREATION", "SmartVacuumCleaner device created: " + name);
    }

    public static FloorGarbage getRandomGarbage() {
        Enums.FloorGarbage[] values = Enums.FloorGarbage.values();
        int index = (int) (Math.random() * values.length);
        return values[index];
    }

    public int getBattery() {
        return battery;
    }

    // New method to set battery for internal use/charging logic
    private void setBattery(int battery) {
        if (battery >= 0 && battery <= 100) {
            int oldBattery = this.battery;
            this.battery = battery;
            if (this.battery != oldBattery) {
                DeviceLogger.logEvent(this, "BATTERY_STATUS", "Battery level changed to: " + this.battery + "%");
            }
        } else {
            DeviceLogger.logEvent(this, "ERROR", "Attempted to set battery out of bounds: " + battery);
        }
    }

    public boolean isCharging() {
        return isCharging;
    }

    private void setCharging(boolean charging) {
        if (this.isCharging != charging) {
            this.isCharging = charging;
            DeviceLogger.logEvent(this, "CHARGE_STATUS", "Charging status changed to: " + (charging ? "Charging" : "Not Charging"));
        }
    }

    public SmartVacuumMode getMode() {
        return mode;
    }

    public void setMode(SmartVacuumMode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            DeviceLogger.logEvent(this, "SETTING_CHANGE", "Mode set to: " + mode);
        }
    }

    @Override
    public void turnOn() throws AlreadyON {
        if(getStatus() == Status.ON) {
            throw new AlreadyON("Vacuum cleaner is already ON.");
        }
        // Can only turn on if not charging and has some battery
        if (isCharging) {
            throw new AlreadyON("Vacuum cleaner is charging. Please disconnect from outlet first.");
        }
        if (battery == 0) {
            throw new AlreadyON("Battery is empty. Please charge the vacuum cleaner.");
        }
        setStatus(Status.ON);
        DeviceLogger.logEvent(this, "STATUS_CHANGE", "Turned ON.");
    }

    @Override
    public void turnOff() throws AlreadyOFF {
        if(getStatus() == Status.OFF) {
            throw new AlreadyOFF("Vacuum cleaner is already OFF.");
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
    public void connectToOutlet(Outlet outlet) {
        if (this.connectedOutlet != null) {
            if (this.connectedOutlet.equals(outlet)) {
                DeviceLogger.logEvent(this, "CONNECTION_INFO", "Already connected to this outlet: " + outlet.getName());
                return;
            } else {
                // If connecting to a different outlet, disconnect from the current one first
                disconnectFromOutlet(this.connectedOutlet);
            }
        }
        outlet.connectDevice(this); // Tell the outlet to connect this device
        this.connectedOutlet = outlet;
        setCharging(true); // Start charging when connected
        DeviceLogger.logEvent(this, "CONNECTION", "Connected to outlet: " + outlet.getName());

        // Immediately start charging simulation if outlet is on and vacuum is off
        if (outlet.isOn() && !isOn()) {
            startChargingSimulation();
        }
    }

    @Override
    public void disconnectFromOutlet(Outlet outlet) {
        if (this.connectedOutlet == null || !this.connectedOutlet.equals(outlet)) {
            DeviceLogger.logEvent(this, "CONNECTION_WARNING", "Not connected to outlet: " + outlet.getName() + " or no outlet connected.");
            return;
        }
        outlet.removeDevice(); // Tell the outlet to remove this device
        this.connectedOutlet = null;
        setCharging(false); // Stop charging when disconnected
        stopChargingSimulation(); // Stop any charging simulation
        DeviceLogger.logEvent(this, "CONNECTION", "Disconnected from outlet: " + outlet.getName());
    }

    @Override
    public Outlet getOutlet() {
        return this.connectedOutlet;
    }

    // Private method to handle charging simulation
    private void startChargingSimulation() {
        if (isCharging && battery < 100 && !isSimulating()) { // Only charge if needed and not already simulating main task
            setSimulating(true); // Use SmartDevice's isSimulating to block other simulations
            DeviceLogger.logEvent(this, "SIMULATION_STATUS", "Charging simulation started.");

            simulationThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted() && battery < 100 && isCharging && connectedOutlet != null && connectedOutlet.isOn()) {
                        setBattery(Math.min(battery + 1, 100)); // Increase battery
                        Thread.sleep(500); // Charge faster for simulation, e.g., 1% every 0.5 sec
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    DeviceLogger.logEvent(this, "SIMULATION_STATUS", "Charging simulation interrupted.");
                } finally {
                    setCharging(false); // Stop charging flag when done or interrupted
                    setSimulating(false); // Release simulation lock
                    DeviceLogger.logEvent(this, "SIMULATION_STATUS", "Charging simulation stopped.");
                    // If battery is full, vacuum can be turned off
                    if (battery == 100 && getStatus() == Status.OFF) {
                        DeviceLogger.logEvent(this, "BATTERY_STATUS", "Battery fully charged. Ready for use.");
                    }
                }
            });
            simulationThread.start();
        } else {
            DeviceLogger.logEvent(this, "CHARGE_ERROR", "Cannot start charging simulation (already charging, full, or main simulation running).");
        }
    }

    private void stopChargingSimulation() {
        if (isSimulating() && isCharging) { // Assuming isSimulating covers both types of simulation threads
            if (simulationThread != null && simulationThread.isAlive()) {
                simulationThread.interrupt();
            }
            // setSimulating(false) and setCharging(false) are handled in the charging simulation's finally block
        }
    }


    @Override
    public void simulate() throws DeviceDisabled, SimulationInterrupted {
        if (getStatus() == Status.OFF) {
            throw new DeviceDisabled(getName() + " is disabled. Turn it ON to simulate.");
        }
        if (battery == 0) {
            throw new DeviceDisabled(getName() + " has no battery. Please charge it first.");
        }
        if (isCharging) {
            throw new DeviceDisabled(getName() + " is charging. Disconnect from outlet to start cleaning.");
        }
        if (isSimulating()) { // Check if any simulation (cleaning or charging) is already running
            return;
        }

        setSimulating(true);
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " cleaning simulation started in mode: " + getMode());

        simulationThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && getStatus() == Status.ON && battery > 0) {
                    FloorGarbage pickedGarbage = getRandomGarbage();
                    DeviceLogger.logEvent(this, "CLEANING_ACTION", "Picked up: " + pickedGarbage + ". Battery: " + battery + "%");
                    setBattery(battery - 1); // Decrease battery

                    if (battery == 0) {
                        DeviceLogger.logEvent(this, "BATTERY_STATUS", "Battery empty. Returning to base.");
                        turnOff(); // Automatically turn off and stop simulation
                        break;
                    }

                    long sleepTime = 0;
                    switch (mode) {
                        case ECO -> sleepTime = 5000; // Faster for simulation
                        case FAST -> sleepTime = 3000;
                        case ULTRA -> sleepTime = 1000;
                    }
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " cleaning simulation interrupted.");
            } catch (AlreadyOFF e) { // Catch potential exceptions from turnOff()
                DeviceLogger.logEvent(this, "SIMULATION_ERROR", "Error during cleaning simulation: " + e.getMessage());
            } finally {
                setSimulating(false);
                DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " cleaning simulation stopped.");
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
        return "SmartVacuumCleaner{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", battery=" + battery + "%" +
                ", mode=" + mode +
                ", isCharging=" + (isCharging ? "Yes" : "No") +
                ", connectedOutlet=" + (connectedOutlet != null ? connectedOutlet.getName() : "None") +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                '}';
    }

    // --- Removed controlMenu(java.util.Scanner scanner) ---

    // --- NEW GUI method implementation ---
    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        SmartVacuumCleanerControlDialog dialog = new SmartVacuumCleanerControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }
}