package com.vibeprivate.service;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class RegionManagerSelectionRegionStore implements RegionSelectionRegionStore {
    private final RegionManager regionManager;

    public RegionManagerSelectionRegionStore(RegionManager regionManager) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
    }

    @Override
    public Optional<Region> getRegion(String regionId) {
        return regionManager.getRegion(regionId);
    }

    @Override
    public Collection<Region> getRegionsInWorld(String worldName) {
        Objects.requireNonNull(worldName, "worldName");
        return regionManager.getRegionsInWorld(worldName);
    }
}
