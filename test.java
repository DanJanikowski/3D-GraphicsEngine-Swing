import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class test {
    public static void main(String[] args) {
        Point[] c = new Point[3];
//        c[0] = new Point((int)(Math.random() * 10.0), (int)(Math.random() * 10.0));
//        c[1] = new Point((int)(Math.random() * 10.0), (int)(Math.random() * 10.0));
//        c[2] = new Point((int)(Math.random() * 10.0), (int)(Math.random() * 10.0));
        c[0] = new Point(5, 2);
        c[1] = new Point(2, 1);
        c[2] = new Point(3, 2);
        for (Point p : c) System.out.println(p);
        System.out.println();

//        Arrays.sort(c, new SortByY());
        for (Point p : c) System.out.println(p);
    }
}
