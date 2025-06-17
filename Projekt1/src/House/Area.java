package House;

public class Area {
    private double dimensionX;
    private double dimensionY;

    public Area(double dimensionX, double dimensionY) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
    }

    public double getDimensionX() {
        return dimensionX;
    }
    public void setDimensionX(double dimensionX) {
        this.dimensionX = dimensionX;
    }

    public double getDimensionY() {
        return dimensionY;
    }

    public void setDimensionY(double dimensionY) {
        this.dimensionY = dimensionY;
    }

    public double getArea() {
        return dimensionX * dimensionY;
    }

    public String toString() {
        return "Area [dimensionX=" + dimensionX + ", dimensionY=" + dimensionY + "]";
    }
}
