/**
 * Created by Dan on 8/6/2018.
 */
public class Quaternion {

    //  X  i  j  k
    //  i -1  k -j
    //  j -k -1  i
    //  k  j -i -1

    //  ii = jj = kk = ijk = -1

    public double a, b, c, d;

    public Quaternion(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Quaternion(Quaternion q) {
        this(q.a, q.b, q.c, q.d);
    }

    //Used for rotation where s is the given rotation angle
    public Quaternion(double s, Vector v) {
        a = s;
        b = v.x;
        c = v.y;
        d = v.z;
    }

    //Used to make 4d vecor for matrix multiplication
    public Quaternion(Vector v, double s) {
        a = v.x;
        b = v.y;
        c = v.z;
        d = s;
    }

    public Quaternion add(Quaternion q) {
        a += q.a;
        b += q.b;
        c += q.c;
        d += q.d;
        return this;
    }

    public Quaternion sub(Quaternion q) {
        a -= q.a;
        b -= q.b;
        c -= q.c;
        d -= q.d;
        return this;
    }

    public Quaternion scale(double s) {
        a *= s;
        b *= s;
        c *= s;
        d *= s;
        return this;
    }

    //Hamilton Product
    public Quaternion mult(Quaternion q) {
        double w, x, y, z;
        w = a * q.a - b * q.b - c * q.c - d * q.d;
        x = a * q.b + b * q.a + c * q.d - d * q.c;
        y = a * q.c - b * q.d + c * q.a + d * q.b;
        z = a * q.d + b * q.c - c * q.b + d * q.a;
        a = w;
        b = x;
        c = y;
        d = z;
        return this;
    }

    public Quaternion inverse() {
        scale(squareNorm());
        conjugate();
        return this;
    }
    public Quaternion div(Quaternion q) {
        mult(q.clone().inverse());
        return this;
    }

    public Quaternion conjugate() {
        b *= -1.0;
        c *= -1.0;
        d *= -1.0;
        return this;
    }
    public Vector vectorPart() {
        return new Vector(b, c, d);
    }

    public double dotProduct(Quaternion q) {
        return a * q.a + b * q.b + c * q.c + d * q.d;
    }

    public double norm() {
        return Math.sqrt(a * a + b * b + c * c + d * d);
    }
    public double squareNorm() {
        return a * a + b * b + c * c + d * d;
    }
    public double dist(Quaternion q) {
        return clone().sub(q).norm();
    }



    public Quaternion unitize() {
        scale(1.0 / norm());
        return this;
    }

    public Quaternion set(Quaternion q) {
        a = q.a;
        b = q.b;
        c = q.c;
        d = q.d;
        return this;
    }
    public Quaternion clone() {
        return new Quaternion(a, b, c, d);
    }
    @Override
    public String toString() {
        return String.format("[%.3f,%.3f,%.3f,%.3f]", a, b, c, d);
    }
}
