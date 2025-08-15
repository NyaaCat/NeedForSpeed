package cat.nyaa.nfs.command;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;

import com.sk89q.worldedit.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CkptCommand implements CommandExecutor {

    int counter = 0;

    private WorldEditPlugin getWorldEdit() {
        return (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "unable to comply non-player execution");
            return true;
        }
        if(!sender.hasPermission("nfs.admin")){
            sender.sendMessage("permission denied!");
            return true;
        }
        Player player = (Player) sender;

        var actor = BukkitAdapter.adapt(player); // WorldEdit's native Player class extends Actor
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        LocalSession localSession = manager.get(actor);

        try {
            var region = localSession.getSelection();
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            String json = String.format(
                    "{\"world\":\"%s\",\"a\":{\"x\":%.1f,\"y\":%.1f,\"z\":%.1f},\"b\":{\"x\":%.1f,\"y\":%.1f,\"z\":%.1f}}",
                    player.getWorld().getName(),
                    (double) max.x(), (double) max.y(), (double) max.z(),
                    (double) min.x(), (double) min.y(), (double) min.z()
            );

            player.sendMessage(ChatColor.GREEN + String.format("ckpt %04d: %s", counter++, json));

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "region not selected");
        }

        return true;
    }
}
