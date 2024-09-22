package cat.nyaa.nfs.dataclasses;

import static java.lang.Math.sqrt;

public class Point {
    public double x;
    public double y;
    public double z;

    public Point() {
    }

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double[] asArray() {
        return new double[]{x, y, z};
    }

    public double distanceTo(double x, double y, double z) {
        return sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y) + (this.z - z) * (this.z - z));
    }
}
