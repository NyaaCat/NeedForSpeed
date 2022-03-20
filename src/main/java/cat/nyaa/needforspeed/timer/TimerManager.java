package cat.nyaa.needforspeed.timer;

import cat.nyaa.needforspeed.NeedForSpeed;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TimerManager {
    private final NeedForSpeed plugin;

    public TimerManager(NeedForSpeed pl) {
        plugin = pl;
    }

    public boolean createTimer(String name) {
        if (!plugin.getTimerConfig().timers.containsKey(name)) {
            Timer timer = new Timer();
            timer.setName(name);
            plugin.getTimerConfig().timers.put(name, timer.clone());
            return true;
        }
        return false;
    }

    public Timer getTimer(String name) {
        if (plugin.getTimerConfig().timers.containsKey(name)) {
            return plugin.getTimerConfig().timers.get(name);
        }
        return null;
    }

    public ArrayList<Checkpoint> getCheckpoint(Player player, boolean checkEnable) {
        return getCheckpoint(player.getLocation(), checkEnable);
    }

    public ArrayList<Checkpoint> getCheckpoint(Location loc, boolean checkEnable) {
        ArrayList<Checkpoint> list = new ArrayList<>();
        if (!plugin.getTimerConfig().timers.isEmpty()) {
            for (Timer timer : plugin.getTimerConfig().timers.values()) {
                if (checkEnable && !timer.isEnabled()) {
                    continue;
                }
                for (Checkpoint checkpoint : timer.getCheckpointList()) {
                    if (checkpoint.inArea(loc)) {
                        list.add(checkpoint.clone());
                        break;
                    }
                }
            }
        }
        return list;
    }

    public boolean removeTimer(String name) {
        if (plugin.getTimerConfig().timers.containsKey(name)) {
            plugin.getTimerConfig().timers.remove(name);
            return true;
        }
        return false;
    }
}
