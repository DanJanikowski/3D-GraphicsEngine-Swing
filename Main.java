import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Thread.sleep;

/**
 * Created by dan.janikowski on 12/21/2015.
 */
public class Main {

//    public static Dimension WindowSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static final Dimension WindowSize = new Dimension(900, 900);

    public static void main(String[] args) {
        JFrame frame = new JFrame("Engine");
        Space space = new Space(WindowSize);
        frame.add(space);
        frame.setSize(WindowSize);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        space.startThread();
    }

    public static void printMatrix(double[] m) {
        for (int i = 0; i < 16; i++) {
            System.out.print(m[i] + " ");
            if (i%4 == 3) System.out.println();
        }
    }
}


/**
 * Created by dan.janikowski on 12/21/2015.
 */
class Space extends Canvas implements Runnable, MouseListener, MouseMotionListener, KeyListener {

    private CustomGraphics customGraphics;

    private Camera camera;
    private byte[] cMoveArr;

    private Random rand;
    private Robot rob;
    private ArrayList<Vector> points;
    private ArrayList<CPolygon> polygons, toDraw, toRemove;
    private ArrayList<Volume> shapes;

    private boolean drawCorners = false, drawMode = false;

    public Space(Dimension dim) {
        setBackground(Color.BLACK);
        invisibleMouse();

        setPreferredSize(Main.WindowSize);
        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);

        rand = new Random();

        customGraphics = new CustomGraphics();

        camera = new Camera(new Vector(0, 0, 200));
        cMoveArr = new byte[6];
        for (int i = 0; i < cMoveArr.length; i++) cMoveArr[i] = 0;

        points = new ArrayList<>();
        polygons = new ArrayList<>();
        toDraw = new ArrayList<>();
        toRemove = new ArrayList<>();

        double dubeSz = 200;
//        polygons.add(new CPolygon(new Vector(0, 0, 0), new Vector(100, 100, 0), new Vector(-100, 150, 0)));
        for (int i = 0; i < 40; i++) polygons.add(new CPolygon(new Vector(rand.nextDouble()*dubeSz - dubeSz/2, rand.nextDouble()*dubeSz - dubeSz/2, rand.nextDouble()*dubeSz - dubeSz/2),
                new Vector(rand.nextDouble()*dubeSz - dubeSz/2, rand.nextDouble()*dubeSz - dubeSz/2, rand.nextDouble()*dubeSz - dubeSz/2),
                new Vector(rand.nextDouble()*dubeSz - dubeSz/2, rand.nextDouble()*dubeSz - dubeSz/2, rand.nextDouble()*dubeSz - dubeSz/2)));

        shapes = new ArrayList<>();
//        shapes.add(new Box(0, 0, 0, 100, 100, 100, Color.RED));
//        shapes.add(new Box(100, 200, 300, 100, 50, 400, Color.GREEN));

