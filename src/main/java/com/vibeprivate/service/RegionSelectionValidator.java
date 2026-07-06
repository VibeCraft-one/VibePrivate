package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.SelectionBounds;

import java.util.Objects;

public final class RegionSelectionValidator {
    private final RegionSelectionRegionStore regionStore;

    public RegionSelectionValidator(RegionSelectionRegionStore regionStore) {
        this.regionStore = Objects.requireNonNull(regionStore, "regionStore");
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

        return bounds.getMinX() >= regionBounds.getMinX()
                && bounds.getMaxX() <= regionBounds.getMaxX()
                && bounds.getMinY() >= regionBounds.getMinY()
                && bounds.getMaxY() <= regionBounds.getMaxY()
                && bounds.getMinZ() >= regionBounds.getMinZ()
                && bounds.getMaxZ() <= regionBounds.getMaxZ();
    }

    private Region requireRegion(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        return regionStore.getRegion(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown region id: " + regionId));
    }
}
