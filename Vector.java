/**
 * Created by Dan on 1/31/2016.
 */
public class Vector {

    //http://www.cs.brandeis.edu/~cs155/Lecture_07_6.pdf
    //http://web.cs.iastate.edu/~cs577/handouts/homogeneous-transform.pdf

    public double x, y, z;

    public Vector() {
        x = 0;
        y = 0;
        z = 0;
    }

    //Component form
    public Vector(double cx, double cy, double cz) {
        x = cx;
        y = cy;
        z = cz;
    }

    public Vector(Vector v) {
        this(v.x, v.y, v.z);
    }

    //Polar form
    public static Vector fromPolar(double mag, double theta, double phi) {
        return new Vector(mag * Math.cos(theta) * Math.sin(phi), mag * Math.sin(theta) * Math.sin(phi), mag * Math.cos(phi));
    }

    public Vector add(Vector v) {
        x += v.x;
        y += v.y;
        z += v.z;
        return this;
    }

    public Vector sub(Vector v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        return this;
    }

    public Vector scale(double s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public double dot(Vector v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector cross(Vector v) {
        return new Vector(y * v.z - v.y * z, v.x * z - x * v.z, x * v.y - v.x * y);
    }

    public double norm() {
        return Math.sqrt(x * x + y * y + z * z);
    }
    public double squareNorm() {
        return x * x + y * y + z * z;
    }

    public double distance(Vector v) {
        return clone().sub(v).norm();
    }

    public Vector projectOnto(Vector v) {
        return v.clone().scale(dot(v) / v.squareNorm());
    }

    //Convention is to have camera centered at origin and have it facing down negative z axis
    //Coordinate System (+x right), (+y up), (+z out of screen)
    public double getTheta() {
        double theta = Math.atan2(x, z);
        return (theta >= 0) ? theta : 2.0 * Math.PI + theta;
    }
    public double getPhi() {
        return Math.atan2(Math.sqrt(x * x + z * z), y);
    }

    public Vector setMagnitude(double m) {
        return fromPolar(m, getTheta(), getPhi());
    }

    public Vector normalize() {
        double n = norm();
        if (norm() != 0)
            scale(1.0 / norm());
        return this;
    }

    //Rotate an angle theta around unit vector v
    public Vector rotate(double theta, Vector v) {
        Quaternion p = new Quaternion(0, this);
        Quaternion q = new Quaternion(Math.cos(0.5 * theta), v.clone().normalize().scale(Math.sin(0.5 * theta)));
        p.set(q.clone().mult(p));
        q.inverse();
        set(p.mult(q).vectorPart());
        return this;
    }

    //4X4 Matrix multiplied by a 3D Vector
    //Quaternions oriented HORIZONTALLY
    public Vector matrixMultWithQuat(Quaternion[] m) {
        if (m == null) {
            x = Double.MAX_VALUE;
            y = Double.MAX_VALUE;
            z = Double.MAX_VALUE;
            return this;
        }
        double q, r, s, w;
        q = m[0].a * x + m[0].b * y + m[0].c * z + m[0].d;
        r = m[1].a * x + m[1].b * y + m[1].c * z + m[1].d;
        s = m[2].a * x + m[2].b * y + m[2].c * z + m[2].d;
        w = m[3].a * x + m[3].b * y + m[3].c * z + m[3].d;
        x = q;
        y = r;
        z = s;
        if(w > 0 || w < 0)
            scale(1.0 / w);
        return this;
    }

//    public Vector LeftMatrixMult(double[][] TM) {
//        double q, r, s, w;
//        q = x * TM[0][0] + y * TM[1][0] + z * TM[2][0] + TM[3][0];
//        r = x * TM[0][1] + y * TM[1][1] + z * TM[2][1] + TM[3][1];
//        s = x * TM[0][2] + y * TM[1][2] + z * TM[2][2] + TM[3][2];
//        w = x * TM[0][3] + y * TM[1][3] + z * TM[2][3] + TM[3][3];
//        if (w != 1 && w != 0) {
//            q /= w;
//            r /= w;
//            s /= w;
//        }
//        x = q;
//        y = r;
//        z = s;
//        return this;
//    }
    public Vector RightMatrixMult(double[] TM) {
        double q, r, s, w;
        q = x * TM[0] + y * TM[1] + z * TM[2] + TM[3];
        r = x * TM[4] + y * TM[5] + z * TM[6] + TM[7];
        s = x * TM[8] + y * TM[9] + z * TM[10] + TM[11];
        w = x * TM[12] + y * TM[13] + z * TM[14] + TM[15];
        if (w != 1 && w != 0) {
            q /= w;
            r /= w;
            s /= w;
        }
        x = q;
        y = r;
        z = s;
        return this;
    }

    public static double[] MatrixProduct(double[] TM, double[] PM) {
        double[] combined = new double[16];
        combined[0] = TM[0] * PM[0] + TM[4] * PM[1] + TM[8] * PM[2] + TM[12] * PM[3];
        combined[1] = TM[1] * PM[0] + TM[5] * PM[1] + TM[9] * PM[2] + TM[13] * PM[3];
        combined[2] = TM[2] * PM[0] + TM[6] * PM[1] + TM[10] * PM[2] + TM[14] * PM[3];
        combined[3] = TM[3] * PM[0] + TM[7] * PM[1] + TM[11] * PM[2] + TM[15] * PM[3];

        combined[4] = TM[0] * PM[4] + TM[4] * PM[5] + TM[8] * PM[6] + TM[12] * PM[7];
        combined[5] = TM[1] * PM[4] + TM[5] * PM[5] + TM[9] * PM[6] + TM[13] * PM[7];
        combined[6] = TM[2] * PM[4] + TM[6] * PM[5] + TM[10] * PM[6] + TM[14] * PM[7];
        combined[7] = TM[3] * PM[4] + TM[7] * PM[5] + TM[11] * PM[6] + TM[15] * PM[7];

        combined[8]  = TM[0] * PM[8] + TM[4] * PM[9] + TM[8] * PM[10] + TM[12] * PM[11];
        combined[9]  = TM[1] * PM[8] + TM[5] * PM[9] + TM[9] * PM[10] + TM[13] * PM[11];
        combined[10] = TM[2] * PM[8] + TM[6] * PM[9] + TM[10] * PM[10] + TM[14] * PM[11];
        combined[11] = TM[3] * PM[8] + TM[7] * PM[9] + TM[11] * PM[10] + TM[15] * PM[11];

        combined[12] = TM[0] * PM[12] + TM[4] * PM[13] + TM[8] * PM[14] + TM[12] * PM[15];
        combined[13] = TM[1] * PM[12] + TM[5] * PM[13] + TM[9] * PM[14] + TM[13] * PM[15];
        combined[14] = TM[2] * PM[12] + TM[6] * PM[13] + TM[10] * PM[14] + TM[14] * PM[15];
        combined[15] = TM[3] * PM[12] + TM[7] * PM[13] + TM[11] * PM[14] + TM[15] * PM[15];
        return combined;
    }

    public static double[] InvertMatrix(double[] m) {
        double[] inv = new double[16];
        double det;

        inv[0] = m[5]  * m[10] * m[15] -
                m[5]  * m[11] * m[14] -
                m[9]  * m[6]  * m[15] +
                m[9]  * m[7]  * m[14] +
                m[13] * m[6]  * m[11] -
                m[13] * m[7]  * m[10];

        inv[4] = -m[4]  * m[10] * m[15] +
                m[4]  * m[11] * m[14] +
                m[8]  * m[6]  * m[15] -
                m[8]  * m[7]  * m[14] -
                m[12] * m[6]  * m[11] +
                m[12] * m[7]  * m[10];

        inv[8] = m[4]  * m[9] * m[15] -
                m[4]  * m[11] * m[13] -
                m[8]  * m[5] * m[15] +
                m[8]  * m[7] * m[13] +
                m[12] * m[5] * m[11] -
                m[12] * m[7] * m[9];

        inv[12] = -m[4]  * m[9] * m[14] +
                m[4]  * m[10] * m[13] +
                m[8]  * m[5] * m[14] -
                m[8]  * m[6] * m[13] -
                m[12] * m[5] * m[10] +
                m[12] * m[6] * m[9];

        inv[1] = -m[1]  * m[10] * m[15] +
                m[1]  * m[11] * m[14] +
                m[9]  * m[2] * m[15] -
                m[9]  * m[3] * m[14] -
                m[13] * m[2] * m[11] +
                m[13] * m[3] * m[10];

        inv[5] = m[0]  * m[10] * m[15] -
                m[0]  * m[11] * m[14] -
                m[8]  * m[2] * m[15] +
                m[8]  * m[3] * m[14] +
                m[12] * m[2] * m[11] -
                m[12] * m[3] * m[10];

        inv[9] = -m[0]  * m[9] * m[15] +
                m[0]  * m[11] * m[13] +
                m[8]  * m[1] * m[15] -
                m[8]  * m[3] * m[13] -
                m[12] * m[1] * m[11] +
                m[12] * m[3] * m[9];

        inv[13] = m[0]  * m[9] * m[14] -
                m[0]  * m[10] * m[13] -
                m[8]  * m[1] * m[14] +
                m[8]  * m[2] * m[13] +
                m[12] * m[1] * m[10] -
                m[12] * m[2] * m[9];

        inv[2] = m[1]  * m[6] * m[15] -
                m[1]  * m[7] * m[14] -
                m[5]  * m[2] * m[15] +
                m[5]  * m[3] * m[14] +
                m[13] * m[2] * m[7] -
                m[13] * m[3] * m[6];

        inv[6] = -m[0]  * m[6] * m[15] +
                m[0]  * m[7] * m[14] +
                m[4]  * m[2] * m[15] -
                m[4]  * m[3] * m[14] -
                m[12] * m[2] * m[7] +
                m[12] * m[3] * m[6];

        inv[10] = m[0]  * m[5] * m[15] -
                m[0]  * m[7] * m[13] -
                m[4]  * m[1] * m[15] +
                m[4]  * m[3] * m[13] +
                m[12] * m[1] * m[7] -
                m[12] * m[3] * m[5];

        inv[14] = -m[0]  * m[5] * m[14] +
                m[0]  * m[6] * m[13] +
                m[4]  * m[1] * m[14] -
                m[4]  * m[2] * m[13] -
                m[12] * m[1] * m[6] +
                m[12] * m[2] * m[5];

        inv[3] = -m[1] * m[6] * m[11] +
                m[1] * m[7] * m[10] +
                m[5] * m[2] * m[11] -
                m[5] * m[3] * m[10] -
                m[9] * m[2] * m[7] +
                m[9] * m[3] * m[6];

        inv[7] = m[0] * m[6] * m[11] -
                m[0] * m[7] * m[10] -
                m[4] * m[2] * m[11] +
                m[4] * m[3] * m[10] +
                m[8] * m[2] * m[7] -
                m[8] * m[3] * m[6];

        inv[11] = -m[0] * m[5] * m[11] +
                m[0] * m[7] * m[9] +
                m[4] * m[1] * m[11] -
                m[4] * m[3] * m[9] -
                m[8] * m[1] * m[7] +
                m[8] * m[3] * m[5];

        inv[15] = m[0] * m[5] * m[10] -
                m[0] * m[6] * m[9] -
                m[4] * m[1] * m[10] +
                m[4] * m[2] * m[9] +
                m[8] * m[1] * m[6] -
                m[8] * m[2] * m[5];

        det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];

        if (det == 0)
            return null;

        det = 1.0 / det;

        for (int i = 0; i < 16; i++)
            inv[i] *= det;

        return inv;
    }

    public Vector set(Vector v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return this;
    }
    public Vector set(double x_, double y_, double z_) {
        x = x_;
        y = y_;
        z = z_;
        return this;
    }

    public boolean isEqual(Vector v) {
        return x == v.x && y == v.y && z == v.z;
    }
    public Vector clone() {
        return new Vector(x, y, z);
    }
    @Override
    public String toString() {
        return String.format("[%.3f,%.3f,%.3f]", x, y, z);
    }
}
