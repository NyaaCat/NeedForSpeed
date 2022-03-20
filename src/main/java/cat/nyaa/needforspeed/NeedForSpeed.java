package cat.nyaa.needforspeed;

import cat.nyaa.needforspeed.timer.TimerCommands;
import cat.nyaa.needforspeed.timer.TimerConfig;
import cat.nyaa.needforspeed.timer.TimerListener;
import cat.nyaa.needforspeed.timer.TimerManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeedForSpeed extends JavaPlugin {

    private WorldEdit worldEdit;

    private TimerCommands timerCommands;
    private TimerListener timerListener;
    private WorldEditPlugin worldEditPlugin;
    private TimerManager timerManager;
    private TimerConfig timerConfig;
    private I18n i18n;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        load();
    }

    public WorldEditPlugin getWorldEditPlugin(){
        return this.worldEditPlugin;
    }

    public TimerManager getTimerManager(){
        return timerManager;
    }

    public TimerConfig getTimerConfig(){
        return this.timerConfig;
    }

    public void load() {
        reloadConfig();
        loadDependencies();

        // nyaa stuff
        i18n = new I18n(this);
        worldEditPlugin = getPlugin(WorldEditPlugin.class);
        timerManager = new TimerManager(this);
        timerConfig = new TimerConfig(this);
        timerCommands = new TimerCommands(this,i18n);
        timerListener = new TimerListener(this);

        getServer().getPluginManager().registerEvents(timerListener, this);
        getCommand("nfs").setExecutor(timerCommands);

        timerConfig.load();
    }

    private void loadDependencies() {
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = WorldEdit.getInstance();
        }

    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
