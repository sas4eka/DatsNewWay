package datamodel.json;

import java.util.List;
import java.util.stream.Collectors;

public class Snake {
    private String id;
    private Point3D direction;
    private Point3D oldDirection;
    private List<Point3D> geometry;
    private int deathCount;
    private String status;
    private int reviveRemainMs;

    public Snake() {
    }

    public Snake(Snake other) {
        this.id = other.id;
        this.direction = new Point3D(other.direction);
        this.oldDirection = new Point3D(other.oldDirection);
        this.geometry = other.geometry.stream().map(Point3D::new).collect(Collectors.toList());
        this.deathCount = other.deathCount;
        this.status = other.status;
        this.reviveRemainMs = other.reviveRemainMs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Point3D getDirection() {
        return direction;
    }

    public void setDirection(Point3D direction) {
        this.direction = direction;
    }

    public Point3D getOldDirection() {
        return oldDirection;
    }

    public void setOldDirection(Point3D oldDirection) {
        this.oldDirection = oldDirection;
    }

    public List<Point3D> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<Point3D> geometry) {
        this.geometry = geometry;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getReviveRemainMs() {
        return reviveRemainMs;
    }

    public void setReviveRemainMs(int reviveRemainMs) {
        this.reviveRemainMs = reviveRemainMs;
    }
}
