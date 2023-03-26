import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class CustomGraphics {

    private double[][] zBuffer;

    public CustomGraphics() {
        zBuffer = new double[Main.WindowSize.width][Main.WindowSize.height];
    }

    /*
        Old Method:
            Find triangle mind and max bounds then iterate over every pixel and fill them in if they are within the bounds of the triangle
        New Method:
            Sort points by increasing y coords then fill the triangle by calculating boundary points using slope and scanning across
     */
    public BufferedImage rasterize(BufferedImage bi, Camera camera, ArrayList<CPolygon> polygons) {
        for (int i = 0; i < zBuffer.length; i++)
            for (int j = 0; j < zBuffer[0].length; j++)
                zBuffer[i][j] = 0;
        //RASTERIZATION
        Point[] tempArray = new Point[3];
        polygons.forEach(p -> {

            //NEW METHOD===============================================================================================================================
            // Sort the points by increasing y coords or x if two y are equal
            tempArray[0] = new Point(p.drawVertices[0]);
            tempArray[1] = new Point(p.drawVertices[1]);
            tempArray[2] = new Point(p.drawVertices[2]);
            Arrays.sort(tempArray, new SortByY());
            if (tempArray[0].y == tempArray[2].y) return;
            double Ax = p.drawVertices[0].x, Ay = p.drawVertices[0].y, Az = p.drawZ[0];
            double Bx = p.drawVertices[1].x, By = p.drawVertices[1].y, Bz = p.drawZ[1];
            double Cx = p.drawVertices[2].x, Cy = p.drawVertices[2].y, Cz = p.drawZ[2];
            double BAx = (Bx - Ax), BAy = (By - Ay), BAz = (Bz - Az), CAx = (Cx - Ax), CAy = (Cy - Ay), CAz = (Cz - Az);
            double frac1 = (BAx*CAz-CAx*BAz)/(BAx*CAy-CAx*BAy), frac2 = (BAy*CAz-CAy*BAz)/(BAx*CAy-CAx*BAy);
            boolean shortside = (By - Ay) * (Cx - Ax) < (Bx - Ax) * (Cy - Ay); // false=left side, true=right side
            int yiters = (int)(Cy - Ay), half = (int)(By - Ay);
            double zVal;
            double xs = (int)Ax, xf = (int)Ax;
            double dx1, dx2, dx3;
            dx1 = (Bx - Ax) / (By - Ay);
            dx2 = (Cx - Bx) / (Cy - By);
            dx3 = (Cx - Ax) / (Cy - Ay);
            int xCoord, yCoord;
            for (int i = 0; i < yiters; i++) {// SCANLINE setting all pixel colors and depths
                yCoord = (int)Ay + i;
                if (yCoord <= -Main.WindowSize.height/2 || yCoord >= Main.WindowSize.height/2) continue;
                for (int j = (int)xs; j < xf; j++) {
                    xCoord = j;
                    if (xCoord <= -Main.WindowSize.width/2 || xCoord >= Main.WindowSize.width/2) continue;
                    crtSetRGB(bi, xCoord, yCoord, p.color[0]);
                }
                // Now increment the left and right x bounds
                if (shortside) {
                    if (i <= half) xf += dx1;
                    else xf += dx2;
                    xs += dx3;
                } else {
                    if (i <= half) xs += dx1;
                    else xs += dx2;
                    xf += dx3;
                }
            }


//          THE OLD METHOD==============================================================================================================================
            //Vals specific per polygon to calculate z depths
//            double Ax = p.drawVertices[0].x, Ay = p.drawVertices[0].y, Az = p.drawZ[0];
//            double Bx = p.drawVertices[1].x, By = p.drawVertices[1].y, Bz = p.drawZ[1];
//            double Cx = p.drawVertices[2].x, Cy = p.drawVertices[2].y, Cz = p.drawZ[2];
//            double BAx = (Bx - Ax), BAy = (By - Ay), BAz = (Bz - Az), CAx = (Cx - Ax), CAy = (Cy - Ay), CAz = (Cz - Az);
//            double frac1 = (BAx*CAz-CAx*BAz)/(BAx*CAy-CAx*BAy), frac2 = (BAy*CAz-CAy*BAz)/(BAx*CAy-CAx*BAy);
//            int minX = Math.max((int)min(Ax, Bx, Cx), -Main.WindowSize.width/2), minY = Math.max((int)min(Ay, By, Cy), -Main.WindowSize.height/2 + 1),
//                    maxX = Math.min((int)max(Ax, Bx, Cx), Main.WindowSize.width/2), maxY = Math.min((int)max(Ay, By, Cy), Main.WindowSize.height/2);
//            for (int j = minY; j < maxY; j++) {
//                for (int i = minX; i < maxX; i++) {
//                    if (containedIn(i, j, p.drawVertices[0], p.drawVertices[1], p.drawVertices[2])) {
//                        double zVal = Az + frac1 * (j - Ay) - frac2 * (i - Ax);
//                        int iW = i + Main.WindowSize.width / 2, jH = j + Main.WindowSize.height / 2;
//                        if (zBuffer[iW][jH] == 0 || zBuffer[iW][jH] > zVal) {
//                            zBuffer[iW][jH] = zVal;
//                            crtSetRGB(bi, i, j, p.color[0]); //Change this to interpolate color
//                        }
//                    }
//                }
//            }
        });
        return bi;
    }
    public boolean containedIn(int x, int y, Point a, Point b, Point c) {
        double d1 = sign(new Point(x, y), a, b);
        double d2 = sign(new Point(x, y), b, c);
        double d3 = sign(new Point(x, y), c, a);
        boolean hasNeg, hasPos;
        hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos);
    }
    public double sign(Point a, Point b, Point c) {
        return (a.x - c.x) * (b.y - c.y) - (b.x - c.x) * (a.y - c.y);
    }
    private double min(double x, double y, double z) {
        if (x < y) {
            if (x < z) return x;
            else return z;
        } else {
            if (y < z) return y;
            else return z;
        }
    }
    private double max(double x, double y, double z) {
        if (x < y) {
            if (z < y) return y;
            else return z;
        } else {
            if (z < x) return x;
            else return z;
        }
    }

    //Since drawing to buffered image, we manually reflect and translate coords.
    private void crtSetRGB(BufferedImage bi, double x, double y, Color color) {
        bi.setRGB((int)(x + Main.WindowSize.width / 2.0),
                (int)(-y + Main.WindowSize.height / 2.0),
                color.getRGB());
    }
}

class SortByY implements Comparator<Point> {
    @Override
    public int compare(Point p, Point q) {
        return p.y < q.y ? -1 : p.y > q.y ? 1 : p.x < q.x ? -1 : 0;
    }
}