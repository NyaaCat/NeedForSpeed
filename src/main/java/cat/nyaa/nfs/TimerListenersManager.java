package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.Objective;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TimerListenersManager {
    private final File objectivesFolder;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final NeedForSpeed pluginInstance;
    private final Map<String, UUID> loadedNameUniqueIDMap = new HashMap<>();
    private final Map<UUID, TimerListenerInstance> timerListenerMap = new HashMap<>();


    public TimerListenersManager(NeedForSpeed pluginInstance, File pluginDataFolder) throws IOException {
        this.pluginInstance = pluginInstance;
        this.objectivesFolder = new File(pluginDataFolder, "objectives");
        if (!objectivesFolder.exists()) {
            objectivesFolder.mkdirs();
        }

        Arrays.stream(objectivesFolder.listFiles()).filter(File::isFile).filter(file -> file.getName().endsWith(".json")).forEach(file -> {
            try {
                loadObjective(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void loadObjective(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            Objective objective = gson.fromJson(reader, Objective.class);
            if (objective != null) {
                var timerListenerInstance = new TimerListenerInstance(objective, pluginInstance.getRecorder());
                loadedNameUniqueIDMap.put(objective.getName(), objective.getUniqueID());
                timerListenerMap.put(objective.getUniqueID(), timerListenerInstance);
                pluginInstance.getServer().getPluginManager().registerEvents(timerListenerInstance, pluginInstance);
            }
        }
    }

    public void saveObjective(Objective objective) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(objectivesFolder, objective.getName() + ".json")), StandardCharsets.UTF_8))) {
            writer.write(gson.toJson(objective));
        }
    }

    public void createNewObjective(Objective objective) throws IOException {
        saveObjective(objective);
        loadObjective(new File(objectivesFolder, objective.getName() + ".json"));
    }

    public TimerListenerInstance getListenerInstance(UUID uniqueID) {
        return timerListenerMap.get(uniqueID);
    }

    public TimerListenerInstance getListenerInstance(String name) {
        if (!loadedNameUniqueIDMap.containsKey(name)) {
            return null;
        }
        return getListenerInstance(loadedNameUniqueIDMap.get(name));
    }

}
