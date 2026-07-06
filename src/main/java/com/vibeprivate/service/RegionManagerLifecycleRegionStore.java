package com.vibeprivate.service;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class RegionManagerLifecycleRegionStore implements RegionLifecycleRegionStore {
    private final RegionManager regionManager;

    public RegionManagerLifecycleRegionStore(RegionManager regionManager) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
    }

    @Override
    public Optional<Region> getRegion(String regionId) {
        return regionManager.getRegion(regionId);
    }

    @Override
    public Collection<Region> getRegions() {
        return regionManager.getRegions();
    }

    @Override
    public void saveRegion(Region region) {
        regionManager.saveRegion(region);
    }
}
