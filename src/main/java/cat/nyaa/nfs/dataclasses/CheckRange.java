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
        var start = from.toVector();
        var end = to.toVector();
        var delta = end.clone().subtract(start);
        double distance = delta.length();
        var dir = delta.clone().normalize();

        if (distance < 1e-8) {
            return boundingBox.contains(start);
        }

        var result = boundingBox.rayTrace(start, dir, distance);
        return result != null;
    }

    private boolean isInside(Point point) {
        return point.x <= (max.x + 1) && point.x >= (min.x - 1) &&
                point.y <= (max.y + 1) && point.y >= (min.y - 1) &&
                point.z <= (max.z + 1) && point.z >= (min.z - 1);
    }
}
