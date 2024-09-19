package cat.nyaa.nfs.save;

import java.util.UUID;

public record PlayerRecord(int id, long createdTime, UUID objectiveUUID,
                           UUID playerUUID, long timeInMillisecond,
                           String recordDetail) {
    public PlayerRecord {
        if (id < 0) {
            throw new IllegalArgumentException("id must be non-negative");
        }
        if (createdTime < 0) {
            throw new IllegalArgumentException("createdTime must be non-negative");
        }
        if (timeInMillisecond < 0) {
            throw new IllegalArgumentException("timeInMillisecond must be non-negative");
        }
    }
}
