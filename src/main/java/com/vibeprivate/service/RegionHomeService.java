package com.vibeprivate.service;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionHome;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.storage.RegionHomeRepository;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public final class RegionHomeService {
    private final RegionManager regionManager;
    private final RegionHomeRepository homeRepository;

    public RegionHomeService(RegionManager regionManager, RegionHomeRepository homeRepository) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.homeRepository = Objects.requireNonNull(homeRepository, "homeRepository");
    }

    public Optional<Region> getPlayerHomeRegion(Player player) {
        Objects.requireNonNull(player, "player");
        String ownerId = player.getUniqueId().toString();
        return regionManager.getRegionsByOwnerAndType(ownerId, RegionType.HOME).stream()
                .min(Comparator.comparing(Region::getCreatedAt));
    }

    public Optional<RegionHome> getHome(Region region) {
        Objects.requireNonNull(region, "region");
        return homeRepository.getHome(region.getId());
    }

    public boolean setHome(Player player) {
        Objects.requireNonNull(player, "player");
        Region region = regionManager.getRegionIncludingDisabledAtOrNull(player.getLocation());
        if (region == null || region.getType() != RegionType.HOME
                || !region.getOwnerId().equals(player.getUniqueId().toString())) {
            return false;
        }

        return setHome(player, region);
    }

    public boolean setHome(Player player, Region region) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null || !region.getBounds().contains(world.getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            return false;
        }

        homeRepository.saveHome(new RegionHome(
                region.getId(),
                world.getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        ));
        return true;
    }
}
