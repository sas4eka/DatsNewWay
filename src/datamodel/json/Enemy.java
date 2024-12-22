package datamodel.json;

import java.util.List;
import java.util.stream.Collectors;

public class Enemy {
    private List<Point3D> geometry;
    private String status;
    private int kills;

    public Enemy() {
    }

    public Enemy(Enemy other) {
        this.geometry = other.geometry.stream().map(Point3D::new).collect(Collectors.toList());
        this.status = other.status;
        this.kills = other.kills;
    }

    public List<Point3D> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<Point3D> geometry) {
        this.geometry = geometry;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }
}
