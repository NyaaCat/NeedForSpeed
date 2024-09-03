package cat.nyaa.nfs.dataclasses;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimerRecords {
    Map<UUID, Long> records = new HashMap<>();

    public TimerRecords() {
    }

    public TimerRecords(Map<UUID, Long> records) {
        this.records = records;
    }

    public Map<UUID, Long> getRecords(){
        return Collections.unmodifiableMap(records);
    }

    public void update(UUID playerUniqueID, Long time) {
        records.put(playerUniqueID, time);
    }
}
