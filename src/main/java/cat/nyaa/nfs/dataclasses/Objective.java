package cat.nyaa.nfs.dataclasses;

import java.util.List;
import java.util.UUID;

public class Objective {
    String name;
    UUID uniqueID;
    boolean enabled;
    List<CheckRange> checkRanges;

    public Objective(UUID uniqueID, String name, List<CheckRange> checkRanges) {
        this.name = name;
        this.uniqueID = uniqueID;
        enabled = true;
        this.checkRanges = checkRanges;
    }

    public Objective(UUID uniqueID, String name, boolean enabled, List<CheckRange> checkRanges) {
        this.name = name;
        this.uniqueID = uniqueID;
        this.enabled = enabled;
        this.checkRanges = checkRanges;
    }

    public Objective() {
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public String getName() {
        return name;
    }

    public List<CheckRange> getCheckAreas() {
        return checkRanges;
    }

    public void addCheck(int index, CheckRange checkRange) {
        checkRanges.add(index, checkRange);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addCheck(CheckRange checkRange) {
        checkRanges.add(checkRange);
    }

    public boolean removeLastCheck() {
        if (checkRanges.isEmpty())
            return false;
        return checkRanges.removeLast() != null;
    }

    public boolean removeCheck(int index) {
        return checkRanges.remove(index) != null;
    }

    public CheckRange getCheck(int index) {
        return checkRanges.get(index);
    }

    public void replaceCheck(int index, CheckRange checkRange) {
        checkRanges.set(index, checkRange);
    }

    public int getSize() {
        return checkRanges.size();
    }
}
