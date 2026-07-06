package com.vibeprivate.service;

import com.vibeprivate.model.Region;

import java.util.Collection;
import java.util.Optional;

public interface RegionLifecycleRegionStore {
    Optional<Region> getRegion(String regionId);

    Collection<Region> getRegions();

    void saveRegion(Region region);
}
