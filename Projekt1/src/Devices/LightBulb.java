package Devices;

import Enums.Status;
import Interfaces.Switchable;
import Logger.DeviceLogger;
import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;
import SmartExceptions.DeviceDisabled;
import SmartExceptions.SimulationInterrupted;
import GUI.LightBulbControlDialog; // Import nowego dialogu GUI

import javax.swing.*;
import java.awt.Color;
import java.util.List;
// Usunięto import java.util.Scanner

public class LightBulb extends SmartDevice implements Switchable {

    private static final float DEFAULT_HUE = 30; // CIE HSB (Warm White)
    private static final float DEFAULT_SATURATION = 0.01f; // Niska saturacja dla białego światła
    private static final float DEFAULT_VALUE = 0.98f; // Wysoka jasność

    private float hue;
    private float saturation;
    private float value;

    // Zaktualizowany konstruktor: dodano deviceType
    public LightBulb(String name) {
        super(name, "LightBulb"); // Przekazujemy "LightBulb" jako deviceType
        this.hue = DEFAULT_HUE;
        this.saturation = DEFAULT_SATURATION;
        this.value = DEFAULT_VALUE;
        setStatus(Status.ON); // Żarówka domyślnie włączona po utworzeniu
    }

    @Override
    public void turnOn() throws AlreadyON {
        if (super.getStatus() == Status.OFF) {
            setStatus(Status.ON);
            // Po włączeniu przywracamy domyślne ustawienia koloru/jasności
            this.hue = DEFAULT_HUE;
            this.saturation = DEFAULT_SATURATION;
            this.value = DEFAULT_VALUE;
            DeviceLogger.logStatusChange(this, "Turned ON. Resetting color to default warm white.");
        } else {
            throw new AlreadyON("The LightBulb is already ON.");
        }
    }

    @Override
    public void turnOff() throws AlreadyOFF {
        if (getStatus() == Status.ON) {
            setStatus(Status.OFF);
            // Po wyłączeniu, ustawiamy wartości na 0, aby symulować brak światła (czarny kolor)
            this.hue = 0;
            this.saturation = 0;
            this.value = 0;
            DeviceLogger.logStatusChange(this, "Turned OFF.");
            stopSimulation(); // Zatrzymuje symulację po wyłączeniu urządzenia
        } else {
            throw new AlreadyOFF("The LightBulb is already OFF.");
        }
    }

    // Gettery dla GUI
    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getValue() {
        return value;
    }

    public void setHue(float hue) {
        if (hue >= 0 && hue <= 360) { // Zmieniono na <= 360, bo 360 to to samo co 0
            this.hue = hue;
            DeviceLogger.logStatusChange(this, "Hue set to " + String.format("%.2f", hue));
        } else {
            DeviceLogger.logEvent(this, "INVALID_INPUT", "Attempted to set Hue out of range (0-360): " + hue);
            // Nie rzucamy wyjątku tutaj, aby GUI mogło obsłużyć błąd bez crashowania
        }
    }

    public void setSaturation(float saturation) {
        if (saturation >= 0 && saturation <= 1) {
            this.saturation = saturation;
            DeviceLogger.logStatusChange(this, "Saturation set to " + String.format("%.2f", saturation));
        } else {
            DeviceLogger.logEvent(this, "INVALID_INPUT", "Attempted to set Saturation out of range (0-1): " + saturation);
        }
    }

    public void setValue(float value) {
        if (value >= 0 && value <= 1) {
            this.value = value;
            DeviceLogger.logStatusChange(this, "Brightness (Value) set to " + String.format("%.2f", value));
        } else {
            DeviceLogger.logEvent(this, "INVALID_INPUT", "Attempted to set Brightness (Value) out of range (0-1): " + value);
        }
    }

    /**
     * Konwertuje wartości HSL (Hue, Saturation, Lightness) na obiekt Color (RGB).
     * Wartości HSL są często używane do bardziej intuicyjnej kontroli koloru.
     * Metoda używa standardowej konwersji HSB (Hue, Saturation, Brightness/Value) na RGB.
     * Hue: 0-360 stopni
     * Saturation: 0-1 (0% - 100%)
     * Value (Brightness): 0-1 (0% - 100%)
     */
    public Color getRGBColor() {
        // Używamy standardowej metody Color.getHSBColor, która przyjmuje floaty w zakresie 0-1 dla wszystkich parametrów
        // Hue w Javie Color.getHSBColor() jest skalowane do 0-1, więc musimy podzielić naszą wartość hue przez 360.
        return Color.getHSBColor(this.hue / 360.0f, this.saturation, this.value);
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
            // Komunikat dla GUI będzie wyświetlony przez wywołującą metodę
            return;
        }

        setSimulating(true);
        DeviceLogger.logEvent(this, "SIMULATION_STATUS", getName() + " simulation started.");

        simulationThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && getStatus() == Status.ON) {
                    DeviceLogger.logEvent(this, "SIMULATION_ACTION", "*LightBulb is glowing*");
                    Thread.sleep(20000); // Symuluje działanie żarówki co 20 sekund
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
        return "LightBulb{" +
                "name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", hue=" + String.format("%.2f", hue) +
                ", saturation=" + String.format("%.2f", saturation) +
                ", value=" + String.format("%.2f", value) +
                ", simulating=" + (isSimulating() ? "Yes" : "No") +
                '}';
    }

    // --- Usunięto metodę controlMenu(Scanner scanner) ---

    // --- Implementacja NOWEJ metody GUI ---
    @Override
    public void showDeviceSpecificGUI(JFrame parentFrame, List<SmartDevice> allSystemDevices) {
        LightBulbControlDialog dialog = new LightBulbControlDialog(parentFrame, this);
        dialog.setVisible(true);
    }
}