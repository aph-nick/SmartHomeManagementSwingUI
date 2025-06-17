package Interfaces;

import SmartExceptions.AlreadyOFF;
import SmartExceptions.AlreadyON;

public interface Switchable {
    void turnOn() throws AlreadyON;
    void turnOff() throws AlreadyOFF;
    boolean isOn();
}
