package Interfaces;

public interface SensorDevice<T> {
    T readValue();
    String getUnit();
}
