package com.vibeprivate.service;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;

import java.util.Objects;
import java.util.Optional;

public final class RegionManagerClanRegionManagementRegionStore implements ClanRegionManagementRegionStore {
    private final RegionManager regionManager;

    public RegionManagerClanRegionManagementRegionStore(RegionManager regionManager) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
    }

    @Override
    public Optional<Region> getRegion(String regionId) {
        return regionManager.getRegion(regionId);
    }
}
