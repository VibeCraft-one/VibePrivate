package com.vibeprivate.service;

import com.vibeprivate.model.Region;

import java.util.Optional;

public interface RegionSelectionRegionStore {
    Optional<Region> getRegion(String regionId);
}
