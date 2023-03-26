import java.awt.*;

public class CPolygon {

    //Vertex order, surface normal is by right hand rule, vertices read in CC order.
    public Vector[] vertices;
    public Vector normal;
    public Color[] color;
    public Point[] drawVertices;
    public double[] drawZ;

    public Vector avgPos;

    public CPolygon(Vector p1, Vector p2, Vector p3) {
        vertices = new Vector[3];
        vertices[0] = p1;
        vertices[1] = p2;
        vertices[2] = p3;
        normal = p2.clone().sub(p1).cross(p3.clone().sub(p1));
        color = new Color[3];
        color[0] = Color.getHSBColor((float)(Math.random() * 360), 1.0f, 1.0f);
        color[1] = Color.getHSBColor((float)(Math.random() * 360), 1.0f, 1.0f);
        color[2] = Color.getHSBColor((float)(Math.random() * 360), 1.0f, 1.0f);

        drawVertices = new Point[]{new Point(0, 0), new Point(0, 0), new Point(0, 0)};
        drawZ = new double[3];
    }
    public CPolygon(Vector p1, Vector p2, Vector p3, Color c) {
        vertices = new Vector[3];
        vertices[0] = p1;
        vertices[1] = p2;
        vertices[2] = p3;
        normal = p2.clone().sub(p1).cross(p3.clone().sub(p1));
        color = new Color[3];
        color[0] = c;
        color[1] = c;
        color[2] = c;
//        color = Color.getHSBColor((float)(Math.random() * 360), 1.0f, 1.0f);

        drawVertices = new Point[]{new Point(0, 0), new Point(0, 0), new Point(0, 0)};
        drawZ = new double[3];
    }

    private void updateAvgPos() {
        Vector avg = new Vector();
        for (int i = 0; i < vertices.length; i++) {
            avg.x += vertices[i].x;
            avg.y += vertices[i].y;
            avg.z += vertices[i].z;
        }
        avg.scale(1.0 / vertices.length);
//        return avg;
        avgPos = avg;
    }

    public void updateDrawP(double[] tMat, Vector cPos, Vector cDir) {
        updateAvgPos();
        Vector[] tr = new Vector[3];
        tr[0] = vertices[0].clone().RightMatrixMult(tMat);
        tr[1] = vertices[1].clone().RightMatrixMult(tMat);
        tr[2] = vertices[2].clone().RightMatrixMult(tMat);
        for (int i = 0; i < 3; i++) {
            drawVertices[i].x = (int)tr[i].x;
            drawVertices[i].y = (int)tr[i].y;
            drawZ[i] = tr[i].z;
        }
    }
}
