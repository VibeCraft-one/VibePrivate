package com.vibeprivate.model;

import java.util.Objects;

public final class RegionLifecycleState {
    public static final long INDEFINITE_PAUSE = Long.MAX_VALUE;

    private final String regionId;
    private final RegionStatus status;
    private final String statusReason;
    private final long statusChangedAt;
    private final Long upkeepPausedUntil;
    private final String upkeepPauseReason;

    public RegionLifecycleState(String regionId, RegionStatus status, String statusReason, long statusChangedAt,
                                Long upkeepPausedUntil, String upkeepPauseReason) {
        this.regionId = requireText(regionId, "regionId");
        this.status = Objects.requireNonNull(status, "status");
        this.statusReason = normalize(statusReason);
        this.statusChangedAt = Math.max(0L, statusChangedAt);
        this.upkeepPausedUntil = upkeepPausedUntil == null ? null : Math.max(0L, upkeepPausedUntil);
        this.upkeepPauseReason = normalize(upkeepPauseReason);
    }

    public String getRegionId() {
        return regionId;
    }

    public RegionStatus getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public long getStatusChangedAt() {
        return statusChangedAt;
    }

    public Long getUpkeepPausedUntil() {
        return upkeepPausedUntil;
    }

    public String getUpkeepPauseReason() {
        return upkeepPauseReason;
    }

    public boolean isUpkeepPaused(long now) {
        return upkeepPausedUntil != null && upkeepPausedUntil > Math.max(0L, now);
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }

        return value;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
