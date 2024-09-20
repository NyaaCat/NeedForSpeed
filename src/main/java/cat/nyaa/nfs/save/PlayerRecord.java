package cat.nyaa.nfs.save;

import java.util.List;
import java.util.UUID;

public record PlayerRecord(int id, long createdTime, UUID objectiveUUID,
                           UUID playerUUID, RecordBy source,
                           long timeInMillisecond,
                           List<Long> recordDetail) {

    public static PlayerRecord capture(UUID objectiveUniqueID, UUID playerUniqueID, RecordBy source, List<Long> progress) {
        var timeUsedInMilliseconds = switch (source) {
            case FINISHED -> progress.getLast() - progress.getFirst();
            default -> System.currentTimeMillis() - progress.getFirst();
        };
        return new PlayerRecord(
                -1,
                System.currentTimeMillis(),
                objectiveUniqueID,
                playerUniqueID,
                source,
                timeUsedInMilliseconds,
                progress
        );
    }
}
