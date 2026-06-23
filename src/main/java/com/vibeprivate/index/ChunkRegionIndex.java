package com.vibeprivate.index;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ChunkRegionIndex {
    private final Map<String, Map<Long, Set<String>>> regionsByChunk = new HashMap<>();
    private final Map<String, Set<Long>> chunksByRegion = new HashMap<>();

    public void rebuild(Collection<Region> regions) {
        regionsByChunk.clear();
        chunksByRegion.clear();

        for (Region region : regions) {
            add(region);
        }
    }

    public void add(Region region) {
        Objects.requireNonNull(region, "region");
        Set<Long> chunks = collectChunkKeys(region);
        chunksByRegion.put(region.getId(), chunks);

        Map<Long, Set<String>> worldChunks = regionsByChunk.computeIfAbsent(region.getWorldName(), ignored -> new HashMap<>());
        for (Long chunkKey : chunks) {
            worldChunks.computeIfAbsent(chunkKey, ignored -> new HashSet<>()).add(region.getId());
        }
    }

    public void remove(Region region) {
        Objects.requireNonNull(region, "region");
        Set<Long> chunks = chunksByRegion.remove(region.getId());
        if (chunks == null) {
            return;
        }

        Map<Long, Set<String>> worldChunks = regionsByChunk.get(region.getWorldName());
        if (worldChunks == null) {
            return;
        }

        for (Long chunkKey : chunks) {
            Set<String> regionIds = worldChunks.get(chunkKey);
            if (regionIds == null) {
                continue;
            }

            regionIds.remove(region.getId());
            if (regionIds.isEmpty()) {
                worldChunks.remove(chunkKey);
            }
        }
    }

    public List<String> getRegionIds(String worldName, int chunkX, int chunkZ) {
        Objects.requireNonNull(worldName, "worldName");
        Map<Long, Set<String>> worldChunks = regionsByChunk.get(worldName);
        if (worldChunks == null) {
            return List.of();
        }

        Set<String> ids = worldChunks.get(chunkKey(chunkX, chunkZ));
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(ids);
    }

    public Set<Long> collectChunkKeys(Region region) {
        Objects.requireNonNull(region, "region");
        RegionBounds bounds = region.getBounds();
        Set<Long> chunks = new HashSet<>();

        for (int chunkX = bounds.getMinChunkX(); chunkX <= bounds.getMaxChunkX(); chunkX++) {
            for (int chunkZ = bounds.getMinChunkZ(); chunkZ <= bounds.getMaxChunkZ(); chunkZ++) {
                chunks.add(chunkKey(chunkX, chunkZ));
            }
        }

        return chunks;
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }
}
