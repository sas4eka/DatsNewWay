package datamodel.json;

public class SnakeAction {
    String id;
    Point3D direction;

    public SnakeAction(String id, Point3D direction) {
        this.id = id;
        this.direction = direction;
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
}
