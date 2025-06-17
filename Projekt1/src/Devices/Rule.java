package Devices;

import Devices.SmartDevice;
import Logger.DeviceLogger;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Rule<T extends SmartDevice> {
    private final T device;
    Predicate<T> condition;
    Consumer<T> action;
    private String description;
    private boolean enabled; // Nowe pole: czy reguła jest aktywna

    public Rule(T device, Predicate<T> condition, Consumer<T> action, String description) {
        this.device = device;
        this.condition = condition;
        this.action = action;
        this.description = description;
        this.enabled = true; // Domyślnie reguła jest włączona
    }

    public boolean check(){
        // Reguła jest sprawdzana tylko jeśli jest włączona
        return enabled && condition.test(device);
    }

    public void execute() {
        if (!enabled) {
            DeviceLogger.logEvent(device, "RULE_DISABLED", "Rule '" + this.description + "' is disabled and not executed.");
            return;
        }

        if (check()) { // check() już sprawdzi enabled
            action.accept(device);
            DeviceLogger.logEvent(device, "RULE_TRIGGERED", "Rule '" + this.description + "' triggered.");
        } else {
            DeviceLogger.logEvent(device, "RULE_CHECK", "Rule '" + this.description + "' not triggered (condition not met).");
        }
    }

    public T getDevice(){
        return device;
    }

    public Predicate<T> getPredicate(){
        return condition;
    }

    public Consumer<T> getConsumer(){
        return action;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            DeviceLogger.logEvent(device, "RULE_MANAGEMENT", "Rule '" + this.description + "' was " + (enabled ? "enabled" : "disabled") + ".");
        }
    }

    @Override
    public String toString() {
        return (enabled ? "[Enabled] " : "[Disabled] ") +
                "Rule for " + device.getName() + ": '" + description + "'";
    }
}