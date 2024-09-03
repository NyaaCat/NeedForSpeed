package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.PlayerRecords;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerRecordManager implements Listener {
    private final Map<UUID, PlayerRecords> playerRecordsMap = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final File playerDataFolder;


    public PlayerRecordManager(File playerDataFolder) {
        this.playerDataFolder = playerDataFolder;
        playerDataFolder.mkdir();
    }

    private void loadPlayerData(UUID playerUniqueID) throws IOException {
        var playerData = new File(playerDataFolder, playerUniqueID + ".json");
        if (!playerData.createNewFile() && playerData.length() > 0) {
            playerRecordsMap.put(playerUniqueID, gson.fromJson(new InputStreamReader(new FileInputStream(playerData), StandardCharsets.UTF_8), PlayerRecords.class));
        } else {
            playerRecordsMap.put(playerUniqueID, new PlayerRecords(new HashMap<>()));
        }
    }

    private void savePlayerData(UUID playerUniqueID) throws IOException {
        var playerDataFile = new File(playerDataFolder, playerUniqueID + ".json");
        playerDataFile.createNewFile();
        var writer = new OutputStreamWriter(new FileOutputStream(playerDataFile, false), StandardCharsets.UTF_8);
        gson.toJson(playerRecordsMap.get(playerUniqueID), writer);
        writer.flush();
        writer.close();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        try {
            savePlayerData(event.getPlayer().getUniqueId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            loadPlayerData(event.getPlayer().getUniqueId());
        } catch (IOException e) {
            e.printStackTrace();
            playerRecordsMap.put(event.getPlayer().getUniqueId(), new PlayerRecords(new HashMap<>()));
        }
    }

    public boolean pushNewRecord(UUID playerUniqueID, UUID timerUniqueID, List<Long> timestamps) {
        if (!playerRecordsMap.containsKey(playerUniqueID))
            playerRecordsMap.put(playerUniqueID, new PlayerRecords());
        return playerRecordsMap.get(playerUniqueID).pushRecord(timerUniqueID, timestamps);
    }
}
