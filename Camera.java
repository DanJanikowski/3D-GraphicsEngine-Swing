public class Camera {

    public double fov = 90, S = 1.0 / Math.tan(fov * Math.PI / 360.0);
    //clipping planes
    private final double near = 0.1, far = 50, f1 = -far / (far - near), f2 = -(1.0 * near) / (far - near);


    public Vector pos;
    //Basis vectors
    public Vector[] bs;
    public double[] tMat;

    public Camera(Vector p) {
        pos = p;
        bs = new Vector[3];
        //Convention is to have camera centered at origin and have it facing down negative z axis
        //However apparently camera lens faces opposite of forward vector thus...
        bs[0] = new Vector(1, 0, 0); //Right
        bs[1] = new Vector(0, 1, 0); //Up
        bs[2] = new Vector(0, 0, 1); //Forward

        tMat = new double[16];
        updateCombinedMatrix();
    }

    public void updateFOV(double newFov) {
        fov = newFov;
        S = 1.0 / Math.tan(fov * Math.PI / 360.0);
//        System.out.println(fov);
    }

    public double[] combinedInverseMatrix() {
        double[] inverseTMat = new double[16];
        inverseTMat[0] =  bs[0].x * S;
        inverseTMat[1] =  bs[1].x * S;
        inverseTMat[2] =  bs[2].x * S;
        inverseTMat[3] =  pos.x * S;
        inverseTMat[4] =  bs[0].y * S;
        inverseTMat[5] =  bs[1].y * S;
        inverseTMat[6] =  bs[2].y * S;
        inverseTMat[7] =  pos.y * S;
        inverseTMat[8] =  bs[0].z * f1;
        inverseTMat[9] =  bs[1].z * f1;
        inverseTMat[10] = bs[2].z * f1;
        inverseTMat[11] = pos.z * f1 - 1;
        inverseTMat[12] = bs[0].z * f2;
        inverseTMat[13] = bs[1].z * f2;
        inverseTMat[14] = bs[2].z * f2;
        inverseTMat[15] = pos.z * f2;
        return inverseTMat;
    }

    //Generate combined transformation/perspective matrix
    public void updateCombinedMatrix() {
        double[] tmi = new double[9];
        double d1 = bs[1].y*bs[2].z-bs[2].y*bs[1].z,
                d2 = bs[2].y*bs[0].z-bs[0].y*bs[2].z,
                d3 = bs[0].y*bs[1].z-bs[1].y*bs[0].z,
                det = bs[0].x*d1+bs[1].x*d2+bs[2].x*d3;
        det = 1.0/det;
        tmi[0] = d1 * det;
        tmi[1] = (bs[2].x*bs[1].z-bs[1].x*bs[2].z) * det;
        tmi[2] = (bs[1].x*bs[2].y-bs[2].x*bs[1].y) * det;
        tmi[3] = d2 * det;
        tmi[4] = (bs[0].x*bs[2].z-bs[2].x*bs[0].z) * det;
        tmi[5] = (bs[2].x*bs[0].y-bs[0].x*bs[2].y) * det;
        tmi[6] = d3 * det;
        tmi[7] = (bs[0].z*bs[1].x-bs[0].x*bs[1].z) * det;
        tmi[8] = (bs[0].x*bs[1].y-bs[0].y*bs[1].x) * det;
        double[] tp = new double[3];
        tp[0] = pos.x * -tmi[0] + pos.y * -tmi[1] + pos.z * -tmi[2];
        tp[1] = pos.x * -tmi[3] + pos.y * -tmi[4] + pos.z * -tmi[5];
        tp[2] = pos.x * -tmi[6] + pos.y * -tmi[7] + pos.z * -tmi[8];
        tMat[0] =  tmi[0] * S;
        tMat[1] =  tmi[1] * S;
        tMat[2] =  tmi[2] * S;
        tMat[3] =  tp[0] * S;
        tMat[4] =  tmi[3] * S;
        tMat[5] =  tmi[4] * S;
        tMat[6] =  tmi[5] * S;
        tMat[7] =  tp[1] * S;
        tMat[8] =  tmi[6] * f1;
        tMat[9] =  tmi[7] * f1;
        tMat[10] = tmi[8] * f1;
        tMat[11] = tp[2] * f1 - 1;
        tMat[12] = tmi[6] * f2;
        tMat[13] = tmi[7] * f2;
        tMat[14] = tmi[8] * f2;
        tMat[15] = tp[2] * f2;
    }

    public void rotate(double dx, double dy) {
        //Rotate all vectors first around vertical axis
        for (int i = 0; i < 3; i++) bs[i].rotate(dx, new Vector(0, 1, 0));
        //Now for pitch, first check if we aren't at a vertical angle
        Vector newZ = bs[2].clone().rotate(dy, bs[0]);
        double phiDiff = bs[2].getPhi() - newZ.getPhi();
        if ((dy < 0 && phiDiff > 0) || (dy > 0 && phiDiff < 0)) {
            bs[2].set(newZ);
            bs[1].rotate(dy, bs[0]);
        }
        for (int i = 0; i < 3; i++) bs[i].normalize();
    }

    public void moveCamera(byte[] cameraMoveDirection) {
        double moveScale = 2.0;
        if (cameraMoveDirection[0] == 1)//Forward
            pos.sub(bs[2].clone().scale(moveScale));
        if (cameraMoveDirection[1] == 1)//Backward
            pos.add(bs[2].clone().scale(moveScale));
        if (cameraMoveDirection[2] == 1)//Left
            pos.sub(bs[0].clone().scale(moveScale));
        if(cameraMoveDirection[3] == 1)//Right
            pos.add(bs[0].clone().scale(moveScale));
        if(cameraMoveDirection[4] == 1)//Up
            pos.add(new Vector(0, moveScale, 0));
        if(cameraMoveDirection[5] == 1)//Down
            pos.add(new Vector(0, -moveScale, 0));
    }

    public String toString() {
        return bs[0] + " " + bs[1] + " " + bs[2];
    }
}
