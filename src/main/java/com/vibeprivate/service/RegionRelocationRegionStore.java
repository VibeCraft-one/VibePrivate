package com.vibeprivate.service;

import com.vibeprivate.model.Region;

import java.util.Collection;
import java.util.Optional;

public interface RegionRelocationRegionStore {
    Optional<Region> getRegion(String regionId);

    Collection<Region> getRegionsInWorld(String worldName);

    boolean isAllowedWorld(String worldName);

    void replaceRegion(Region region);
}
