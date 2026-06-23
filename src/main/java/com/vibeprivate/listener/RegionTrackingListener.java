package com.vibeprivate.listener;

import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.visualization.RegionBoundaryVisualizer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public final class RegionTrackingListener implements Listener {
    private final RegionManager regionManager;
    private final PlayerRegionCache playerRegionCache;
    private final RegionBoundaryVisualizer boundaryVisualizer;

    public RegionTrackingListener(RegionManager regionManager, PlayerRegionCache playerRegionCache,
                                  RegionBoundaryVisualizer boundaryVisualizer) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.playerRegionCache = Objects.requireNonNull(playerRegionCache, "playerRegionCache");
        this.boundaryVisualizer = Objects.requireNonNull(boundaryVisualizer, "boundaryVisualizer");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        update(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerRegionCache.remove(event.getPlayer().getUniqueId());
        boundaryVisualizer.clear(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        update(event.getPlayer(), event.getPlayer().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        update(event.getPlayer(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null || !changedBlock(event.getFrom(), to)) {
            return;
        }

        update(event.getPlayer(), to);
    }

    private void update(Player player, Location location) {
        Region region = regionManager.getRegionAtOrNull(location);
        playerRegionCache.set(player.getUniqueId(), region);
        if (region != null) {
            boundaryVisualizer.showIfNearBoundary(player, region, location);
        }
    }

    private boolean changedBlock(Location from, Location to) {
        if (from.getWorld() != to.getWorld()) {
            return true;
        }

        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }
}
