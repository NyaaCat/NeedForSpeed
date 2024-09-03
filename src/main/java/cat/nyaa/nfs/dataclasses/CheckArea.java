package cat.nyaa.nfs.dataclasses;

public class CheckArea {
    String world;
    Point a;
    Point b;

    public CheckArea() {
    }

    public CheckArea(String world, Point a, Point b) {
        this.world = world;
        this.a = new Point(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));
        this.b = new Point(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
    }

    public String getWorld() {
        return world;
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }
}
