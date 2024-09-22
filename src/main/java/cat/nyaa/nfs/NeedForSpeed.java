package cat.nyaa.nfs;

import cat.nyaa.nfs.command.NFSCommand;
import cat.nyaa.nfs.save.Recorder;
import land.melon.lab.simplelanguageloader.SimpleLanguageLoader;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class NeedForSpeed extends JavaPlugin{
    public static NeedForSpeed instance;
    private final File dataFolder = this.getDataFolder();
    private TimerListenersManager timerListenersManager;
    private final SimpleLanguageLoader simpleLanguageLoader = new SimpleLanguageLoader();
    private Recorder recorder;
    private Language language;
    private GuidanceService guidanceService;

    public TimerListenersManager getTimerInstanceManager() {
        return timerListenersManager;
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public void onEnable() {
        instance = this;
        dataFolder.mkdir();

        try {
            reload();
        } catch (IOException | SQLException e) {
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }
    }

    private void reload() throws IOException, SQLException {

        onDisable();


        var languageFile = new File(dataFolder, "language.json");
        language = simpleLanguageLoader.loadOrInitialize(languageFile, Language.class, Language::new);

        var recorderFile = new File(dataFolder, "records.db");
        recorder = new Recorder(recorderFile);

        timerListenersManager = new TimerListenersManager(this, dataFolder);

        guidanceService = new GuidanceService();
        getServer().getPluginManager().registerEvents(guidanceService, this);

        getCommand("nfs").setExecutor(new NFSCommand(this));
    }

    public Recorder getRecorder() {
        return recorder;
    }

    public GuidanceService getGuidanceService() {
        return guidanceService;
    }

    @Override
    public void onDisable() {
        try {
            if (recorder != null) recorder.shutdown();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (timerListenersManager != null) timerListenersManager.shutdown();
        HandlerList.unregisterAll(this);
    }
}
