package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.CheckAreaGroup;
import cat.nyaa.nfs.dataclasses.TimerRecords;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import land.melon.lab.simplelanguageloader.SimpleLanguageLoader;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TimerInstanceManager {
    private final File timerDataFolder;
    private final File timerRecordFolder;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Map<String, UUID> nameUniqueIDMap = new HashMap<>();
    private final Map<UUID, CheckAreaGroup> uniqueIDTimerPointGroupMap = new HashMap<>();
    private final Map<UUID, TimerInstance> enabledMap = new HashMap<>();
    private final PlayerRecordManager playerRecordManager;
    private final File enabledCheckAreaGroupsFile;


    public TimerInstanceManager(File pluginDataFolder, PlayerRecordManager playerRecordManager) throws IOException {
        this.timerDataFolder = new File(pluginDataFolder, "checkAreaGroups");
        this.timerRecordFolder = new File(pluginDataFolder, "groupRecords");
        this.playerRecordManager = playerRecordManager;
        timerDataFolder.mkdir();
        timerRecordFolder.mkdir();
        enabledCheckAreaGroupsFile = new File(pluginDataFolder, "enabled.json");

        var enabledList = new SimpleLanguageLoader().loadOrInitialize(enabledCheckAreaGroupsFile, new TypeToken<ArrayList<UUID>>() {
        }.getType(), () -> new ArrayList<UUID>());

        Arrays.stream(Objects.requireNonNull(timerDataFolder.listFiles())).filter(File::isFile).map(file -> {
            try {
                return gson.fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), CheckAreaGroup.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toList().forEach(checkAreaGroup -> {
                    try {
                        loadCheckAreaGroup(checkAreaGroup);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        enabledList.forEach(uuid -> {
            if (!uniqueIDTimerPointGroupMap.containsKey(uuid)) {
                Bukkit.getLogger().warning("check area config " + uuid.toString() + ".json doesn't exist!");
            } else {
                try {
                    enableCheckAreaGroup(uniqueIDTimerPointGroupMap.get(uuid));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        Bukkit.getScheduler().runTaskTimer(NeedForSpeed.instance, () -> enabledMap.values().forEach(timerInstance -> {
            try {
                saveTimerRecord(timerInstance.getTimerRecords(), timerInstance.getCheckAreaGroup().getUniqueID());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }), 20 * 600L, 20 * 600L);
    }

    private void loadCheckAreaGroup(CheckAreaGroup checkAreaGroup) throws FileNotFoundException {
        nameUniqueIDMap.put(checkAreaGroup.getName(), checkAreaGroup.getUniqueID());
        uniqueIDTimerPointGroupMap.put(checkAreaGroup.getUniqueID(), checkAreaGroup);
    }

    public void enableCheckAreaGroup(CheckAreaGroup checkAreaGroup) throws FileNotFoundException {
        var instance = new TimerInstance(checkAreaGroup, loadTimerRecord(checkAreaGroup.getUniqueID()), playerRecordManager);
        Bukkit.getServer().getPluginManager().registerEvents(instance, NeedForSpeed.instance);
        enabledMap.put(checkAreaGroup.getUniqueID(), instance);
    }

    public boolean deleteCheckAreaGroup(String name) {
        if (!nameUniqueIDMap.containsKey(name))
            return false;
        else {
            var uniqueID = nameUniqueIDMap.get(name);
            if (enabledMap.containsKey(uniqueID) || !uniqueIDTimerPointGroupMap.containsKey(uniqueID))
                return false;
            nameUniqueIDMap.remove(name);
            uniqueIDTimerPointGroupMap.remove(uniqueID);
            return true;
        }
    }

    public boolean disableCheckAreaGroup(String name) {
        if (!nameUniqueIDMap.containsKey(name))
            return false;
        else
            return disableCheckAreaGroup(nameUniqueIDMap.get(name));
    }

    public boolean disableCheckAreaGroup(UUID uniqueID) {
        if (!enabledMap.containsKey(uniqueID))
            return false;
        var timerInstance = enabledMap.get(uniqueID);
        try {
            saveTimerRecord(timerInstance.getTimerRecords(), timerInstance.getCheckAreaGroup().getUniqueID());
            timerInstance.disable();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        enabledMap.remove(uniqueID);
        return true;
    }

    public boolean createNewCheckAreaGroup(String name) throws FileNotFoundException {
        if (nameUniqueIDMap.containsKey(name)) return false;
        else {
            loadCheckAreaGroup(new CheckAreaGroup(UUID.randomUUID(), name, new ArrayList<>()));
            return true;
        }
    }

    public TimerInstance getTimerInstance(String name) {
        return nameUniqueIDMap.get(name) == null ? null : getTimerInstance(nameUniqueIDMap.get(name));
    }

    public TimerInstance getTimerInstance(UUID uniqueID) {
        return enabledMap.get(uniqueID);
    }

    public CheckAreaGroup getCheckAreaGroup(String name) {
        if (!nameUniqueIDMap.containsKey(name))
            return null;
        else return getCheckAreaGroup(nameUniqueIDMap.get(name));
    }

    public CheckAreaGroup getCheckAreaGroup(UUID uniqueID) {
        return uniqueIDTimerPointGroupMap.get(uniqueID);
    }

    public void saveAll() {
        enabledMap.values().forEach(timerInstance -> {
            try {
                saveTimerRecord(timerInstance.getTimerRecords(), timerInstance.getCheckAreaGroup().getUniqueID());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            saveEnabled();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveEnabled() throws IOException {
        var outPutStream = new OutputStreamWriter(new FileOutputStream(enabledCheckAreaGroupsFile, false), StandardCharsets.UTF_8);
        gson.toJson(enabledMap.keySet(), outPutStream);
        outPutStream.flush();
        outPutStream.close();
    }

    public TimerRecords loadTimerRecord(UUID uniqueID) throws FileNotFoundException {
        var recordFile = new File(timerRecordFolder, uniqueID + ".json");
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

    public void saveCheckAreaGroup(CheckAreaGroup group) throws IOException {
        var groupFile = new File(timerDataFolder, group.getUniqueID() + ".json");
        groupFile.createNewFile();
        var writer = new OutputStreamWriter(new FileOutputStream(groupFile, false), StandardCharsets.UTF_8);
        gson.toJson(group, writer);
        writer.flush();
        writer.close();
    }
}
