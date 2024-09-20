package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.Objective;
import cat.nyaa.nfs.save.PlayerRecord;
import cat.nyaa.nfs.save.RecordBy;
import cat.nyaa.nfs.save.Recorder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

public class TimerListenerInstance implements Listener {
    private static final Gson compactGson = new GsonBuilder().disableHtmlEscaping().create();
    private final Objective objective;
    private final Recorder recorder;
    private final Map<UUID, PlayerRecord> playerBestCache = new HashMap<>();
    private final Map<UUID, List<Long>> playerProgress = new HashMap<>();
    private final Map<UUID, Long> resetCoolDownMap = new HashMap<>();
    private final DecimalFormat numberFormatter = new DecimalFormat("#0.00");

    public TimerListenerInstance(Objective objective, Recorder recorder) {
        this.objective = objective;
        this.recorder = recorder;
    }

    private Language getLanguage() {
        return NeedForSpeed.instance.getLanguage();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            var playerBestPlay = recorder.getBestPlayerRecord(event.getPlayer().getUniqueId(), objective.getUniqueID());
            playerBestCache.put(event.getPlayer().getUniqueId(), Objects.requireNonNullElseGet(playerBestPlay, () -> new PlayerRecord(-1, -1, objective.getUniqueID(), event.getPlayer().getUniqueId(), RecordBy.NONE, Long.MAX_VALUE, new ArrayList<>())));
        } catch (SQLException e) {
            event.getPlayer().sendMessage(getLanguage().failedToLoadPersonalBestOn.produce(Pair.of("groupName", objective.getName())));
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerBestCache.remove(event.getPlayer().getUniqueId());
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null)
            return;
        if (objective.getSize() < 3)
            return;
        if (!objective.isEnabled())
            return;
        List<Long> progress;
        var player = event.getPlayer();
        if (playerProgress.containsKey(player.getUniqueId()))
            progress = playerProgress.get(player.getUniqueId());
        else {
            progress = new ArrayList<>();
            playerProgress.put(player.getUniqueId(), progress);
        }
        if (System.currentTimeMillis() - resetCoolDownMap.getOrDefault(player.getUniqueId(), System.currentTimeMillis()) > 3000 // which is 3 seconds
                && !progress.isEmpty()
                && objective.getCheck(0).isRelevant(event.getFrom(), event.getTo())) {
            resetCoolDownMap.put(player.getUniqueId(), System.currentTimeMillis());
            var record = recordThenReset(player, RecordBy.RESTARTED);
            pushRecordAsync(record);
        }
        if (objective.getCheck(progress.size()).isRelevant(event.getFrom(), event.getTo())) {
            progress.add(System.currentTimeMillis());
            if (progress.size() == 1) {
                resetCoolDownMap.put(player.getUniqueId(), System.currentTimeMillis());
                player.sendTitle(" ", getLanguage().firstCheckAreaSubtitle.produce(Pair.of("groupName", objective.getName())), 0, 20, 5);
                player.sendMessage(getLanguage().firstCheckAreaNotice.produce(Pair.of("groupName", objective.getName())));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);
                playerProgress.put(player.getUniqueId(), progress);
            } else if (progress.size() == objective.getCheckAreas().size()) {
                var timeUsedInMilliseconds = progress.getLast() - progress.getFirst();
                var formattedTime = numberFormatter.format((progress.getLast() - progress.getFirst()) / 1000D);
                player.sendMessage(getLanguage().finishNotice.produce(Pair.of("groupName", objective.getName()), Pair.of("time", formattedTime)));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);

                var record = recordThenReset(player, RecordBy.FINISHED);
                var isNewRecord = isBestPlay(player, record.timeInMillisecond());
                if (isNewRecord) {
                    player.sendMessage(getLanguage().newRecordNotice.produce());
                    playerBestCache.put(player.getUniqueId(), record);
                }
                pushRecordAsync(record);

                player.sendTitle(" ", getLanguage().finishSubtitle.produce(
                        Pair.of("groupName", objective.getName()),
                        Pair.of("time", formattedTime),
                        Pair.of("completeTag", isNewRecord ? getLanguage().newRecordTag.produce() : getLanguage().normalCompleteTag.produce()
                        )), 0, 20, 5);
            } else {
                var totalTime = numberFormatter.format((progress.getLast() - progress.getFirst()) / 1000D);
                var partTime = numberFormatter.format((progress.getLast() - progress.get(progress.size() - 2)) / 1000D);
                player.sendTitle(" ", getLanguage().checkAreaPassSubtitle.produce(Pair.of("groupName", objective.getName()), Pair.of("checkAreaNumber", progress.size() - 1), Pair.of("totalTime", totalTime), Pair.of("partTime", partTime)), 0, 20, 5);
                player.sendMessage(getLanguage().checkAreaPassNotice.produce(Pair.of("groupName", objective.getName()), Pair.of("checkAreaNumber", progress.size() - 1), Pair.of("totalTime", totalTime), Pair.of("partTime", partTime)));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);
            }
        }
    }

    private boolean isBestPlay(Player player, long timeUsedInMilliseconds) {
        if (!playerBestCache.containsKey(player.getUniqueId())) {
            return false; // degraded mode due to failed to load personal best
        } else {
            return timeUsedInMilliseconds < playerBestCache.get(player.getUniqueId()).timeInMillisecond();
        }
    }

    private void pushRecordAsync(PlayerRecord record) {
        NeedForSpeed.instance.getServer().getScheduler().runTaskAsynchronously(NeedForSpeed.instance, () -> {
            try {
                recorder.record(record);
            } catch (SQLException e) {
                NeedForSpeed.instance.getLogger().severe("Failed to log record");
                NeedForSpeed.instance.getLogger().severe(record.toString());
                throw new RuntimeException(e);
            }
        });
    }

    private PlayerRecord recordThenReset(Player player, RecordBy source) { // need playerProgress.contains(player.getUniqueId())
        var record = PlayerRecord.capture(objective.getUniqueID(), player.getUniqueId(), source, playerProgress.get(player.getUniqueId()));
        playerProgress.remove(player.getUniqueId());
        return record;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!objective.needClearOnDeath()) return;
        if (playerProgress.containsKey(event.getEntity().getUniqueId()) && !playerProgress.get(event.getEntity().getUniqueId()).isEmpty()) {
            event.getEntity().sendTitle(" ", getLanguage().timerResetAuto.produce(Pair.of("groupName", objective.getName())), 0, 20, 10);
            var record = recordThenReset(event.getEntity(), RecordBy.DEATH);
            pushRecordAsync(record);
        }
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }

    public Objective getObjective() {
        return objective;
    }

}
