package cat.nyaa.nfs.dataclasses;

import org.bukkit.Location;

public class CheckRange {
    String world;
    Point a;
    Point b;

    public CheckRange() {
    }

    public CheckRange(String world, Point a, Point b) {
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

    public void setA(Point a) {
        this.a = a;
    }

    public void setB(Point b) {
        this.b = b;
    }

    public boolean isRelevant(Location from, Location to) {
        // Step 1: Ensure both locations are in the same world as the range
        if (!from.getWorld().getName().equals(world) || !to.getWorld().getName().equals(world)) {
            return false;
        }

        Point fromPoint = new Point(from.getX(), from.getY(), from.getZ());
        Point toPoint = new Point(to.getX(), to.getY(), to.getZ());

        // Step 2: Check if either `from` or `to` is inside the box
        if (isInside(fromPoint) || isInside(toPoint)) {
            return true;
        }

        // Step 3: Check if the line segment intersects the box
//        return intersectsBoundingBox(fromPoint, toPoint);
        return false;
    }

    private boolean isInside(Point point) {
        return point.x <= (a.x + 1) && point.x >= (b.x - 1) &&
                point.y <= (a.y + 1) && point.y >= (b.y - 1) &&
                point.z <= (a.z + 1) && point.z >= (b.z - 1);
    }

    private boolean intersectsBoundingBox(Point from, Point to) {
        return false;
    }
}

