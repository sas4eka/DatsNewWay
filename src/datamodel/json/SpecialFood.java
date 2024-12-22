package datamodel.json;

import java.util.List;
import java.util.stream.Collectors;

public class SpecialFood {
    private List<Point3D> golden;
    private List<Point3D> suspicious;

    public SpecialFood() {
    }

    public SpecialFood(SpecialFood other) {
        this.golden = other.golden.stream().map(Point3D::new).collect(Collectors.toList());
        this.suspicious = other.suspicious.stream().map(Point3D::new).collect(Collectors.toList());
    }

    public List<Point3D> getGolden() {
        return golden;
    }

    public void setGolden(List<Point3D> golden) {
        this.golden = golden;
    }

    public List<Point3D> getSuspicious() {
        return suspicious;
    }

    public void setSuspicious(List<Point3D> suspicious) {
        this.suspicious = suspicious;
    }
}
