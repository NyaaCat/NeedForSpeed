package cat.nyaa.nfs.dataclasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerRecords {
    Map<UUID, List<Long>> recordMap = new HashMap<>();

    public PlayerRecords() {
    }

    public PlayerRecords(Map<UUID, List<Long>> recordMap) {
        this.recordMap = recordMap;
    }

    public List<Long> getRecord(UUID timerUniqueID) {
        return recordMap.get(timerUniqueID);
    }

    public boolean pushRecord(UUID timerUniqueID, List<Long> timestamps) {
        if (recordMap.containsKey(timerUniqueID)) {
            var record = recordMap.get(timerUniqueID);
            var timeUsedBefore = record.get(record.size() - 1) - record.get(0);
            var timeUsedNow = timestamps.get(timestamps.size() - 1) - timestamps.get(0);
            if (timeUsedNow < timeUsedBefore) {
                recordMap.put(timerUniqueID, timestamps);
                return true;
            } else {
                return false;
            }
        } else {
            recordMap.put(timerUniqueID, timestamps);
            return true;
        }
    }
}
