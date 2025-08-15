package cat.nyaa.nfs;

import cat.nyaa.nfs.command.CkptCommand;
import cat.nyaa.nfs.command.NFSCommand;
import cat.nyaa.nfs.save.Recorder;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import land.melon.lab.simplelanguageloader.SimpleLanguageLoader;
import land.melon.lab.simplelanguageloader.utils.Pair;
import org.bukkit.event.HandlerList;
import org.bukkit.persistence.PersistentDataType;
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
        getCommand("ckpt").setExecutor(new CkptCommand());
        registerCommands();
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
        unregisterCommands();
        HandlerList.unregisterAll(this);
    }

    private void registerCommands() {
        new CommandAPICommand("guidance").withArguments(new StringArgument("status").replaceSuggestions(ArgumentSuggestions.strings("on", "partial", "off"))).executesPlayer((player, args) -> {
            var arg = (String) args.get("status");
            if (arg == null) return;
            var pdc = player.getPersistentDataContainer();
            GuidanceLevel guidanceLevel;
            String statusText;
            switch (arg) {
                case "partial" -> {
                    guidanceLevel = GuidanceLevel.PARTIAL;
                    statusText = language.partial.produce();
                }
                case "off" -> {
                    guidanceLevel = GuidanceLevel.OFF;
                    statusText = language.off.produce();
                }
                default -> {
                    guidanceLevel = GuidanceLevel.ON;
                    statusText = language.on.produce();
                }
            }
            player.sendMessage(language.guidanceEnabled.produce(Pair.of("level", statusText)));
            pdc.set(GuidanceService.guidancePreferenceKey, PersistentDataType.INTEGER, guidanceLevel.ordinal());
        }).register(this);
    }

    private void unregisterCommands() {
        CommandAPI.unregister("guidance");
    }
}
