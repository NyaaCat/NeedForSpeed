package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.Point;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.lang.Math.min;

public class GuidanceService implements Listener {
    private final Map<UUID, Marker> guidanceMap = new HashMap<>();
    public static final NamespacedKey guidancePreferenceKey = new NamespacedKey(NeedForSpeed.instance, "guidance");

    public void updateGuidance(UUID playerUniqueID, Point location, String world) {
        removeGuidance(playerUniqueID);

        var bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) return;
        var marker = (Marker) bukkitWorld.spawnEntity(new Location(bukkitWorld, location.x, location.y, location.z), EntityType.MARKER);
        guidanceMap.put(playerUniqueID, marker);
    }

    public void removeGuidance(UUID playerUniqueID) {
        var marker = guidanceMap.remove(playerUniqueID);
        if (marker != null) {
            marker.remove();
        }
    }

    public void updatePlayerPreference(Player player, GuidanceLevel guidanceLevel) {
        player.getPersistentDataContainer().set(guidancePreferenceKey, PersistentDataType.INTEGER, guidanceLevel.ordinal());
    }

    public GuidanceLevel getGuidanceLevel(Player player) {
        if (!player.getPersistentDataContainer().has(guidancePreferenceKey))
            return GuidanceLevel.ON;
        var ordinal = player.getPersistentDataContainer().get(guidancePreferenceKey, PersistentDataType.INTEGER);
        return GuidanceLevel.values()[Objects.requireNonNullElseGet(ordinal, GuidanceLevel.ON::ordinal)];

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        var guidanceLevel = getGuidanceLevel(event.getPlayer());
        if (guidanceLevel == GuidanceLevel.OFF) return;
        var player = event.getPlayer();
        if (!guidanceMap.containsKey(player.getUniqueId())) return;
        var guidanceMarker = guidanceMap.get(event.getPlayer().getUniqueId());

        player.spawnParticle(Particle.HAPPY_VILLAGER, guidanceMarker.getLocation(), 10);

        if (!player.isGliding()) return;
        if (guidanceLevel != GuidanceLevel.ON) return;
        var playerLocation = player.getLocation();
        var playerSpeed = player.getVelocity().length();
        var playerDistance = playerLocation.distance(guidanceMarker.getLocation());

        if (playerSpeed > 0.15 && playerDistance > 8) {
            var locInFront = playerLocation.clone().add(playerLocation.getDirection().multiply(8));
            var vibrationSpeed = min(playerDistance / playerSpeed / 1.5, playerDistance / 2);
            var data = new Vibration(locInFront, new Vibration.Destination.BlockDestination(guidanceMarker.getLocation().getBlock()), (int) (vibrationSpeed));
            player.spawnParticle(Particle.VIBRATION, locInFront, 3, data);
        }
    }

}
