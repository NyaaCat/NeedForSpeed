package cat.nyaa.needforspeed.timer;

import cat.nyaa.needforspeed.I18n;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.needforspeed.NeedForSpeed;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class TimerCommands extends CommandReceiver {
    private NeedForSpeed plugin;

    public TimerCommands(Object plugin, LanguageRepository i18n) {
        super((NeedForSpeed) plugin, i18n);
        this.plugin = (NeedForSpeed) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "timer";
    }


    @SubCommand(value = "create", permission = "nu.createtimer")
    public void commandCreateTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.create.usage");
            return;
        }
        String name = args.nextString();
        if (plugin.getTimerManager().createTimer(name)) {
            msg(sender, "user.timer.timer_create");
            plugin.getTimerConfig().save();
        } else {
            msg(sender, "user.timer.timer_exist", name);
        }
    }

    @SubCommand(value = "remove", permission = "nu.createtimer")
    public void commandRemoveTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.remove.usage");
            return;
        }
        String name = args.next();
        if (plugin.getTimerManager().removeTimer(name)) {
            msg(sender, "user.timer.timer_remove", name);
            plugin.getTimerConfig().save();
        } else {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
    }

    @SubCommand(value = "enable", permission = "nu.createtimer")
    public void commandEnableTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.enable.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.getTimerManager().getTimer(name);
        if (timer != null) {
            timer.setEnable(true);
            msg(sender, "user.timer.timer_enable", name);
            plugin.getTimerConfig().save();
        } else {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
    }

    @SubCommand(value = "disable", permission = "nu.createtimer")
    public void commandDisableTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.disable.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.getTimerManager().getTimer(name);
        if (timer != null) {
            timer.setEnable(false);
            msg(sender, "user.timer.timer_disable", name);
            plugin.getTimerConfig().save();
        } else {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
    }

    @SubCommand(value = "addcheckpoint", permission = "nu.createtimer")
    public void commandAddCheckpoint(CommandSender sender, Arguments args) {
        if (args.length() < 3) {
            msg(sender, "manual.timer.addcheckpoint.usage");
            return;
        }
        Player player = asPlayer(sender);
        String name = args.nextString();
        Timer timer = plugin.getTimerManager().getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        Location pos1 = null;
        Location pos2 = null;
        if (args.remains() >= 6) {
            pos1 = new Location(player.getWorld(), args.nextInt(), args.nextInt(), args.nextInt());
            pos2 = new Location(player.getWorld(), args.nextInt(), args.nextInt(), args.nextInt());
        } else if (plugin.getWorldEditPlugin() != null) {
            Region selection = null;
            try {
                selection = plugin.getWorldEditPlugin().getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
                if (selection != null) {
                    pos1 = BukkitAdapter.adapt(player.getWorld(), selection.getMinimumPoint());
                    pos2 = BukkitAdapter.adapt(player.getWorld(), selection.getMaximumPoint());
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } else {
            msg(sender, "user.timer.select");
            return;
        }

        int checkpointID = -1;
        if (args.remains() == 1) {
            checkpointID = args.nextInt();
        }
        int id = 0;
        if (checkpointID == -1) {
            id = plugin.getTimerConfig().timers.get(name).addCheckpoint(pos1, pos2);
        } else {
            id = plugin.getTimerConfig().timers.get(name).addCheckpoint(checkpointID, pos1, pos2);
        }
        msg(sender, "user.timer.checkpoint_info", id, player.getWorld().getName(),
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
        plugin.getTimerConfig().save();
    }

    @SubCommand(value = "removecheckpoint", permission = "nu.createtimer")
    public void commandRemoveCheckpoint(CommandSender sender, Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.timer.removecheckpoint.usage");
            return;
        }
        String name = args.next();
        int checkpointID = args.nextInt();
        Timer timer = plugin.getTimerManager().getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        if (timer.getCheckpoint(checkpointID) != null) {
            timer.removeCheckpoint(checkpointID);
            plugin.getTimerConfig().save();
            msg(sender, "user.timer.checkpoint_remove", checkpointID);
        } else {
            msg(sender, "user.timer.checkpoint_not_found", checkpointID);
        }
    }

    @SubCommand(value = "togglepointbroadcast", permission = "nu.createtimer")
    public void commandTogglePointBroadcast(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.togglepointbroadcast.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.getTimerManager().getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        if (plugin.getTimerConfig().timers.get(name).togglePointBroadcast()) {
            msg(sender, "user.timer.checkpoint_broadcast_enable");
        } else {
            msg(sender, "user.timer.checkpoint_broadcast_disable");
        }
        plugin.getTimerConfig().save();
    }

    @SubCommand(value = "togglefinishbroadcast", permission = "nu.createtimer")
    public void commandFinishPointBroadcast(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.togglefinishbroadcast.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.getTimerManager().getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        if (plugin.getTimerConfig().timers.get(name).toggleFinishBroadcast()) {
            msg(sender, "user.timer.finishpoint_broadcast_enable");
        } else {
            msg(sender, "user.timer.finishpoint_broadcast_disable");
        }
        plugin.getTimerConfig().save();
    }

    @SubCommand(value = "info", permission = "nu.createtimer")
    public void commandInfo(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.info.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.getTimerManager().getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        String point_broadcast = I18n.format("user.info." + (timer.point_broadcast ? "enabled" : "disabled"));
        String finish_broadcast = I18n.format("user.info." + (timer.finish_broadcast ? "enabled" : "disabled"));
        String status = I18n.format("user.info." + (timer.isEnabled() ? "enabled" : "disabled"));
        msg(sender, "user.timer.timer_info", timer.getName(), timer.getCheckpointList().size(), status, point_broadcast, finish_broadcast);
        for (Checkpoint c : timer.getCheckpointList()) {
            msg(sender, "user.timer.checkpoint_info", c.getCheckpointID(), c.getMaxPos().getWorld().getName(),
                    c.getMaxPos().getBlockX(), c.getMaxPos().getBlockY(), c.getMaxPos().getBlockZ(),
                    c.getMinPos().getBlockX(), c.getMinPos().getBlockY(), c.getMinPos().getBlockZ());
        }
    }

    @SubCommand(value = "list", permission = "nu.createtimer")
    public void commandList(CommandSender sender, Arguments args) {
        HashMap<String, Timer> timers = plugin.getTimerConfig().timers;
        msg(sender, "user.timer.list", timers.size());
        for (Timer timer : timers.values()) {

            String pointBroadcast = TimerLang.getInstance().pointBroadcast.produce(Pair.of("name", timer.getName()));
            String finishBroadcast = TimerLang.getInstance().finishBroadcast.produce(Pair.of("name", timer.getName()));
            String status = TimerLang.getInstance().status.produce(
                    Pair.of("status", timer.isEnabled()? "enabled": "disabled")
            );
            new Message(TimerLang.getInstance().info.produce(
                    Pair.of("timer", timer.getName()),
                    Pair.of("checkPoint", timer.getCheckpointList().size()),
                    Pair.of("status", status),
                    Pair.of("pointBroadcast", pointBroadcast),
                    Pair.of("finishBroadcast", finishBroadcast)
                    )).send(sender);
        }
    }
}