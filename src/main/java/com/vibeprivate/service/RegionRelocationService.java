package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionHome;
import com.vibeprivate.model.RegionShape;

import java.util.Objects;

public final class RegionRelocationService {
    private final RegionRelocationRegionStore regionStore;

    public RegionRelocationService(RegionRelocationRegionStore regionStore) {
        this.regionStore = Objects.requireNonNull(regionStore, "regionStore");
    }

    public boolean canMoveRegionToWorld(String regionId, String targetWorld) {
        try {
            Region region = requireRegion(regionId);
            validateTargetWorld(region, targetWorld);
            return !hasForeignNonAdminOverlap(buildMovedRegion(region, targetWorld));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public Region moveRegionToWorldSameBounds(String regionId, String targetWorld) {
        Region region = requireRegion(regionId);
        validateTargetWorld(region, targetWorld);
        Region moved = buildMovedRegion(region, targetWorld);
        ensureNoForeignNonAdminOverlap(moved);
        if (moved.getWorldName().equals(region.getWorldName())) {
            return region;
        }

        regionStore.replaceRegion(moved);
        remapHomeWorldIfNeeded(region, moved);
        return moved;
    }

    public boolean canRelocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ) {
        try {
            Region region = requireRegion(regionId);
            validateRelocationCandidate(region, targetWorld);
            return !hasForeignNonAdminOverlap(buildRelocatedRegion(region, targetWorld, targetCenterX, targetCenterZ));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public Region relocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ) {
        Region region = requireRegion(regionId);
        validateRelocationCandidate(region, targetWorld);
        Region relocated = buildRelocatedRegion(region, targetWorld, targetCenterX, targetCenterZ);
        ensureNoForeignNonAdminOverlap(relocated);
        if (relocated.getWorldName().equals(region.getWorldName())
                && Objects.equals(relocated.getCenterX(), region.getCenterX())
                && Objects.equals(relocated.getCenterZ(), region.getCenterZ())) {
            return region;
        }

        regionStore.replaceRegion(relocated);
        return relocated;
    }

    private void validateTargetWorld(Region region, String targetWorld) {
        requireWorldName(targetWorld);
        if (!region.isAdmin() && !regionStore.isAllowedWorld(targetWorld)) {
            throw new IllegalArgumentException("World is not allowed for player regions: " + targetWorld);
        }
    }

    private void validateRelocationCandidate(Region region, String targetWorld) {
        requireWorldName(targetWorld);
        if (region.isAdmin()) {
            throw new UnsupportedOperationException("ADMIN regions are not supported by relocateRegion.");
        }

        if (region.getShape() != RegionShape.RADIUS) {
            throw new UnsupportedOperationException("Only radius regions are supported by relocateRegion.");
        }

        if (!regionStore.isAllowedWorld(targetWorld)) {
            throw new IllegalArgumentException("World is not allowed for player regions: " + targetWorld);
        }
    }

    private Region requireRegion(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        return regionStore.getRegion(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown region id: " + regionId));
    }

    private Region buildMovedRegion(Region region, String targetWorld) {
        if (region.getShape() == RegionShape.RADIUS) {
            return Region.radiusRegion(region.getId(), region.getName(), region.getType(), region.getOwnerId(), targetWorld)
                    .radius(requireInt(region.getCenterX(), "centerX"), requireInt(region.getCenterZ(), "centerZ"),
                            requireInt(region.getRadius(), "radius"), region.getMinY(), region.getMaxY())
                    .state(region.isEnabled(), region.getFuelExpiresAt(), region.getFuelEmptySince(),
                            region.getLastFuelDrainAt(), region.getUpgradeLevel(), region.getVisualizationMode(),
                            region.getCreatedAt())
                    .build();
        }

        return Region.adminRegion(region.getId(), region.getName(), region.getOwnerId(), targetWorld)
                .cuboid(requireInt(region.getPos1X(), "pos1X"), requireInt(region.getPos1Y(), "pos1Y"),
                        requireInt(region.getPos1Z(), "pos1Z"), requireInt(region.getPos2X(), "pos2X"),
                        requireInt(region.getPos2Y(), "pos2Y"), requireInt(region.getPos2Z(), "pos2Z"))
                .state(region.isEnabled(), region.getFuelExpiresAt(), region.getFuelEmptySince(),
                        region.getLastFuelDrainAt(), region.getUpgradeLevel(), region.getVisualizationMode(),
                        region.getCreatedAt())
                .build();
    }

    private Region buildRelocatedRegion(Region region, String targetWorld, int targetCenterX, int targetCenterZ) {
        return Region.radiusRegion(region.getId(), region.getName(), region.getType(), region.getOwnerId(), targetWorld)
                .radius(targetCenterX, targetCenterZ, requireInt(region.getRadius(), "radius"),
                        region.getMinY(), region.getMaxY())
                .state(region.isEnabled(), region.getFuelExpiresAt(), region.getFuelEmptySince(),
                        region.getLastFuelDrainAt(), region.getUpgradeLevel(), region.getVisualizationMode(),
                        region.getCreatedAt())
                .build();
    }

    private boolean hasForeignNonAdminOverlap(Region candidate) {
        if (candidate.isAdmin()) {
            return false;
        }

        RegionBounds candidateBounds = candidate.getBounds();
        return regionStore.getRegionsInWorld(candidate.getWorldName()).stream()
                .filter(existing -> !existing.isAdmin())
                .filter(existing -> !existing.getId().equals(candidate.getId()))
                .anyMatch(existing -> existing.getBounds().intersects(candidateBounds));
    }

    private void ensureNoForeignNonAdminOverlap(Region candidate) {
        if (hasForeignNonAdminOverlap(candidate)) {
            throw new IllegalStateException("Region overlaps another non-admin region.");
        }
    }

    private void remapHomeWorldIfNeeded(Region original, Region moved) {
        if (moved.getWorldName().equals(original.getWorldName())) {
            return;
        }

        regionStore.getHome(original.getId())
                .filter(home -> !moved.getWorldName().equals(home.worldName()))
                .ifPresent(home -> regionStore.saveHome(remapHomeWorld(home, moved.getWorldName())));
    }

    private static RegionHome remapHomeWorld(RegionHome home, String targetWorld) {
        return new RegionHome(home.regionId(), targetWorld, home.x(), home.y(), home.z(), home.yaw(), home.pitch());
    }

    private static String requireWorldName(String worldName) {
        Objects.requireNonNull(worldName, "targetWorld");
        if (worldName.isBlank()) {
            throw new IllegalArgumentException("targetWorld must not be blank.");
        }

        return worldName;
    }

    private static int requireInt(Integer value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }

        return value;
    }
}
