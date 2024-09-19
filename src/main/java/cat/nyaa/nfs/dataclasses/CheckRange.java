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

    public boolean isRelevant(Location from, Location to) {
        // Step 1: Ensure both locations are in the same world as the range
        if (!from.getWorld().getName().equals(world) || !to.getWorld().getName().equals(world)) {
            return false;
        }
        // Step 2: Check if either `from` or `to` is inside the box
        if (isInside(from) || isInside(to)) {
            return true;
        }
        // Step 3: Check if the line segment intersects the box
        Point fromPoint = new Point(from.getX(), from.getY(), from.getZ());
        Point toPoint = new Point(to.getX(), to.getY(), to.getZ());

        return intersectsBoundingBox(fromPoint, toPoint);
    }

    private boolean isInside(Location loc) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= b.x && x <= a.x &&
                y >= b.y && y <= a.y &&
                z >= b.z && z <= a.z;
    }

    private boolean intersectsBoundingBox(Point from, Point to) {
        // Use the "slab method" to check if the line intersects the AABB
        double tmin = (b.x - from.x) / (to.x - from.x);
        double tmax = (a.x - from.x) / (to.x - from.x);
        if (tmin > tmax) {
            double temp = tmin;
            tmin = tmax;
            tmax = temp;
        }

        double tymin = (b.y - from.y) / (to.y - from.y);
        double tymax = (a.y - from.y) / (to.y - from.y);
        if (tymin > tymax) {
            double temp = tymin;
            tymin = tymax;
            tymax = temp;
        }

        if ((tmin > tymax) || (tymin > tmax)) {
            return false;
        }

        if (tymin > tmin) {
            tmin = tymin;
        }

        if (tymax < tmax) {
            tmax = tymax;
        }

        double tzmin = (b.z - from.z) / (to.z - from.z);
        double tzmax = (a.z - from.z) / (to.z - from.z);
        if (tzmin > tzmax) {
            double temp = tzmin;
            tzmin = tzmax;
            tzmax = temp;
        }

        if ((tmin > tzmax) || (tzmin > tmax)) {
            return false;
        }

        return true;
    }
}

