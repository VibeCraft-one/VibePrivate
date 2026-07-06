package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.SelectionBounds;

import java.util.Objects;

public final class RegionSelectionValidator {
    private final RegionSelectionRegionStore regionStore;
    private final RegionSelectionWorldHeightProvider worldHeightProvider;

    public RegionSelectionValidator(RegionSelectionRegionStore regionStore,
                                    RegionSelectionWorldHeightProvider worldHeightProvider) {
        this.regionStore = Objects.requireNonNull(regionStore, "regionStore");
        this.worldHeightProvider = Objects.requireNonNull(worldHeightProvider, "worldHeightProvider");
    }

    public RegionBounds getRegionBounds(String regionId) {
        return requireRegion(regionId).getBounds();
    }

    public boolean isAreaInsideRegion(String regionId, SelectionBounds bounds) {
        if (bounds == null || bounds.isEmpty()) {
            return false;
        }

        RegionBounds regionBounds = requireRegion(regionId).getBounds();
        if (!regionBounds.getWorldName().equals(bounds.getWorldName())) {
            return false;
        }

        if (!isInsideRegionBounds(regionBounds, bounds)) {
            return false;
        }

        if (!isWithinWorldHeight(bounds)) {
            return false;
        }

        return !overlapsForeignRegion(regionId, bounds);
    }

    private boolean isInsideRegionBounds(RegionBounds regionBounds, SelectionBounds bounds) {
        return bounds.getMinX() >= regionBounds.getMinX()
                && bounds.getMaxX() <= regionBounds.getMaxX()
                && bounds.getMinY() >= regionBounds.getMinY()
                && bounds.getMaxY() <= regionBounds.getMaxY()
                && bounds.getMinZ() >= regionBounds.getMinZ()
                && bounds.getMaxZ() <= regionBounds.getMaxZ();
    }

    private boolean isWithinWorldHeight(SelectionBounds bounds) {
        return worldHeightProvider.getWorldHeight(bounds.getWorldName())
                .map(worldHeight -> bounds.getMinY() >= worldHeight.minY()
                        && bounds.getMaxY() <= worldHeight.maxY())
                .orElse(false);
    }

    private boolean overlapsForeignRegion(String regionId, SelectionBounds bounds) {
        RegionBounds selectionBounds = new RegionBounds(bounds.getWorldName(), bounds.getMinX(), bounds.getMaxX(),
                bounds.getMinY(), bounds.getMaxY(), bounds.getMinZ(), bounds.getMaxZ());

        return regionStore.getRegionsInWorld(bounds.getWorldName()).stream()
                .filter(region -> !region.isAdmin())
                .filter(region -> !region.getId().equals(regionId))
                .anyMatch(region -> region.getBounds().intersects(selectionBounds));
    }

    private Region requireRegion(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        return regionStore.getRegion(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown region id: " + regionId));
    }
}
