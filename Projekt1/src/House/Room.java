package House;

import Devices.SmartDevice;
import Enums.RoomType;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String name;
    private RoomType roomType;
    private List<SmartDevice> devices;
    private double length;
    private double width;
    private double area;

    public Room(String name, RoomType roomType, double length, double width) {
        this.name = name;
        this.roomType = roomType;
        this.devices = new ArrayList<>();
        this.length = length;
        this.width = width;
        this.area = length * width;
    }

    public String getName() {
        return name;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public List<SmartDevice> getDevices() {
        return devices;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getArea() {
        return area;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public void setLength(double length) {
        this.length = length;
        recalculateArea();
    }

    public void setWidth(double width) {
        this.width = width;
        recalculateArea();
    }

    private void recalculateArea() {
        this.area = this.length * this.width;
    }

    public void addDevice(SmartDevice device) {
        if (!devices.contains(device)) {
            devices.add(device);
        }
    }

    public void removeDevice(SmartDevice device) {
        devices.remove(device);
    }


    @Override
    public String toString() {
        return "Room: " +
                "name = '" + name + '\'' +
                ", roomType = " + roomType +
                ", numberOfDevices = " + devices.size() +
                ", length = " + length +
                ", width = " + width +
                ", area = " + area;
    }
}
