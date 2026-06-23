package com.vibeprivate.service;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AdminRegionService {
    private final RegionManager regionManager;
    private final RegionAccessService regionAccessService;
    private final Map<UUID, Location> pos1ByPlayer = new HashMap<>();
    private final Map<UUID, Location> pos2ByPlayer = new HashMap<>();

    public AdminRegionService(RegionManager regionManager, RegionAccessService regionAccessService) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
    }

    public void setPos1(Player player) {
        Objects.requireNonNull(player, "player");
        pos1ByPlayer.put(player.getUniqueId(), copyBlockLocation(player.getLocation()));
    }

    public void setPos2(Player player) {
        Objects.requireNonNull(player, "player");
        pos2ByPlayer.put(player.getUniqueId(), copyBlockLocation(player.getLocation()));
    }

    public AdminRegionResult create(Player player, String name) {
        Objects.requireNonNull(player, "player");
        String safeName = Objects.requireNonNullElse(name, "").trim();
        if (safeName.isBlank()) {
            safeName = "ADMIN";
        }

        Location pos1 = pos1ByPlayer.get(player.getUniqueId());
        Location pos2 = pos2ByPlayer.get(player.getUniqueId());
        if (pos1 == null || pos2 == null) {
            return AdminRegionResult.fail(AdminRegionStatus.MISSING_POSITIONS);
        }

        World world1 = pos1.getWorld();
        World world2 = pos2.getWorld();
        if (world1 == null || world2 == null || !world1.getName().equals(world2.getName())) {
            return AdminRegionResult.fail(AdminRegionStatus.DIFFERENT_WORLDS);
        }

        Region region = Region.adminRegion(UUID.randomUUID().toString(), safeName,
                        player.getUniqueId().toString(), world1.getName())
                .cuboid(
                        pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                        pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()
                )
                .build();

        try {
            regionManager.addRegion(region);
            clear(player);
            return AdminRegionResult.success(region);
        } catch (RuntimeException exception) {
            return AdminRegionResult.fail(AdminRegionStatus.FAILED);
        }
    }

    public AdminRegionResult createNoClaim(Player player, int radius) {
        Objects.requireNonNull(player, "player");
        if (radius <= 0) {
            return AdminRegionResult.fail(AdminRegionStatus.INVALID_RADIUS);
        }

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return AdminRegionResult.fail(AdminRegionStatus.FAILED);
        }

        Region region = Region.adminRegion(UUID.randomUUID().toString(), "NO_CLAIM_" + radius,
                        player.getUniqueId().toString(), world.getName())
                .cuboid(
                        location.getBlockX() - radius,
                        world.getMinHeight(),
                        location.getBlockZ() - radius,
                        location.getBlockX() + radius,
                        world.getMaxHeight(),
                        location.getBlockZ() + radius
                )
                .build();

        try {
            regionManager.addRegion(region);
            regionAccessService.setDefaultFlag(region.getId(), RegionFlag.NO_CLAIM, true);
            return AdminRegionResult.success(region);
        } catch (RuntimeException exception) {
            return AdminRegionResult.fail(AdminRegionStatus.FAILED);
        }
    }

    private void clear(Player player) {
        pos1ByPlayer.remove(player.getUniqueId());
        pos2ByPlayer.remove(player.getUniqueId());
    }

    private Location copyBlockLocation(Location location) {
        World world = location.getWorld();
        return new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
