package cat.nyaa.nfs;

import cat.nyaa.nfs.dataclasses.Point;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Marker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.min;

public class GuidanceService implements Listener {
    private final Map<UUID, Marker> guidanceMap = new HashMap<>();

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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        if (!guidanceMap.containsKey(player.getUniqueId())) return;
        var guidanceMarker = guidanceMap.get(event.getPlayer().getUniqueId());

        player.spawnParticle(Particle.HAPPY_VILLAGER, guidanceMarker.getLocation(), 10);

        if (!player.isGliding()) return;
        var playerLocation = player.getLocation();
        var playerSpeed = player.getVelocity().length();
        var playerDistance = playerLocation.distance(guidanceMarker.getLocation());

        if (playerSpeed > 0.15 && playerDistance > 5) {
            var locInFront = playerLocation.clone().add(playerLocation.getDirection().multiply(5));
            var vibrationSpeed = min(playerDistance / playerSpeed / 1.5, playerDistance / 2);
            var data = new Vibration(locInFront, new Vibration.Destination.BlockDestination(guidanceMarker.getLocation().getBlock()), (int) (vibrationSpeed));
            player.spawnParticle(Particle.VIBRATION, locInFront, 1, data);
        }
    }

}
