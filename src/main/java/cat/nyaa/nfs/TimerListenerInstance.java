package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.Objective;
import cat.nyaa.nfs.save.PlayerRecord;
import cat.nyaa.nfs.save.RecordBy;
import cat.nyaa.nfs.save.Recorder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
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
        if (!playerProgress.containsKey(event.getPlayer().getUniqueId()))
            playerProgress.put(event.getPlayer().getUniqueId(), new ArrayList<>());
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
        if (!objective.isClearOnQuit()) return;
        if (isPlayingThisObjective(event.getPlayer())) {
            var record = recordThenReset(event.getPlayer(), RecordBy.QUIT);
            pushRecordAsync(record);
        }
        if (playerProgress.get(event.getPlayer().getUniqueId()).isEmpty())
            playerProgress.remove(event.getPlayer().getUniqueId());
        resetCoolDownMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!objective.isClearOnDeath()) return;
        if (isPlayingThisObjective(event.getEntity())) {
            sendTimerResetAutoTitle(event.getEntity());
            var record = recordThenReset(event.getEntity(), RecordBy.DEATH);
            pushRecordAsync(record);
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (!playerProgress.containsKey(event.getPlayer().getUniqueId()))
            return;
        if (isPlayingThisObjective(event.getPlayer())) {
            var progress = playerProgress.get(event.getPlayer().getUniqueId());
            var worldNameNeed = objective.getCheck(progress.size()).getWorld();
            if (!worldNameNeed.equals(event.getPlayer().getWorld().getName())) {
                sendTimerResetAutoTitle(event.getPlayer());
                var record = recordThenReset(event.getPlayer(), RecordBy.CHANGE_WORLD);
                pushRecordAsync(record);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null)
            return;
        if (objective.getSize() < 3)
            return;
        if (!objective.isEnabled())
            return;
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR)
            return;
        var player = event.getPlayer();
        List<Long> progress = playerProgress.get(player.getUniqueId());

        if (System.currentTimeMillis() - resetCoolDownMap.getOrDefault(player.getUniqueId(), Long.MAX_VALUE) > 3000 // which is 3 seconds
                && !progress.isEmpty()
                && objective.getCheck(0).isRelevant(event.getFrom(), event.getTo())) {
            var record = recordThenReset(player, RecordBy.RESTARTED);
            pushRecordAsync(record);
        }
        if (objective.getCheck(progress.size()).isRelevant(event.getFrom(), event.getTo())) {
            progress.add(System.currentTimeMillis());
            if (progress.size() == 1) {
                player.sendTitle(" ", getLanguage().firstCheckAreaSubtitle.produce(Pair.of("groupName", objective.getName())), 0, 20, 5);
                player.sendMessage(getLanguage().firstCheckAreaNotice.produce(Pair.of("groupName", objective.getName())));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);
                resetCoolDownMap.put(player.getUniqueId(), System.currentTimeMillis());
                playerProgress.put(player.getUniqueId(), progress);
                NeedForSpeed.instance.getGuidanceService().updateGuidance(player.getUniqueId(), objective.getCheck(1).getCenter(), objective.getCheck(1).getWorld());
            } else if (progress.size() == objective.getCheckRanges().size()) {
                var timeUsedInMilliseconds = progress.getLast() - progress.getFirst();
                var formattedTime = numberFormatter.format(timeUsedInMilliseconds / 1000D);
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
                player.sendTitle(" ", getLanguage().checkAreaPassSubtitle.produce(Pair.of("groupName", objective.getName()), Pair.of("checkAreaNumber", currentCheckRangeNumber(progress)), Pair.of("totalTime", totalTime), Pair.of("partTime", partTime)), 0, 20, 5);
                player.sendMessage(getLanguage().checkAreaPassNotice.produce(Pair.of("groupName", objective.getName()), Pair.of("checkAreaNumber", currentCheckRangeNumber(progress)), Pair.of("totalTime", totalTime), Pair.of("partTime", partTime)));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);
                NeedForSpeed.instance.getGuidanceService().updateGuidance(player.getUniqueId(), objective.getCheck(progress.size()).getCenter(), objective.getCheck(progress.size()).getWorld());
            }
        }
    }

    private int currentCheckRangeNumber(List<Long> progress) {
        return objective.isFirstRangeCountsCheckNumber() ? progress.size() : progress.size() - 1;
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
            pushRecordSync(record);
        });
    }

    private void pushRecordSync(PlayerRecord record) {
        try {
            recorder.record(record);
        } catch (SQLException e) {
            NeedForSpeed.instance.getLogger().severe("Failed to log record");
            NeedForSpeed.instance.getLogger().severe(record.toString());
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        HandlerList.unregisterAll(this);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            if (isPlayingThisObjective(player)) {
                var record = recordThenReset(player, RecordBy.SERVER_SHUTDOWN);
                pushRecordSync(record);
            }
        });
    }

    private PlayerRecord recordThenReset(Player player, RecordBy source) { // need playerProgress.contains(player.getUniqueId())
        var record = PlayerRecord.capture(objective.getUniqueID(), player.getUniqueId(), source, playerProgress.get(player.getUniqueId()));
        playerProgress.get(player.getUniqueId()).clear();
        NeedForSpeed.instance.getGuidanceService().removeGuidance(player.getUniqueId());
        return record;
    }

    private boolean isPlayingThisObjective(Player player) {
        return playerProgress.containsKey(player.getUniqueId()) && !playerProgress.get(player.getUniqueId()).isEmpty();
    }

    private void sendTimerResetAutoTitle(Player player) {
        player.sendTitle(" ", getLanguage().timerResetAuto.produce(Pair.of("groupName", objective.getName())), 0, 20, 10);
    }

    public Objective getObjective() {
        return objective;
    }

}
