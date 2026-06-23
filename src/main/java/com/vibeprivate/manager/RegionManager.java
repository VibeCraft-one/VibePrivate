package com.vibeprivate.manager;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.index.ChunkRegionIndex;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.service.ChunkProtectionService;
import com.vibeprivate.storage.RegionRepository;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class RegionManager {
    private final RegionRepository regionRepository;
    private final ConfigService configService;
    private final ChunkRegionIndex chunkIndex = new ChunkRegionIndex();
    private final Map<String, Region> regionsById = new HashMap<>();
    private ChunkProtectionService chunkProtectionService;

    public RegionManager(RegionRepository regionRepository, ConfigService configService) {
        this.regionRepository = Objects.requireNonNull(regionRepository, "regionRepository");
        this.configService = Objects.requireNonNull(configService, "configService");
    }

    public void load() {
        regionsById.clear();

        for (Region region : regionRepository.loadAll()) {
            regionsById.put(region.getId(), region);
        }

        chunkIndex.rebuild(regionsById.values());
    }

    public void setChunkProtectionService(ChunkProtectionService chunkProtectionService) {
        this.chunkProtectionService = chunkProtectionService;
    }

    public void addRegion(Region region) {
        Objects.requireNonNull(region, "region");

        if (regionsById.containsKey(region.getId())) {
            throw new IllegalArgumentException("Region with id '" + region.getId() + "' already exists.");
        }

        validateWorld(region);
        validateChunkOverlap(region);
        regionRepository.save(region);
        regionsById.put(region.getId(), region);
        chunkIndex.add(region);
        protectChunks(region);
    }

    public Optional<Region> removeRegion(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        Region removed = regionsById.remove(regionId);
        if (removed == null) {
            return Optional.empty();
        }

        chunkIndex.remove(removed);
        unprotectChunks(removed);
        regionRepository.delete(regionId);
        return Optional.of(removed);
    }

    public void saveRegion(Region region) {
        Objects.requireNonNull(region, "region");

        if (!regionsById.containsKey(region.getId())) {
            throw new IllegalArgumentException("Unknown region id: " + region.getId());
        }

        regionRepository.save(region);
    }

    public void replaceRegion(Region region) {
        Objects.requireNonNull(region, "region");
        Region oldRegion = regionsById.get(region.getId());
        if (oldRegion == null) {
            throw new IllegalArgumentException("Unknown region id: " + region.getId());
        }

        chunkIndex.remove(oldRegion);
        try {
            validateWorld(region);
            validateChunkOverlap(region);
            regionRepository.save(region);
            regionsById.put(region.getId(), region);
            chunkIndex.add(region);
            protectChunks(region);
        } catch (RuntimeException exception) {
            chunkIndex.add(oldRegion);
            throw exception;
        }
    }

    public Optional<Region> getRegion(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        return Optional.ofNullable(regionsById.get(regionId));
    }

    public Optional<Region> getRegionAt(Location location) {
        return Optional.ofNullable(getRegionAtOrNull(location));
    }

    public Region getRegionAtOrNull(Location location) {
        Objects.requireNonNull(location, "location");
        World world = location.getWorld();
        if (world == null) {
            return null;
        }

        return getRegionAtOrNull(
                world.getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    public Region getRegionAtOrNull(String worldName, int x, int y, int z) {
        Objects.requireNonNull(worldName, "worldName");
        return getRegionAtOrNull(worldName, x, y, z, false);
    }

    public Region getRegionIncludingDisabledAtOrNull(Location location) {
        Objects.requireNonNull(location, "location");
        World world = location.getWorld();
        if (world == null) {
            return null;
        }

        return getRegionAtOrNull(world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), true);
    }

    private Region getRegionAtOrNull(String worldName, int x, int y, int z, boolean includeDisabled) {
        Objects.requireNonNull(worldName, "worldName");
        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);
        Region bestRegion = null;
        int bestPriority = 0;

        for (String regionId : chunkIndex.getRegionIds(worldName, chunkX, chunkZ)) {
            Region region = regionsById.get(regionId);
            if (region == null) {
                continue;
            }

            boolean contains = includeDisabled
                    ? region.getBounds().contains(worldName, x, y, z)
                    : region.contains(worldName, x, y, z);
            if (!contains) {
                continue;
            }

            int priority = getPriority(region.getType());
            if (priority > bestPriority || priority == bestPriority && isBetterSamePriority(region, bestRegion)) {
                bestRegion = region;
                bestPriority = priority;
            }
        }

        return bestRegion;
    }

    public List<Region> getRegionsByOwner(String ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        return regionsById.values().stream()
                .filter(region -> region.getOwnerId().equals(ownerId))
                .sorted(Comparator.comparing(Region::getType).thenComparing(Region::getName))
                .toList();
    }

    public List<Region> getRegionsByOwnerAndType(String ownerId, RegionType type) {
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(type, "type");
        return regionsById.values().stream()
                .filter(region -> region.getOwnerId().equals(ownerId))
                .filter(region -> region.getType() == type)
                .sorted(Comparator.comparing(Region::getName))
                .toList();
    }

    public Collection<Region> getRegions() {
        return List.copyOf(regionsById.values());
    }

    private void validateWorld(Region region) {
        if (region.isAdmin()) {
            return;
        }

        if (!configService.getAllowedWorlds().contains(region.getWorldName())) {
            throw new IllegalArgumentException("World is not allowed for player regions: " + region.getWorldName());
        }
    }

    private void validateChunkOverlap(Region region) {
        if (region.isAdmin()) {
            return;
        }

        for (long chunk : chunkIndex.collectChunkKeys(region)) {
            if (!getNonAdminRegionsInChunk(region, chunk).isEmpty()) {
                throw new IllegalArgumentException("Region touches chunks already occupied by another region.");
            }
        }
    }

    private List<Region> getNonAdminRegionsInChunk(Region region, long chunkKey) {
        int chunkX = (int) (chunkKey >> 32);
        int chunkZ = (int) chunkKey;

        return chunkIndex.getRegionIds(region.getWorldName(), chunkX, chunkZ).stream()
                .map(regionsById::get)
                .filter(Objects::nonNull)
                .filter(existing -> !existing.isAdmin())
                .filter(existing -> !existing.getId().equals(region.getId()))
                .toList();
    }

    private int getPriority(RegionType type) {
        return switch (type) {
            case ADMIN -> 4;
            case CLAN -> 3;
            case HOME -> 2;
            case FARM -> 1;
        };
    }

    private boolean isBetterSamePriority(Region candidate, Region current) {
        if (current == null) {
            return true;
        }

        if (candidate.isAdmin() && current.isAdmin()) {
            return candidate.getBounds().getBlockVolume() < current.getBounds().getBlockVolume();
        }

        return false;
    }

    private void protectChunks(Region region) {
        if (chunkProtectionService != null) {
            chunkProtectionService.protectRegion(region);
        }
    }

    private void unprotectChunks(Region region) {
        if (chunkProtectionService != null) {
            chunkProtectionService.unprotectRegion(region);
        }
    }
}
