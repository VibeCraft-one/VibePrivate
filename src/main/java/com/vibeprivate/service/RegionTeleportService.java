package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class RegionTeleportService {
    public boolean teleportHome(Player player, Region region) {
        return teleportHome(player, region, null);
    }

    public boolean teleportHome(Player player, Region region, RegionHome home) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");
        World world = Bukkit.getWorld(region.getWorldName());
        if (world == null) {
            return false;
        }

        if (home != null && home.worldName().equals(world.getName())) {
            Location location = new Location(world, home.x(), home.y(), home.z(), home.yaw(), home.pitch());
            if (region.getBounds().contains(world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                return player.teleport(location);
            }
        }

        int x = region.getCenterX() != null
                ? region.getCenterX()
                : Math.floorDiv(region.getBounds().getMinX() + region.getBounds().getMaxX(), 2);
        int z = region.getCenterZ() != null
                ? region.getCenterZ()
                : Math.floorDiv(region.getBounds().getMinZ() + region.getBounds().getMaxZ(), 2);
        int y = world.getHighestBlockYAt(x, z) + 1;
        return player.teleport(new Location(world, x + 0.5D, y, z + 0.5D));
    }
}
