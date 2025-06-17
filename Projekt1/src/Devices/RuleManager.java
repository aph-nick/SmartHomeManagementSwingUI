package Devices; // Nowy pakiet dla RuleManagera i Rule, jeśli chcesz utrzymać porządek

import Logger.DeviceLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RuleManager {
    private static RuleManager instance;
    private final List<Rule<? extends SmartDevice>> allRules;
    private ScheduledExecutorService scheduler;

    private RuleManager() {
        allRules = new ArrayList<>();
        // Inicjalizuj scheduler, który będzie cyklicznie sprawdzał reguły
        scheduler = Executors.newScheduledThreadPool(1);
        // Sprawdzaj wszystkie reguły co 5 sekund (możesz dostosować)
        scheduler.scheduleAtFixedRate(this::checkAllRules, 0, 5, TimeUnit.SECONDS);
        DeviceLogger.logEvent(null, "SYSTEM_INIT", "RuleManager initialized. Rules will be checked every 5 seconds.");
    }

    public static synchronized RuleManager getInstance() {
        if (instance == null) {
            instance = new RuleManager();
        }
        return instance;
    }

    public void addRule(Rule<? extends SmartDevice> rule) {
        if (!allRules.contains(rule)) {
            allRules.add(rule);
            DeviceLogger.logEvent(rule.getDevice(), "RULE_MANAGEMENT", "Rule '" + rule.getDescription() + "' added to RuleManager.");
        }
    }

    public void removeRule(Rule<? extends SmartDevice> rule) {
        if (allRules.remove(rule)) {
            DeviceLogger.logEvent(rule.getDevice(), "RULE_MANAGEMENT", "Rule '" + rule.getDescription() + "' removed from RuleManager.");
        }
    }

    public List<Rule<? extends SmartDevice>> getAllRules() {
        return Collections.unmodifiableList(allRules);
    }

    /**
     * Sprawdza i wykonuje wszystkie aktywne reguły.
     * Ta metoda jest wywoływana cyklicznie przez scheduler.
     */
    private void checkAllRules() {
        // DeviceLogger.logEvent(null, "RULE_CHECK", "Checking all rules..."); // Może być zbyt często logowane
        for (Rule<? extends SmartDevice> rule : new ArrayList<>(allRules)) { // Iteruj po kopii, by uniknąć ConcurrentModificationException
            try {
                if (rule.check()) {
                    rule.execute(); // Rule.execute() teraz loguje, gdy jest triggered
                } else {
                    // Rule.execute() loguje, gdy nie jest triggered, więc tutaj nic nie robimy
                }
            } catch (Exception e) {
                DeviceLogger.logEvent(rule.getDevice(), "RULE_ERROR", "Error executing rule '" + rule.getDescription() + "': " + e.getMessage());
            }
        }
    }

    /**
     * Zatrzymuje scheduler reguł. Ważne do wywołania przy zamykaniu aplikacji.
     */
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    DeviceLogger.logEvent(null, "SYSTEM_SHUTDOWN", "RuleManager scheduler forced shutdown.");
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                DeviceLogger.logEvent(null, "SYSTEM_SHUTDOWN", "RuleManager scheduler shutdown interrupted.");
            }
            DeviceLogger.logEvent(null, "SYSTEM_SHUTDOWN", "RuleManager scheduler shut down gracefully.");
        }
    }
}