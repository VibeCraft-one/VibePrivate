package com.vibeprivate.service;

import com.vibeprivate.model.Region;

import java.util.Objects;
import java.util.Optional;

public final class AdminRegionResult {
    private final AdminRegionStatus status;
    private final Region region;

    private AdminRegionResult(AdminRegionStatus status, Region region) {
        this.status = Objects.requireNonNull(status, "status");
        this.region = region;
    }

    public static AdminRegionResult success(Region region) {
        return new AdminRegionResult(AdminRegionStatus.SUCCESS, Objects.requireNonNull(region, "region"));
    }

    public static AdminRegionResult fail(AdminRegionStatus status) {
        if (status == AdminRegionStatus.SUCCESS) {
            throw new IllegalArgumentException("Use success() for successful admin region creation.");
        }

        return new AdminRegionResult(status, null);
    }

    public AdminRegionStatus getStatus() {
        return status;
    }

    public Optional<Region> getRegion() {
        return Optional.ofNullable(region);
    }
}
