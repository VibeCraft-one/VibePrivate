package com.vibeprivate.index;

import com.vibeprivate.model.Region;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RegionLookupIndex {
    private final Map<String, Map<String, Region>> regionsByOwner = new LinkedHashMap<>();
    private final Map<String, Map<String, Region>> regionsByWorld = new LinkedHashMap<>();
    private final Map<String, Region> adminRegions = new LinkedHashMap<>();
    private final Map<String, Region> playerRegions = new LinkedHashMap<>();
    private final Map<String, Map<String, Region>> playerRegionsByOwner = new LinkedHashMap<>();

    public void rebuild(Collection<Region> regions) {
        Objects.requireNonNull(regions, "regions");
        regionsByOwner.clear();
        regionsByWorld.clear();
        adminRegions.clear();
        playerRegions.clear();
        playerRegionsByOwner.clear();

        for (Region region : regions) {
            add(region);
        }
    }

    public void add(Region region) {
        Objects.requireNonNull(region, "region");
        regionsByOwner.computeIfAbsent(region.getOwnerId(), ignored -> new LinkedHashMap<>())
                .put(region.getId(), region);
        regionsByWorld.computeIfAbsent(region.getWorldName(), ignored -> new LinkedHashMap<>())
                .put(region.getId(), region);
        if (region.isAdmin()) {
            adminRegions.put(region.getId(), region);
            return;
        }

        playerRegions.put(region.getId(), region);
        playerRegionsByOwner.computeIfAbsent(region.getOwnerId(), ignored -> new LinkedHashMap<>())
                .put(region.getId(), region);
    }

    public void remove(Region region) {
        Objects.requireNonNull(region, "region");
        removeFrom(regionsByOwner, region.getOwnerId(), region.getId());
        removeFrom(regionsByWorld, region.getWorldName(), region.getId());
        if (region.isAdmin()) {
            adminRegions.remove(region.getId());
            return;
        }

        playerRegions.remove(region.getId());
        removeFrom(playerRegionsByOwner, region.getOwnerId(), region.getId());
    }

    public List<Region> getByOwner(String ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        return values(regionsByOwner.get(ownerId));
    }

    public List<Region> getInWorld(String worldName) {
        Objects.requireNonNull(worldName, "worldName");
        return values(regionsByWorld.get(worldName));
    }

    public List<Region> getAdminRegions() {
        return values(adminRegions);
    }

    public int getAdminRegionCount() {
        return adminRegions.size();
    }

    public int getPlayerRegionCount() {
        return playerRegions.size();
    }

    public List<Region> getPlayerRegions() {
        return values(playerRegions);
    }

    public List<String> getPlayerOwnerIds() {
        return List.copyOf(playerRegionsByOwner.keySet());
    }

    public List<Region> getPlayerRegionsByOwner(String ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        return values(playerRegionsByOwner.get(ownerId));
    }

    private void removeFrom(Map<String, Map<String, Region>> index, String key, String regionId) {
        Map<String, Region> regions = index.get(key);
        if (regions == null) {
            return;
        }

        regions.remove(regionId);
        if (regions.isEmpty()) {
            index.remove(key);
        }
    }

    private List<Region> values(Map<String, Region> regions) {
        if (regions == null || regions.isEmpty()) {
            return List.of();
        }

        return List.copyOf(regions.values());
    }
}
