package Logger;

import Devices.SmartDevice;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DeviceLogger {

    private static final String LOG_FILE = "device_events.tsv";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean headerWritten = false;

    private static final String HEADER = "TIMESTAMP\tDEVICE_ID\tDEVICE_TYPE\tROOM_NAME\tEVENT_TYPE\tDESCRIPTION";

    private static void writeHeaderIfNeeded() {
        if (headerWritten) return;

        Path path = Paths.get(LOG_FILE);
        if (Files.exists(path)) {
            try {
                String firstLine = Files.lines(path).findFirst().orElse("");
                if (HEADER.equals(firstLine)) {
                    headerWritten = true;
                    return;
                }
            } catch (IOException ignored) { }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(HEADER);
            writer.newLine();
            headerWritten = true;
        } catch (IOException e) {
            System.err.println("Failed to write log header: " + e.getMessage());
        }
    }

    public static void logEvent(SmartDevice device, String eventType, String description) {
        writeHeaderIfNeeded();

        String cleanDescription = description.replace("\t", " ");

        String roomName = "Unknown";

        if (device.getRoom() != null && device.getRoom().getName() != null) {
            roomName = device.getRoom().getName();
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.join("\t",
                timestamp,
                device.getId().toString(),
                device.getName(),
                device.getRoom().getName(),
                eventType,
                cleanDescription
        );

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(LOG_FILE),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Logger error: unable to write log entry. " + e.getMessage());
        }
    }


    public static void logDeviceAdded(SmartDevice device) {
        logEvent(device, "DEVICE_ADDED", "New Device has been added.");
    }

    public static void logStatusChange(SmartDevice device, String description) {
        logEvent(device, "STATUS_CHANGE", description);
    }

    public static void logRuleTriggered(SmartDevice device, String description) {
        logEvent(device, "RULE_TRIGGERED", description);
    }
}
