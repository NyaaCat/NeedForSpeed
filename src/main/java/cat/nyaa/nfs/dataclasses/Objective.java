package cat.nyaa.nfs.dataclasses;

import java.util.List;
import java.util.UUID;

public class Objective {
    String name;
    UUID uniqueID;
    boolean enabled;
    List<CheckArea> checkAreas;

    public Objective(UUID uniqueID, String name, List<CheckArea> checkAreas) {
        this.name = name;
        this.uniqueID = uniqueID;
        enabled = true;
        this.checkAreas = checkAreas;
    }

    public Objective(UUID uniqueID, String name, boolean enabled, List<CheckArea> checkAreas) {
        this.name = name;
        this.uniqueID = uniqueID;
        this.enabled = enabled;
        this.checkAreas = checkAreas;
    }

    public Objective() {
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public String getName() {
        return name;
    }

    public List<CheckArea> getCheckAreas() {
        return checkAreas;
    }

    public void addCheckArea(int index, CheckArea checkArea) {
        checkAreas.add(index, checkArea);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addCheckArea(CheckArea checkArea) {
        checkAreas.add(checkArea);
    }

    public boolean removeLastArea() {
        if (checkAreas.isEmpty())
            return false;
        return checkAreas.remove(checkAreas.size() - 1) != null;
    }

    public boolean removeCheckArea(int index) {
        return checkAreas.remove(index) != null;
    }

    public CheckArea getCheckArea(int index) {
        return checkAreas.get(index);
    }

    public void replaceCheckArea(int index, CheckArea checkArea) {
        checkAreas.set(index, checkArea);
    }

    public int getSize() {
        return checkAreas.size();
    }
}
