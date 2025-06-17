package House;

import Enums.HouseType;

public class HouseVisualizer {

    public static String draw(HouseType type) {
        return switch (type) {
            case DETACHED -> drawDetached();
            case SEMI_DETACHED -> drawSemiDetached();
            case APARTMENT -> drawApartment();
            case CABIN -> drawCabin();
            case MANSION -> drawMansion();
            default -> "[ No visual available for this house type ]";
        };
    }

    private static String drawDetached() {
        return
                        "   _____   \n" +
                        "  /     \\  \n" +
                        " /_______\\ \n" +
                        " |  ___  | \n" +
                        " | |   | | \n" +
                        " |_|___|_| \n";
    }

    private static String drawSemiDetached() {
        return
                        "   _____    _____  \n" +
                        "  /     \\ /     \\ \n" +
                        " /_______V_______\\\n" +
                        " |  ___     ___   | \n" +
                        " | |   |   |   |  | \n" +
                        " |_|___|___|___|__| \n";
    }

    private static String drawApartment() {
        return
                        "  _________  \n" +
                        " |  _   _  | \n" +
                        " | |_| |_| | \n" +
                        " |  _   _  | \n" +
                        " | |_| |_| | \n" +
                        " |_________| \n";
    }

    private static String drawCabin() {
        return
                        "   /\\    \n" +
                        "  /__\\   \n" +
                        " | [] |  \n" +
                        " |____|  \n";
    }

    private static String drawMansion() {
        return
                        "  _____________  \n" +
                        " |  _   _   _  | \n" +
                        " | |_| |_| |_| | \n" +
                        " |  _   _   _  | \n" +
                        " | |_| |_| |_| | \n" +
                        " |_____|_|_____| \n";
    }
}