        shapes.forEach(shape -> {
            points.addAll(shape.vertices);
            polygons.addAll(shape.polygons);
        });
    }

    private void invisibleMouse() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT);
        Cursor invisibleCursor = toolkit.createCustomCursor(cursorImage, new Point(0,0), "InvisibleCursor");
        setCursor(invisibleCursor);
    }

    public void startThread() {
        new Thread(this).start();
    }

    private double LastRefresh = 0, Checks = 0, drawFPS = 0, LastFPSCheck = 0, MaxFPS = 1000;
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            update();
            render();
            long timeSLU = (long) (System.currentTimeMillis() - LastRefresh);
            Checks++;
            if (Checks >= 15) {
                drawFPS = Checks / ((System.currentTimeMillis() - LastFPSCheck) / 1000.0);
                LastFPSCheck = System.currentTimeMillis();
                Checks = 0;
            }
            if (timeSLU < 1000.0 / MaxFPS) {
                try {
                    Thread.sleep((long) (1000.0 / MaxFPS - timeSLU));
//                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LastRefresh = System.currentTimeMillis();
        }
    }

    //Update camrea position, update polygons draw positions and set avg distance to camera, sort polygons by average depth
    private void update() {
        camera.moveCamera(cMoveArr);
        camera.updateCombinedMatrix();
        polygons.parallelStream().forEach(p -> p.updateDrawP(camera.tMat, camera.pos, camera.bs[2]));
        Collections.sort(polygons, new SortByDistance(camera));

        /*
        toDraw used to store polygons which will be drawn
        - first clear it
        - must add the first one so there is one to compare the rest to
        - (TEMP) do not draw any polygons where cam dir dotted with dir to avg of triangle is positive (negative)
        - if it is negative then get the closest vertex z coord to the camera
        - go through all next other polygons in toDraw and if all vertices are within bounds of previous and depths are greater than max then do not add it
         */


        // SPLIT THIS INTO INDIVIDUAL POLYGON PART AND MULTITHREAD IT
            //ALSO POSSIBLY GET RID OF SECOND PART THAT CHECKS FOR POLYGONS BEHIND OTHERS
        toDraw.clear();
        toRemove.clear();
        toDraw.addAll(polygons);

        toDraw.forEach(td -> {
            boolean lookingAtVisibleFace = camera.pos.clone().sub(td.avgPos).dot(td.normal) > 0;
            boolean polygonInfrontOfCamera = camera.bs[2].dot(camera.pos.clone().sub(td.vertices[0])) > 0 &&
                    camera.bs[2].dot(camera.pos.clone().sub(td.vertices[1])) > 0 &&
                    camera.bs[2].dot(camera.pos.clone().sub(td.vertices[2])) > 0;
//            boolean polygonInfrontOfCamera = true;
            if (!(lookingAtVisibleFace && polygonInfrontOfCamera)) toRemove.add(td);
        });
        toDraw.removeAll(toRemove);

//        for (int i = 0; i < toDraw.size(); i++) {
//            Vector polyToCam = camera.pos.clone().sub(toDraw.get(i).avgPos);
//            boolean lookingAtVisibleFace = polyToCam.dot(toDraw.get(i).normal) > 0;
////            boolean lookingAtVisibleFace = true; //set true for now to just test rasterization
////            boolean polygonInfrontOfCamera = camera.bs[2].dot(polyToCam) > 0;
//            boolean polygonInfrontOfCamera = camera.bs[2].dot(camera.pos.clone().sub(toDraw.get(i).vertices[0])) > 0 &&
//                    camera.bs[2].dot(camera.pos.clone().sub(toDraw.get(i).vertices[1])) > 0 &&
//                    camera.bs[2].dot(camera.pos.clone().sub(toDraw.get(i).vertices[2])) > 0;
//            if (lookingAtVisibleFace && polygonInfrontOfCamera) {
//                double minZ = Math.max(Math.max(toDraw.get(i).drawZ[0], toDraw.get(i).drawZ[1]), toDraw.get(i).drawZ[2]);
//                for (int j = i + 1; j < toDraw.size(); j++) {
////                    Polygon temp = toDraw.get(i).drawablePoly;
////                    Polygon temp = new Polygon(toDraw.get(i).drawablePoly.xpoints, toDraw.get(i).drawablePoly.ypoints, 3);
//                    Polygon temp = new Polygon(new int[]{toDraw.get(i).drawVertices[0].x, toDraw.get(i).drawVertices[1].x, toDraw.get(i).drawVertices[2].x},
//                            new int[]{toDraw.get(i).drawVertices[0].y, toDraw.get(i).drawVertices[1].y, toDraw.get(i).drawVertices[2].y}, 3);
//                    Point p1 = new Point(toDraw.get(j).drawVertices[0].x, toDraw.get(j).drawVertices[0].y);
//                    Point p2 = new Point(toDraw.get(j).drawVertices[1].x, toDraw.get(j).drawVertices[1].y);
//                    Point p3 = new Point(toDraw.get(j).drawVertices[2].x, toDraw.get(j).drawVertices[2].y);
//                    boolean depthCheck = toDraw.get(j).drawZ[0] > minZ && toDraw.get(j).drawZ[1] > minZ && toDraw.get(j).drawZ[2] > minZ;
//                    if (temp.contains(p1) && temp.contains(p2) && temp.contains(p3) && depthCheck) {
//                        toDraw.remove(j);
//                        j--;
//                    }
//                }
//            } else {
//                toDraw.remove(i);
//                i--;
//            }
//        }
    }

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) createBufferStrategy(2);
        else {
            Graphics g = bs.getDrawGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Main.WindowSize.width, Main.WindowSize.height);

            //Do Render (Just draw image produced
            g.drawImage(createImage(), 0, 0, this);

            g.dispose();
            bs.show();
        }
    }
    private Image createImage() {
        BufferedImage buffer = new BufferedImage(Main.WindowSize.width, Main.WindowSize.height, BufferedImage.TYPE_INT_ARGB);

        //Z Buffer rasterization mode (ALLOWS FOR CLIPPING POLYGONS)
        if (drawMode)
            buffer = customGraphics.rasterize(buffer, camera, toDraw);

        Graphics2D g = buffer.createGraphics();
        g.scale(1.0, -1.0);
        g.translate(Main.WindowSize.width / 2.0, -Main.WindowSize.height / 2.0);
        //ADD ANTIALIASING RENDERING HINT
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //Draw Mode without zBuffer thus no clipping (BUT A LOT FASTER)
        if(!drawMode) {
            for (int i = 0; i < toDraw.size(); i++) {
                CPolygon temp = toDraw.get(toDraw.size() - i - 1);
                g.setColor(temp.color[0]);
                g.fillPolygon(new Polygon(new int[]{(int)temp.drawVertices[0].x, (int)temp.drawVertices[1].x, (int)temp.drawVertices[2].x},
                        new int[]{(int)temp.drawVertices[0].y, (int)temp.drawVertices[1].y, (int)temp.drawVertices[2].y}, 3));
            }
        }

        if (drawCorners) {
            g.setColor(Color.WHITE);
            points.forEach(point -> {
                Vector tempV = point.clone().RightMatrixMult(camera.tMat);
                g.drawOval((int)(tempV.x - 2), (int)(tempV.y - 2), 4, 4);
            });
        }

        g.translate(-Main.WindowSize.width / 2.0, Main.WindowSize.height / 2.0);
        g.scale(1.0, -1.0);
        //Draw FPS
        g.setColor(Color.WHITE);
        g.drawString("Current FPS: " + drawFPS, 20, 20);
        String modeString = (drawMode) ? "Z-Buffer" : "No Z-Buffer";
        g.drawString("Current draw mode: " + modeString, 20, 50);

        g.dispose();
        return buffer;
    }

    private void MouseMovement(double dx, double dy) {
        double rotationScale = 0.001;
        camera.rotate(-dx * rotationScale, -dy * rotationScale);
    }

    private void CenterMouse() {
        try {
            rob = new Robot();
            rob.mouseMove(getLocationOnScreen().x + (int)Main.WindowSize.getWidth()/2,
                    getLocationOnScreen().y + (int)Main.WindowSize.getHeight()/2);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void mouseDragged(MouseEvent event) {
        MouseMovement(event.getX() - Main.WindowSize.getWidth()/2, -(event.getY() - Main.WindowSize.getHeight()/2));
        CenterMouse();
    }
    boolean ensureMouseInWindow = false;
    public void mouseMoved(MouseEvent event) {
        if (ensureMouseInWindow)
            MouseMovement(event.getX() - Main.WindowSize.getWidth()/2, (event.getY() - Main.WindowSize.getHeight()/2));
        else ensureMouseInWindow = true;
        CenterMouse();
    }
    public void mousePressed(MouseEvent e) {}

    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                cMoveArr[0] = 1;
                break;
            case KeyEvent.VK_S:
                cMoveArr[1] = 1;
                break;
            case KeyEvent.VK_A:
                cMoveArr[2] = 1;
                break;
            case KeyEvent.VK_D:
                cMoveArr[3] = 1;
                break;
            case KeyEvent.VK_SPACE:
                cMoveArr[4] = 1;
                break;
            case KeyEvent.VK_SHIFT:
                cMoveArr[5] = 1;
                break;
            case KeyEvent.VK_O:
                camera.updateFOV(camera.fov - 1);
                break;
            case KeyEvent.VK_P:
                camera.updateFOV(camera.fov + 1);
                break;
        }}
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_X:
                System.exit(1);
                break;
            case KeyEvent.VK_W:
                cMoveArr[0] = 0;
                break;
            case KeyEvent.VK_S:
                cMoveArr[1] = 0;
                break;
            case KeyEvent.VK_A:
                cMoveArr[2] = 0;
                break;
            case KeyEvent.VK_D:
                cMoveArr[3] = 0;
                break;
            case KeyEvent.VK_SPACE:
                cMoveArr[4] = 0;
                break;
            case KeyEvent.VK_SHIFT:
                cMoveArr[5] = 0;
                break;
            case KeyEvent.VK_E:
                if (drawCorners) drawCorners = false;
                else drawCorners = true;
                break;
            case KeyEvent.VK_M:
                if (drawMode) drawMode = false;
                else drawMode = true;
                break;
        }
    }
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

class SortByDistance implements Comparator<CPolygon> {
    private Camera c;
    public SortByDistance(Camera cam) {
        c = cam;
    }
    public int compare(CPolygon a, CPolygon b) {
//        int comp = -1 * Double.compare(a.avgPos.distance(c.pos), b.avgPos.distance(c.pos));
        int comp = Double.compare(a.avgPos.distance(c.pos), b.avgPos.distance(c.pos));
        return comp;
    }
}