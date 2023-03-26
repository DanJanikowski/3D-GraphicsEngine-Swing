import java.awt.*;
import java.util.ArrayList;

public abstract class Volume {

    protected Color color;
    protected Vector pos;
    protected ArrayList<Vector> vertices;
    protected ArrayList<CPolygon> polygons;

    public Volume(double x, double y, double z, Color col) {
        pos = new Vector(x, y, z);
        color = col;
        vertices = new ArrayList<>();
        polygons = new ArrayList<>();
    }
}