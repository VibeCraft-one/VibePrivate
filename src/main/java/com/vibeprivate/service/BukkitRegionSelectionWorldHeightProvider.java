package com.vibeprivate.service;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Objects;
import java.util.Optional;

public final class BukkitRegionSelectionWorldHeightProvider implements RegionSelectionWorldHeightProvider {
    @Override
    public Optional<RegionSelectionWorldHeight> getWorldHeight(String worldName) {
        Objects.requireNonNull(worldName, "worldName");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return Optional.empty();
        }

        return Optional.of(new RegionSelectionWorldHeight(world.getMinHeight(), world.getMaxHeight()));
    }
}
