package datamodel.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Food {
    @JsonProperty("c")
    private Point3D coordinates;
    private int points;
    private int type;

    public Food() {
    }

    public Food(Food other) {
        this.coordinates = new Point3D(other.coordinates);
        this.points = other.points;
        this.type = other.type;
    }

    public Point3D getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point3D coordinates) {
        this.coordinates = coordinates;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
