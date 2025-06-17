package House;

import Devices.SmartDevice; // Dodaj ten import
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Dodaj ten import

public class SmartHomeSystem {

    private final List<House> houses;
    private House selectedHouse;

    public SmartHomeSystem() {
        this.houses = new ArrayList<>();
        this.selectedHouse = null;
    }

    public boolean addHouse(House house) {
        if (findHouseByName(house.getName()).isPresent()) {
            return false;
        }
        houses.add(house);
        return true;
    }

    public boolean removeHouse(House house) {
        return houses.remove(house);
    }

    public Optional<House> findHouseByName(String name) {
        return houses.stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<House> getHouses() {
        return new ArrayList<>(houses);
    }

    public boolean hasHouses() {
        return !houses.isEmpty();
    }

    public void setSelectedHouse(House house) {
        if (houses.contains(house)) {
            this.selectedHouse = house;
        } else {
            throw new IllegalArgumentException("House not found in the system.");
        }
    }

    public House getSelectedHouse() {
        return selectedHouse;
    }

    public boolean hasSelectedHouse() {
        return selectedHouse != null;
    }

    @Override
    public String toString() {
        if (houses.isEmpty()) {
            return "No houses in the system.";
        }
        String result = "SmartHomeSystem Houses (" + houses.size() + "):\n";
        for (House h : houses) {
            result += h.toString() + "\n";
        }
        return result;
    }

    public List<House> getAllHouses() {
        return new ArrayList<>(houses);
    }

    // --- NOWE METODY POMOCNICZE W SmartHomeSystem ---

    /**
     * Zwraca listę wszystkich pokoi ze wszystkich domów w systemie.
     * Użyteczne do przeszukiwania zależności między urządzeniami.
     *
     * @return Lista wszystkich obiektów Room w systemie.
     */
    public List<Room> getAllRoomsInSystem() {
        return houses.stream()
                .flatMap(house -> house.getRooms().stream())
                .collect(Collectors.toList());
    }

    /**
     * Zwraca listę wszystkich urządzeń ze wszystkich pokoi we wszystkich domach w systemie.
     * Niezbędne do przekazywania do dialogów kontrolnych, które potrzebują dostępu do wszystkich urządzeń.
     *
     * @return Lista wszystkich obiektów SmartDevice w systemie.
     */
    public List<SmartDevice> getAllDevicesInSystem() {
        return houses.stream()
                .flatMap(house -> house.getRooms().stream())
                .flatMap(room -> room.getDevices().stream())
                .collect(Collectors.toList());
    }
}