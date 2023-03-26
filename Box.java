import java.awt.*;
import java.util.ArrayList;

public class Box extends Volume {

    public Box(double x, double y, double z, double length, double width, double height, Color col) {
        super(x, y, z, col);
        vertices.add(new Vector(x + length / 2.0, y + width / 2.0, z + height / 2.0));
        vertices.add(new Vector(x - length / 2.0, y + width / 2.0, z + height / 2.0));
        vertices.add(new Vector(x + length / 2.0, y - width / 2.0, z + height / 2.0));
        vertices.add(new Vector(x + length / 2.0, y + width / 2.0, z - height / 2.0));
        vertices.add(new Vector(x - length / 2.0, y - width / 2.0, z + height / 2.0));
        vertices.add(new Vector(x + length / 2.0, y - width / 2.0, z - height / 2.0));
        vertices.add(new Vector(x - length / 2.0, y + width / 2.0, z - height / 2.0));
        vertices.add(new Vector(x - length / 2.0, y - width / 2.0, z - height / 2.0));

        polygons.add(new CPolygon(vertices.get(0), vertices.get(1), vertices.get(2), col));
        polygons.add(new CPolygon(vertices.get(4), vertices.get(2), vertices.get(1), col));
        polygons.add(new CPolygon(vertices.get(0), vertices.get(2), vertices.get(3), col));
        polygons.add(new CPolygon(vertices.get(5), vertices.get(3), vertices.get(2), col));
        polygons.add(new CPolygon(vertices.get(0), vertices.get(3), vertices.get(1), col));
        polygons.add(new CPolygon(vertices.get(6), vertices.get(1), vertices.get(3), col));
        polygons.add(new CPolygon(vertices.get(7), vertices.get(4), vertices.get(6), col));
        polygons.add(new CPolygon(vertices.get(1), vertices.get(6), vertices.get(4), col));
        polygons.add(new CPolygon(vertices.get(7), vertices.get(5), vertices.get(4), col));
        polygons.add(new CPolygon(vertices.get(2), vertices.get(4), vertices.get(5), col));
        polygons.add(new CPolygon(vertices.get(7), vertices.get(6), vertices.get(5), col));
        polygons.add(new CPolygon(vertices.get(3), vertices.get(5), vertices.get(6), col));
    }
}
