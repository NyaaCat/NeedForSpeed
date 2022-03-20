package cat.nyaa.needforspeed.timer;


import cat.nyaa.needforspeed.NeedForSpeed;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class TimerConfig extends FileConfigure {
    private final NeedForSpeed plugin;
    /* Timer */
    @Serializable
    private int timerCheckInterval = -1;

    public HashMap<String, Timer> timers = new HashMap<>();

    public TimerConfig(NeedForSpeed pl) {
        this.plugin = pl;
    }

    public int getTimerCheckInterval() {
        return timerCheckInterval;
    }

    public void setTimerCheckInterval(int timerCheckInterval) {
        this.timerCheckInterval = timerCheckInterval;
    }

    public HashMap<String, Timer> getTimers() {
        return timers;
    }

    public void setTimers(HashMap<String, Timer> timers) {
        this.timers = timers;
    }

    @Override
    protected String getFileName() {
        return "timers.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        timers.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("timers")) {
            ConfigurationSection list = config.getConfigurationSection("timers");
            for (String k : list.getKeys(false)) {
                Timer timer = new Timer();
                timer.deserialize(list.getConfigurationSection(k));
                timers.put(timer.getName(), timer.clone());
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("timers", null);
        if (!timers.isEmpty()) {
            ConfigurationSection list = config.createSection("timers");
            for (String k : timers.keySet()) {
                timers.get(k).serialize(list.createSection(k));
            }
        }

    }

}