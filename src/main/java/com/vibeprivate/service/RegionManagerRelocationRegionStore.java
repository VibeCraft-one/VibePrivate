package com.vibeprivate.service;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionHome;
import com.vibeprivate.storage.RegionHomeRepository;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class RegionManagerRelocationRegionStore implements RegionRelocationRegionStore {
    private final RegionManager regionManager;
    private final ConfigService configService;
    private final RegionHomeRepository regionHomeRepository;

    public RegionManagerRelocationRegionStore(RegionManager regionManager, ConfigService configService,
                                              RegionHomeRepository regionHomeRepository) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.regionHomeRepository = Objects.requireNonNull(regionHomeRepository, "regionHomeRepository");
    }

    @Override
    public Optional<Region> getRegion(String regionId) {
        return regionManager.getRegion(regionId);
    }

    @Override
    public Collection<Region> getRegionsInWorld(String worldName) {
        Objects.requireNonNull(worldName, "worldName");
        return regionManager.getRegions().stream()
                .filter(region -> region.getWorldName().equals(worldName))
                .toList();
    }

    @Override
    public boolean isAllowedWorld(String worldName) {
        Objects.requireNonNull(worldName, "worldName");
        return configService.getAllowedWorlds().contains(worldName);
    }

    @Override
    public void replaceRegion(Region region) {
        regionManager.replaceRegion(region);
    }

    @Override
    public Optional<RegionHome> getHome(String regionId) {
        return regionHomeRepository.getHome(regionId);
    }

    @Override
    public void saveHome(RegionHome home) {
        regionHomeRepository.saveHome(home);
    }
}
