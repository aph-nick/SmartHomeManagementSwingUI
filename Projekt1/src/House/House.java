package House;

import Enums.HouseColor;
import Enums.HouseType;

import java.util.ArrayList;
import java.util.List;

public class House {
    private String name;
    private Localisation localisation;
    private List<Room> rooms;
    private int roomCount;
    private HouseType houseType;
    private HouseColor color;

    public House(String name, Localisation localisation, HouseType houseType, HouseColor color) {
        this.name = name;
        this.localisation = localisation;
        this.houseType = houseType;
        this.color = color;
        this.rooms = new ArrayList<>();
        this.roomCount = 0;
    }

    public String getName() {
        return name;
    }

    public Localisation getLocalisation() {
        return localisation;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public HouseType getHouseType() {
        return houseType;
    }

    public HouseColor getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocalisation(Localisation localisation) {
        this.localisation = localisation;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
        updateRoomCount();
    }

    public void setHouseType(HouseType houseType) {
        this.houseType = houseType;
    }

    public void setColor(HouseColor color) {
        this.color = color;
    }

    public void addRoom(Room room) {
        if (!rooms.contains(room)) {
            rooms.add(room);
            roomCount++;
        }
    }

    public void removeRoom(Room room) {
        if (rooms.remove(room)) {
            roomCount--;
        }
    }

    public void clearRooms() {
        rooms.clear();
        roomCount = 0;
    }

    private void updateRoomCount() {
        this.roomCount = this.rooms.size();
    }

    public double getTotalArea() {
        double total = 0;
        for (Room room : rooms) {
            total += room.getArea();
        }
        return total;
    }

    @Override
    public String toString() {
        return "House: " +
                "name = '" + name + '\'' +
                ", localisation = " + localisation +
                ", houseType = " + houseType +
                ", color = " + color +
                ", roomCount = " + roomCount +
                ", rooms = " + rooms +
                ", totalArea = " + getTotalArea() +
                "\n" + HouseVisualizer.draw(houseType);
    }
}
