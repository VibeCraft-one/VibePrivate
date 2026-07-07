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

    public void rebuild(Collection<Region> regions) {
        Objects.requireNonNull(regions, "regions");
        regionsByOwner.clear();
        regionsByWorld.clear();

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
    }

    public void remove(Region region) {
        Objects.requireNonNull(region, "region");
        removeFrom(regionsByOwner, region.getOwnerId(), region.getId());
        removeFrom(regionsByWorld, region.getWorldName(), region.getId());
    }

    public List<Region> getByOwner(String ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        return values(regionsByOwner.get(ownerId));
    }

    public List<Region> getInWorld(String worldName) {
        Objects.requireNonNull(worldName, "worldName");
        return values(regionsByWorld.get(worldName));
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
