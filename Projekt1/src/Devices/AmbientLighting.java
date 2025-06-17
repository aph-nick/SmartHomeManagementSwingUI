package Devices;

// Usunięto import CLI.RuleManagerCLI
import Enums.AmbientColors;
import Enums.AmbientIntensity;
import Enums.Status;
import GUI.AmbientLightingControlDialog;
import Interfaces.ObservableDevice;
import Interfaces.Observer;
import Interfaces.Switchable;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
// Usunięto import java.util.Scanner

public class AmbientLighting extends SmartDevice
        implements Observer<LocalTime>, Switchable {
    private AmbientColors ambientColor;
    private AmbientIntensity ambientIntensity;
    private final SmartClock smartClock;

    // Usunięto RuleManagerCLI ruleManagerCLI;

    private LocalTime onTime = LocalTime.of(21, 0); /// def
    private LocalTime offTime = LocalTime.of(6, 0); /// def

    private final List<Rule<AmbientLighting>> rules = new ArrayList<>();

    // Zaktualizowany konstruktor: usunięto Scanner
    public AmbientLighting(String name, SmartClock smartClock) {
        super(name, "AmbientLighting"); // Przekazujemy "AmbientLighting" jako deviceType
        this.smartClock = smartClock;
        smartClock.addObserver(this);
    }

    public void setOnTime(LocalTime onTime) {
        this.onTime = onTime;
    }

    public void setOffTime(LocalTime offTime) {
        this.offTime = offTime;
    }

    public void setAmbientColor(AmbientColors ambientColor) {
        this.ambientColor = ambientColor;
    }

    public AmbientColors getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientIntensity(AmbientIntensity ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }

    public AmbientIntensity getAmbientIntensity() {
        return ambientIntensity;
    }

    @Override
    public void simulate() throws DeviceDisabled, SimulationInterrupted {
        if (smartClock == null) {
            throw new DeviceDisabled("Cannot simulate Ambient Lighting: SmartClock is not connected.");
        }

        if (getStatus() == Status.OFF) {
            throw new DeviceDisabled(getName() + " is disabled. Turn it ON to simulate.");
        }

        if (isSimulating()) {
            return;
        }

        setSimulating(true);

        simulationThread = new Thread(() -> {
            DeviceLogger.logEvent(this, "SIMULATION_STATUS", "Ambient Lighting simulation started.");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    update(); // Wywołuje zasady
                    Thread.sleep(5000); // Odświeżanie co 5 sekund
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                DeviceLogger.logEvent(this, "SIMULATION_STATUS", "Ambient Lighting simulation interrupted.");
                setSimulating(false);
            } finally {
                setSimulating(false);
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
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", "Ambient Lighting simulation stopped.");
    }

    @Override
    public void update() {
        LocalTime currentTime = smartClock.getTime();

        if (currentTime.equals(onTime) && getStatus() == Status.OFF) {
            try {
                turnOn();
                DeviceLogger.logRuleTriggered(this, "Turned ON by SmartClock at " + onTime);
            } catch (AlreadyON e) {
                // Already ON, do nothing
            }
        } else if (currentTime.equals(offTime) && getStatus() == Status.ON) {
            try {
                turnOff();
                DeviceLogger.logRuleTriggered(this, "Turned OFF by SmartClock at " + offTime);
            } catch (AlreadyOFF e) {
                // Already OFF, do nothing
            }
        }

        for (Rule<AmbientLighting> rule : rules) {
            rule.execute();
        }
    }

    @Override
    public void update(ObservableDevice device, Object object) {
        update(); // Wywołaj metodę update() bez parametrów
    }

    @Override
    public void turnOn() throws AlreadyON {
        if (smartClock == null) {
            throw new AlreadyON("Cannot turn on Ambient Lighting: No SmartClock connected.");
        }

        if (getStatus() == Status.OFF) {
            setStatus(Status.ON);
            DeviceLogger.logStatusChange(this, "Turned ON");
        } else {
            throw new AlreadyON("Ambient lighting is already ON.");
        }
    }

    @Override
    public void turnOff() throws AlreadyOFF {
        if(getStatus() == Status.ON) {
            setStatus(Status.OFF);
            DeviceLogger.logStatusChange(this, "Turned OFF");
        } else {
            throw new AlreadyOFF("Ambient lighting is already OFF.");
        }
    }

    @Override
    public boolean isOn() {
        return getStatus() == Status.ON;
    }

    public SmartClock getSmartClock() {
        return smartClock;
    }

    public void addRule(Rule<AmbientLighting> rule) {
        rules.add(rule);
    }

    public void removeRule(Rule<AmbientLighting> rule) {
        rules.remove(rule);
    }
    public List<Rule<AmbientLighting>> getRules() {
        return rules;
    }
    @Override
    public String toString() {
        return "AmbientLighting{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", color=" + ambientColor +
                ", intensity=" + ambientIntensity +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                ", onTime=" + onTime +
                ", offTime=" + offTime +
                '}';
    }

    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        AmbientLightingControlDialog dialog = new AmbientLightingControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }

    public LocalTime getOnTime() {
        return onTime;
    }

    public LocalTime getOffTime() {
        return offTime;
    }
}