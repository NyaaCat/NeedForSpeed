package cat.nyaa.nfs;

import cat.nyaa.nfs.command.NFSCommand;
import land.melon.lab.simplelanguageloader.SimpleLanguageLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class NeedForSpeed extends JavaPlugin{
    public static NeedForSpeed instance;
    private final File dataFolder = this.getDataFolder();
    private TimerInstanceManager timerInstanceManager;
    private PlayerRecordManager playerRecordManager;
    private Language language;

    public TimerInstanceManager getTimerInstanceManager() {
        return timerInstanceManager;
    }

    public Language getLanguage() {
        return language;
    }

    @Override
    public void onEnable() {
        instance = this;
        dataFolder.mkdir();
        playerRecordManager = new PlayerRecordManager(new File(dataFolder, "playerData"));
        getCommand("nfs").setExecutor(new NFSCommand(this));
        try {
            timerInstanceManager = new TimerInstanceManager(dataFolder, playerRecordManager);
        } catch (IOException e) {
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }
        this.getServer().getPluginManager().registerEvents(playerRecordManager, this);
        try {
            reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void reload() throws IOException {
        var languageFile = new File(dataFolder, "language.json");
        SimpleLanguageLoader simpleLanguageLoader = new SimpleLanguageLoader();
        language = simpleLanguageLoader.loadOrInitialize(languageFile, Language.class, Language::new);
    }


    @Override
    public void onDisable() {
        timerInstanceManager.saveAll();
    }
}
