package com.vibeprivate.service;

import com.vibeprivate.model.Region;

import java.util.Objects;
import java.util.Optional;

public final class RegionCreationResult {
    private final RegionCreationStatus status;
    private final Region region;

    private RegionCreationResult(RegionCreationStatus status, Region region) {
        this.status = Objects.requireNonNull(status, "status");
        this.region = region;
    }

    public static RegionCreationResult success(Region region) {
        return new RegionCreationResult(RegionCreationStatus.SUCCESS, Objects.requireNonNull(region, "region"));
    }

    public static RegionCreationResult fail(RegionCreationStatus status) {
        if (status == RegionCreationStatus.SUCCESS) {
            throw new IllegalArgumentException("Use success() for successful creation.");
        }

        return new RegionCreationResult(status, null);
    }

    public RegionCreationStatus getStatus() {
        return status;
    }

    public Optional<Region> getRegion() {
        return Optional.ofNullable(region);
    }
}
