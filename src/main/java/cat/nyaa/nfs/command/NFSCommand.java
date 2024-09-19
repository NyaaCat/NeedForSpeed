package cat.nyaa.nfs.command;

import cat.nyaa.nfs.NeedForSpeed;
import cat.nyaa.nfs.dataclasses.CheckArea;
import cat.nyaa.nfs.dataclasses.Point;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class NFSCommand implements TabExecutor {
    private final WorldEditPlugin worldEditPlugin = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
    private final NeedForSpeed pluginInstance;
    private final DecimalFormat numberFormatter = new DecimalFormat("#0.00");

    public NFSCommand(NeedForSpeed pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(pluginInstance.getLanguage().playerOnlyCommand.produce());
            return true;
        }
        // nfs objective create/del/rename <name>
        // nfs checkarea <groupname> new/del <index>
        if (args.length < 1) {
            sender.sendMessage(pluginInstance.getLanguage().usage.produce());
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "objective" -> {
                if (args.length < 3) {
                    sender.sendMessage(pluginInstance.getLanguage().usage.produce());
                    return true;
                }
                switch (args[1].toLowerCase(Locale.ROOT)) {
                    case "new", "create" -> {
                        if (pluginInstance.getTimerInstanceManager().getCheckAreaGroup(args[2]) != null) {
                            sender.sendMessage(pluginInstance.getLanguage().nameUsed.produce(Pair.of("name", args[2])));
                            return true;
                        } else {
                            try {
                                pluginInstance.getTimerInstanceManager().createNewObjective(args[2]);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            sender.sendMessage(pluginInstance.getLanguage().checkAreaGroupCreated.produce(Pair.of("groupName", args[2])));
                        }
                    }
                    case "del", "delete" -> {
                        if (pluginInstance.getTimerInstanceManager().getCheckAreaGroup(args[2]) == null) {
                            sender.sendMessage(pluginInstance.getLanguage().checkAreaGroupNotFound.produce(Pair.of("name", args[2])));
                            return true;
                        }
                        if (pluginInstance.getTimerInstanceManager().getTimerInstance(args[2]) != null) {
                            sender.sendMessage(pluginInstance.getLanguage().needDisableFirst.produce(Pair.of("name", args[2])));
                            return true;
                        }
                        pluginInstance.getTimerInstanceManager().deleteCheckAreaGroup(args[2]);
                        sender.sendMessage(pluginInstance.getLanguage().checkAreaGroupDeleted.produce(Pair.of("groupName", args[2])));
                    }
                    case "enable" -> {
                        var group = pluginInstance.getTimerInstanceManager().getCheckAreaGroup(args[2]);
                        if (group == null) {
                            sender.sendMessage(pluginInstance.getLanguage().checkAreaGroupNotFound.produce(Pair.of("groupName", args[2])));
                            return true;
                        }
                        if (pluginInstance.getTimerInstanceManager().getTimerInstance(args[2]) != null) {
                            sender.sendMessage(pluginInstance.getLanguage().failedToEnable.produce(Pair.of("groupName", args[2])));
                            return true;
                        }
                        try {
                            pluginInstance.getTimerInstanceManager().enableCheckAreaGroup(group);
                            sender.sendMessage(pluginInstance.getLanguage().enabledSuccessful.produce(Pair.of("groupName", args[2])));
                        } catch (FileNotFoundException e) {
                            sender.sendMessage(pluginInstance.getLanguage().failedToApplyOperation.produce());
                            throw new RuntimeException(e);
                        }
                    }
                    case "disable" -> {
                        if (pluginInstance.getTimerInstanceManager().getCheckAreaGroup(args[2]) == null) {
                            sender.sendMessage(pluginInstance.getLanguage().checkAreaGroupNotFound.produce(Pair.of("groupName", args[2])));
                            return true;
                        }
                        if (pluginInstance.getTimerInstanceManager().disableCheckAreaGroup(args[2])) {
                            sender.sendMessage(pluginInstance.getLanguage().disabledSuccessful.produce(Pair.of("groupName", args[2])));
                        } else {
                            sender.sendMessage(pluginInstance.getLanguage().failedToDisable.produce(Pair.of("groupName", args[2])));
                        }
                    }
                    case "rank" -> {
                        if (pluginInstance.getTimerInstanceManager().getCheckAreaGroup(args[2]) == null) {
                            sender.sendMessage(pluginInstance.getLanguage().checkAreaGroupNotFound.produce(Pair.of("groupName", args[2])));
                            return true;
                        }
                        if (pluginInstance.getTimerInstanceManager().getTimerInstance(args[2]) == null) {
                            sender.sendMessage(pluginInstance.getLanguage().enableBeforeExportRank.produce(Pair.of("groupName", args[2])));
                            return true;
                        }
                        var stringBuilder = new StringBuilder(pluginInstance.getLanguage().rankTitle.produce(Pair.of("groupName", args[2]))).append("\n");
                        var entryList = pluginInstance.getTimerInstanceManager().getTimerInstance(args[2]).getTimerRecords().getRecords().entrySet().stream()
                                .sorted(Map.Entry.comparingByValue()).toList();
                        var playerScope = (int) Math.log10(entryList.size()) + 1;
                        for (int i = 0; i < entryList.size(); i++) {
                            stringBuilder.append(pluginInstance.getLanguage().rankLine.produce(
                                    Pair.of("number", String.format("%-" + playerScope + "d", i + 1)),
                                    Pair.of("playerNameAligned", String.format("%-16s", Bukkit.getOfflinePlayer(entryList.get(i).getKey()).getName())),
                                    Pair.of("time", numberFormatter.format(entryList.get(i).getValue() / 1000D))
                            )).append("\n");
                        }
                        sender.sendMessage(stringBuilder.toString());
                    }
                }

            }
            case "checkarea" -> {
                if (args.length < 3) {
                    sender.sendMessage(pluginInstance.getLanguage().usage.produce());
                    return true;
                }
                var checkAreaGroup = pluginInstance.getTimerInstanceManager().getCheckAreaGroup(args[1]);
                if (checkAreaGroup == null) {
                    sender.sendMessage(pluginInstance.getLanguage().checkAreaGroupNotFound.produce(Pair.of("groupName", args[1])));
                    return true;
                }
                switch (args[2].toLowerCase(Locale.ROOT)) {
                    case "del", "delete" -> {
                        if (args.length < 4) {
                            if (checkAreaGroup.removeLastArea()) {
                                sender.sendMessage(pluginInstance.getLanguage().checkAreaDeleted.produce(
                                        Pair.of("checkAreaName", checkAreaGroup.getName()),
                                        Pair.of("checkAreaNumber", checkAreaGroup.getSize()))
                                );
                            }
                            return true;
                        }
                        try {
                            var index = Integer.valueOf(args[3]);
                            if (index > checkAreaGroup.getSize() - 1)
                                throw new NumberFormatException();
                            if (checkAreaGroup.removeCheckArea(index))
                                sender.sendMessage(
                                        pluginInstance.getLanguage().checkAreaDeleted.produce(
                                                Pair.of("checkAreaName", checkAreaGroup.getName()),
                                                Pair.of("checkAreaNumber", index)
                                        ));
                            else sender.sendMessage(
                                    pluginInstance.getLanguage().failedToDelete.produce()
                            );
                        } catch (NumberFormatException exception) {
                            sender.sendMessage(pluginInstance.getLanguage().notValidIndex.produce(
                                    Pair.of("input", args[4]),
                                    Pair.of("indexMin", 0),
                                    Pair.of("indexMax", checkAreaGroup.getSize() - 1)
                            ));
                        }

                    }
                    case "new", "create" -> {
                        Location posMax;
                        Location posMin;
                        try {
                            var selection = worldEditPlugin.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
                            if (selection == null) {
                                sender.sendMessage(pluginInstance.getLanguage().needSelectPoint.produce());
                                return true;
                            }
                            posMax = BukkitAdapter.adapt(player.getWorld(), selection.getMaximumPoint());
                            posMin = BukkitAdapter.adapt(player.getWorld(), selection.getMinimumPoint());
                        } catch (Exception e) {
                            sender.sendMessage("Some error occurred...");
                            e.printStackTrace();
                            return true;
                        }
                        if (args.length < 4) {
                            checkAreaGroup.addCheckArea(new CheckArea(posMax.getWorld().getName(),
                                    new Point(posMax.getBlockX(), posMax.getBlockY(), posMax.getBlockZ()),
                                    new Point(posMin.getBlockX(), posMin.getBlockY(), posMin.getBlockZ())));
                            sender.sendMessage(
                                    pluginInstance.getLanguage().checkAreaCreated.produce(
                                            Pair.of("groupName", checkAreaGroup.getName()),
                                            Pair.of("checkAreaNumber", checkAreaGroup.getSize() - 1)
                                    ));
                        } else {
                            try {
                                var index = Integer.valueOf(args[3]);
                                if (index > checkAreaGroup.getSize() - 1)
                                    throw new NumberFormatException();
                                checkAreaGroup.addCheckArea(index, new CheckArea(posMax.getWorld().getName(),
                                        new Point(posMax.getBlockX(), posMax.getBlockY(), posMax.getBlockZ()),
                                        new Point(posMin.getBlockX(), posMin.getBlockY(), posMin.getBlockZ())));
                                sender.sendMessage(
                                        pluginInstance.getLanguage().checkAreaCreated.produce(
                                                Pair.of("checkAreaName", checkAreaGroup.getName()),
                                                Pair.of("checkAreaNumber", index)
                                        ));
                            } catch (NumberFormatException exception) {
                                sender.sendMessage(pluginInstance.getLanguage().notValidIndex.produce(
                                        Pair.of("input", args[3]),
                                        Pair.of("indexMin", 0),
                                        Pair.of("indexMax", checkAreaGroup.getSize() - 1)
                                ));
                            }
                        }
                        try {
                            pluginInstance.getTimerInstanceManager().saveObjective(checkAreaGroup);
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage(pluginInstance.getLanguage().failedToApplyOperation.produce());
                        }
                    }

                }
            }
            default -> sender.sendMessage(pluginInstance.getLanguage().usage.produce());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
