package cat.nyaa.nfs.dataclasses;

import com.google.gson.annotations.SerializedName;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import static java.lang.Math.*;

public class CheckRange {
    String world;
    @SerializedName("a")
    Point max;
    @SerializedName("b")
    Point min;

    public CheckRange() {
    }

    public CheckRange(String world, Point max, Point min) {
        this.world = world;
        this.max = new Point(max(max.x, min.x), max(max.y, min.y), max(max.z, min.z));
        this.min = new Point(min(max.x, min.x), min(max.y, min.y), min(max.z, min.z));
    }

    public String getWorld() {
        return world;
    }

    public Point getMax() {
        return max;
    }

    public Point getMin() {
        return min;
    }

    public void setMax(Point max) {
        this.max = max;
    }

    public void setMin(Point min) {
        this.min = min;
    }

    public Point getCenter() {
        return new Point((max.x + min.x) / 2, (max.y + min.y) / 2, (max.z + min.z) / 2);
    }

    public boolean isRelevant(Location from, Location to) {
        var boundingBox = new BoundingBox(max.x, max.y, max.z, min.x, min.y, min.z);
        var direction = to.toVector().subtract(from.toVector());
        var magnitude = direction.length();
        direction.normalize();

        
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

        return false;

        // Step 3: Check if the line segment intersects with the box
//        return intersects(fromPoint, toPoint);
    }

    private boolean isInside(Point point) {
        return point.x <= (max.x + 1) && point.x >= (min.x - 1) &&
                point.y <= (max.y + 1) && point.y >= (min.y - 1) &&
                point.z <= (max.z + 1) && point.z >= (min.z - 1);
    }

    public boolean intersects(Point rayStart, Point rayEnd) {
        // ref:
        // https://tavianator.com/2011/ray_box.html
        // https://tavianator.com/cgit/dimension.git/tree/libdimension/bvh/bvh.c#n194

        Ray ray = new Ray(rayStart, rayEnd);
        double tmax = Double.POSITIVE_INFINITY;
        double tmin = 0;

        double tx1 = (min.x - ray.start.x) * ray.direction_inv.x;
        double tx2 = (max.x - ray.start.x) * ray.direction_inv.x;
        tmin = max(tmin, min(tx1, tx2));
        tmax = min(tmax, max(tx1, tx2));
        

        double ty1 = (min.y - ray.start.y) * ray.direction_inv.y;
        double ty2 = (max.y - ray.start.y) * ray.direction_inv.y;
        tmin = max(tmin, min(ty1, ty2));
        tmax = min(tmax, max(ty1, ty2));

        double tz1 = (min.z - ray.start.z) * ray.direction_inv.z;
        double tz2 = (max.z - ray.start.z) * ray.direction_inv.z;
        tmin = max(tmin, min(tz1, tz2));
        tmax = min(tmax, max(tz1, tz2));

        return tmax >= max(tmin, ray.magnitude);
    }

    private static class Ray {
        final Point start;
        final Point end;
        final Point direction = new Point();
        final Point direction_inv = new Point();
        final double magnitude;


        Ray(Point start, Point end) {
            this.start = start;
            this.end = end;

            this.direction.x = end.x - start.x;
            this.direction.y = end.y - start.y;
            this.direction.z = end.z - start.z;

            // Compute magnitude of direction
            magnitude = sqrt(direction.x * direction.x +
                    direction.y * direction.y +
                    direction.z * direction.z);

            // Normalize direction if necessary
            direction.x /= magnitude;
            direction.y /= magnitude;
            direction.z /= magnitude;

            this.direction_inv.x = 1.0 / this.direction.x;
            this.direction_inv.y = 1.0 / this.direction.y;
            this.direction_inv.z = 1.0 / this.direction.z;
        }
    }
}

