package SmartExceptions;

public class SimulationInterrupted extends RuntimeException {
    public SimulationInterrupted(String message) {
        super(message);
    }
}
