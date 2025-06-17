package House;

public class Localisation {
    private double x;
    private double y;
    private double z;

    public Localisation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String toString() {
        return "X: " + x + " Y: " + y + " Z: " + z;
    }
}
