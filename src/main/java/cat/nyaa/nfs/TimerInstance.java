package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.CheckArea;
import cat.nyaa.nfs.dataclasses.Objective;
import cat.nyaa.nfs.dataclasses.TimerRecords;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.text.DecimalFormat;
import java.util.*;

public class TimerInstance implements Listener {
    private final Objective objective;
    private final TimerRecords timerRecords;
    private final PlayerRecordManager playerRecordManager;
    private final HashMap<UUID, List<Long>> playerProgress = new HashMap<>();
    private final Map<UUID, Long> resetCoolDownMap = new HashMap<>();
    private final DecimalFormat numberFormatter = new DecimalFormat("#0.00");


    public TimerInstance(Objective objective, TimerRecords timerRecords, PlayerRecordManager playerRecordManager) {
        this.objective = objective;
        this.timerRecords = timerRecords;
        this.playerRecordManager = playerRecordManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null)
            return;
        if (objective.getSize() < 3)
            return;
        List<Long> progress;
        if (playerProgress.containsKey(event.getPlayer().getUniqueId()))
            progress = playerProgress.get(event.getPlayer().getUniqueId());
        else {
            progress = new ArrayList<>();
            playerProgress.put(event.getPlayer().getUniqueId(), progress);
        }
        if (System.currentTimeMillis() - resetCoolDownMap.getOrDefault(event.getPlayer().getUniqueId(), System.currentTimeMillis()) > 3000 // which is 1 seconds
                && !progress.isEmpty()
                && isRelevant(event.getFrom(), event.getTo(), objective.getCheckArea(0))) {
            playerProgress.remove(event.getPlayer().getUniqueId());
            resetCoolDownMap.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
        if (isRelevant(event.getFrom(), event.getTo(), objective.getCheckArea(progress.size()))) {
            progress.add(System.currentTimeMillis());
            if (progress.size() == 1) {
                resetCoolDownMap.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
                event.getPlayer().sendTitle(" ", NeedForSpeed.instance.getLanguage().firstCheckAreaSubtitle.produce(Pair.of("groupName", objective.getName())), 0, 20, 5);
                event.getPlayer().sendMessage(NeedForSpeed.instance.getLanguage().firstCheckAreaNotice.produce(Pair.of("groupName", objective.getName())));
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);
                playerProgress.put(event.getPlayer().getUniqueId(), progress);
            } else if (progress.size() == objective.getCheckAreas().size()) {
                var time = numberFormatter.format((progress.get(progress.size() - 1) - progress.get(0)) / 1000D);
                event.getPlayer().sendMessage(NeedForSpeed.instance.getLanguage().finishNotice.produce(Pair.of("groupName", objective.getName()), Pair.of("time", time)));
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);
                playerProgress.remove(event.getPlayer().getUniqueId());
                var isNewRecord = playerRecordManager.pushNewRecord(event.getPlayer().getUniqueId(), objective.getUniqueID(), new ArrayList<>(progress));
                if (isNewRecord) {
                    event.getPlayer().sendMessage(NeedForSpeed.instance.getLanguage().newRecordNotice.produce());
                    timerRecords.update(event.getPlayer().getUniqueId(), progress.get(progress.size() - 1) - progress.get(0));
                }
                event.getPlayer().sendTitle(" ", NeedForSpeed.instance.getLanguage().finishSubtitle.produce(
                        Pair.of("groupName", objective.getName()),
                        Pair.of("time", time),
                        Pair.of("completeTag", isNewRecord ? NeedForSpeed.instance.getLanguage().newRecordTag.produce() : NeedForSpeed.instance.getLanguage().normalCompleteTag.produce()
                        )), 0, 20, 5);
            } else {
                var totalTime = numberFormatter.format((progress.get(progress.size() - 1) - progress.get(0)) / 1000D);
                var partTime = numberFormatter.format((progress.get(progress.size() - 1) - progress.get(progress.size() - 2)) / 1000D);
                event.getPlayer().sendTitle(" ", NeedForSpeed.instance.getLanguage().checkAreaPassSubtitle.produce(Pair.of("groupName", objective.getName()), Pair.of("checkAreaNumber", progress.size() - 1), Pair.of("totalTime", totalTime), Pair.of("partTime", partTime)), 0, 20, 5);
                event.getPlayer().sendMessage(NeedForSpeed.instance.getLanguage().checkAreaPassNotice.produce(Pair.of("groupName", objective.getName()), Pair.of("checkAreaNumber", progress.size() - 1), Pair.of("totalTime", totalTime), Pair.of("partTime", partTime)));
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 1f);
            }
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (playerProgress.containsKey(event.getEntity().getUniqueId()) && !playerProgress.get(event.getEntity().getUniqueId()).isEmpty()) {
            event.getEntity().sendTitle(NeedForSpeed.instance.getLanguage().timerResetAuto.produce(Pair.of("groupName", objective.getName())), " ", 0, 20, 10);
            playerProgress.remove(event.getEntity().getUniqueId());
        }
    }

    private boolean isRelevant(Location from, Location to, CheckArea checkArea) {
        var a = checkArea.getA();
        var b = checkArea.getB();
        var xPitch = Math.max(
                1 - (a.getX() - b.getX()) / 2, 0
        );
        var yPitch = Math.max(
                1 - (a.getY() - b.getY()) / 2, 0
        );
        var zPitch = Math.max(
                1 - (a.getZ() - b.getZ()) / 2, 0
        );
        if (from.getWorld().getName().equals(checkArea.getWorld())) {
            if (a.getX() + xPitch >= to.getBlockX() && b.getX() - xPitch <= to.getBlockX()) {
                if (a.getY() + yPitch >= to.getBlockY() && b.getY() - yPitch <= to.getBlockY()) {
                    return a.getZ() + zPitch >= to.getBlockZ() && b.getZ() - zPitch <= to.getBlockZ();
                }
            }
        }
        return false;
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }

    public Objective getObjective() {
        return objective;
    }

    public TimerRecords getTimerRecords() {
        return timerRecords;
    }
}
