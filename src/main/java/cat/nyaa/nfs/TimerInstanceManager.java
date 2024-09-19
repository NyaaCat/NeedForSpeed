package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.Objective;
import cat.nyaa.nfs.dataclasses.TimerRecords;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TimerInstanceManager {
    private final File timerDataFolder;
    private final File timerRecordFolder;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Map<String, UUID> loadedNameUniqueIDMap = new HashMap<>();
    private final Map<UUID, Objective> loadedUniqueIDObjectivesMap = new HashMap<>();
    private final Map<UUID, TimerInstance> enabledInstanceMap = new HashMap<>();
    private final PlayerRecordManager playerRecordManager;


    public TimerInstanceManager(File pluginDataFolder, PlayerRecordManager playerRecordManager) throws IOException {
        this.timerDataFolder = new File(pluginDataFolder, "objectives");
        this.timerRecordFolder = new File(pluginDataFolder, "groupRecords");
        this.playerRecordManager = playerRecordManager;
        timerDataFolder.mkdir();
        timerRecordFolder.mkdir();

        Arrays.stream(Objects.requireNonNull(timerDataFolder.listFiles())).filter(File::isFile).map(file -> {
            try {
                return gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Objective.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toList().forEach(objective -> {
                    try {
                        loadCheckAreaGroup(objective);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        Bukkit.getScheduler().runTaskTimerAsynchronously(NeedForSpeed.instance, () -> enabledInstanceMap.values().forEach(timerInstance -> {
            try {
                saveTimerRecord(timerInstance.getTimerRecords(), timerInstance.getObjective().getUniqueID());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }), 20 * 600L, 20 * 600L);
    }

    private void loadCheckAreaGroup(Objective objective) throws FileNotFoundException {
        loadedNameUniqueIDMap.put(objective.getName(), objective.getUniqueID());
        loadedUniqueIDObjectivesMap.put(objective.getUniqueID(), objective);
        if(objective.isEnabled()){
            enableCheckAreaGroup(objective);
        }
    }

    public void enableCheckAreaGroup(Objective objective) throws FileNotFoundException {
        var instance = new TimerInstance(objective, loadTimerRecord(objective.getUniqueID()), playerRecordManager);
        Bukkit.getServer().getPluginManager().registerEvents(instance, NeedForSpeed.instance);
        enabledInstanceMap.put(objective.getUniqueID(), instance);
    }

    public boolean deleteCheckAreaGroup(String name) {
        if (!loadedNameUniqueIDMap.containsKey(name))
            return false;
        else {
            var uniqueID = loadedNameUniqueIDMap.get(name);
            if (enabledInstanceMap.containsKey(uniqueID) || !loadedUniqueIDObjectivesMap.containsKey(uniqueID))
                return false;
            loadedNameUniqueIDMap.remove(name);
            loadedUniqueIDObjectivesMap.remove(uniqueID);
            return true;
        }
    }

    public boolean disableCheckAreaGroup(String name) {
        if (!loadedNameUniqueIDMap.containsKey(name))
            return false;
        else
            return disableCheckAreaGroup(loadedNameUniqueIDMap.get(name));
    }

    public boolean disableCheckAreaGroup(UUID uniqueID) {
        if (!enabledInstanceMap.containsKey(uniqueID))
            return false;
        var timerInstance = enabledInstanceMap.get(uniqueID);
        try {
            saveTimerRecord(timerInstance.getTimerRecords(), timerInstance.getObjective().getUniqueID());
            timerInstance.disable();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        enabledInstanceMap.remove(uniqueID);
        return true;
    }

    public boolean createNewObjective(String name) throws FileNotFoundException {
        if (loadedNameUniqueIDMap.containsKey(name)) return false;
        else {
            loadCheckAreaGroup(new Objective(UUID.randomUUID(), name, new ArrayList<>()));
            return true;
        }
    }

    public TimerInstance getTimerInstance(String name) {
        return loadedNameUniqueIDMap.get(name) == null ? null : getTimerInstance(loadedNameUniqueIDMap.get(name));
    }

    public TimerInstance getTimerInstance(UUID uniqueID) {
        return enabledInstanceMap.get(uniqueID);
    }

    public Objective getCheckAreaGroup(String name) {
        if (!loadedNameUniqueIDMap.containsKey(name))
            return null;
        else return getCheckAreaGroup(loadedNameUniqueIDMap.get(name));
    }

    public Objective getCheckAreaGroup(UUID uniqueID) {
        return loadedUniqueIDObjectivesMap.get(uniqueID);
    }

    public void saveAll() {
        enabledInstanceMap.values().forEach(timerInstance -> {
            try {
                saveTimerRecord(timerInstance.getTimerRecords(), timerInstance.getObjective().getUniqueID());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public TimerRecords loadTimerRecord(UUID uniqueID) throws FileNotFoundException {
        var recordFile = new File(timerRecordFolder, loadedUniqueIDObjectivesMap.get(uniqueID).getName() + ".json");
        return recordFile.exists() && recordFile.length() != 0 ? gson.fromJson(new InputStreamReader(new FileInputStream(recordFile), StandardCharsets.UTF_8), TimerRecords.class) : new TimerRecords();
    }

    public void saveTimerRecord(TimerRecords records, UUID timerUniqueID) throws IOException {
        var recordFile = new File(timerRecordFolder, timerUniqueID + ".json");
        recordFile.createNewFile();
        var writer = new OutputStreamWriter(new FileOutputStream(recordFile, false), StandardCharsets.UTF_8);
        gson.toJson(records, writer);
        writer.flush();
        writer.close();
    }

    public void saveObjective(Objective objective) throws IOException {
        var groupFile = new File(timerDataFolder, objective.getUniqueID() + ".json");
        groupFile.createNewFile();
        var writer = new OutputStreamWriter(new FileOutputStream(groupFile, false), StandardCharsets.UTF_8);
        gson.toJson(objective, writer);
        writer.flush();
        writer.close();
    }
}
