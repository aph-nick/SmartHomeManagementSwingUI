package Devices;

import Enums.Status;
import House.Room;
import Interfaces.Switchable;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;

import java.util.List;
import java.util.UUID;
import javax.swing.*;

public abstract class SmartDevice
        implements Switchable {

    private UUID id;
    private String name;
    private Status status;
    private boolean simulating;
    private Room room;

    protected Thread simulationThread;

    private String deviceType; // Pole przechowujące typ urządzenia

    public SmartDevice(String name, String deviceType) { // Zmiana konstruktora: dodajemy deviceType
        this.id = UUID.randomUUID();
        this.name = name;
        this.status = Status.ON;
        this.simulating = false;
        this.deviceType = deviceType;
    }

    public UUID getId() {
        return this.id;
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public abstract void simulate() throws DeviceDisabled, SimulationInterrupted;

    public abstract void stopSimulation();

    public abstract void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices);

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "SmartDevice ID: " + id + "; Name: " + name + "; Status: " + status;
    }

    public boolean isSimulating() {
        return simulating;
    }

    public void setSimulating(boolean simulating) {
        this.simulating = simulating;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}