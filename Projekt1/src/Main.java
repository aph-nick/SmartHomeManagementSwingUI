import GUI.SmartHomeGUI;
import House.SmartHomeSystem;

public class Main {
    public static void main(String[] args) {
        SmartHomeSystem system = new SmartHomeSystem();
        new SmartHomeGUI(system);
    }
}
