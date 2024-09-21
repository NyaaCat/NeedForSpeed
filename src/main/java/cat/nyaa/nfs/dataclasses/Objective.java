package cat.nyaa.nfs.dataclasses;

import java.util.List;
import java.util.UUID;

public class Objective {
    private String name;
    private UUID uniqueID;
    private boolean enabled;
    private boolean clearOnDeath;
    private boolean clearOnQuit;
    private boolean firstRangeCountsCheckNumber;
    private List<CheckRange> checkRanges;

    public Objective(UUID uniqueID, String name, List<CheckRange> checkRanges) {
        this.name = name;
        this.uniqueID = uniqueID;
        enabled = true;
        clearOnDeath = true;
        clearOnQuit = false;
        firstRangeCountsCheckNumber = true;
        this.checkRanges = checkRanges;
    }

    public Objective(UUID uniqueID, String name, boolean enabled, boolean clearOnDeath, boolean clearOnQuit, boolean firstRangeCountsCheckNumber, List<CheckRange> checkRanges) {
        this.name = name;
        this.uniqueID = uniqueID;
        this.enabled = enabled;
        this.clearOnDeath = clearOnDeath;
        this.clearOnQuit = clearOnQuit;
        this.firstRangeCountsCheckNumber = firstRangeCountsCheckNumber;
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

    public List<CheckRange> getCheckRanges() {
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

    public boolean isClearOnQuit() {
        return clearOnQuit;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setClearOnDeath(boolean clearOnDeath) {
        this.clearOnDeath = clearOnDeath;
    }

    public void setClearOnQuit(boolean clearOnQuit) {
        this.clearOnQuit = clearOnQuit;
    }

    public void setFirstRangeCountsCheckNumber(boolean firstRangeCountsCheckNumber) {
        this.firstRangeCountsCheckNumber = firstRangeCountsCheckNumber;
    }

    public void setCheckRanges(List<CheckRange> checkRanges) {
        this.checkRanges = checkRanges;
    }

    public boolean isFirstRangeCountsCheckNumber() {
        return firstRangeCountsCheckNumber;
    }

    public boolean isClearOnDeath() {
        return clearOnDeath;
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
