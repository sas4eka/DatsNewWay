package datamodel.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Point3D {
    private int x;
    private int y;
    private int z;

    public Point3D() {
    }

    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(Point3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    // Encode Point3D as an array in JSON
    @JsonValue
    public int[] toArray() {
        return new int[]{x, y, z};
    }

    // Decode Point3D from an array in JSON
    @JsonCreator
    public static Point3D fromArray(int[] coordinates) {
        if (coordinates.length != 3) {
            throw new IllegalArgumentException("Invalid Point3D array length: " + coordinates.length);
        }
        return new Point3D(coordinates[0], coordinates[1], coordinates[2]);
    }

    public int getDist(Point3D other) {
        return Math.abs(getX() - other.getX()) + Math.abs(getY() - other.getY()) + Math.abs(getZ() - other.getZ());
    }
}