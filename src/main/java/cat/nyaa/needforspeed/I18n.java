package cat.nyaa.needforspeed;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.Plugin;

public class I18n extends LanguageRepository {
    private static I18n instance;
    Plugin plugin;
    public I18n(Plugin plugin) {
        super();
        instance = this;
    }

    public static String format(String key, Object... args){
        return instance.getFormatted(key, args);
    }
    @Override
    protected Plugin getPlugin() {
        return plugin;
    }

    @Override
    protected String getLanguage() {
        return (String) plugin.getConfig().get("language");
    }
}
