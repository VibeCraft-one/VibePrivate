package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionHome;

import java.util.Collection;
import java.util.Optional;

public interface RegionRelocationRegionStore {
    Optional<Region> getRegion(String regionId);

    Collection<Region> getRegionsInWorld(String worldName);

    boolean isAllowedWorld(String worldName);

    void replaceRegion(Region region);

    Optional<RegionHome> getHome(String regionId);

    void saveHome(RegionHome home);
}
