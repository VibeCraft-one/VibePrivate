package com.vibeprivate.service;

import com.vibeprivate.api.event.RegionArchiveEvent;
import com.vibeprivate.api.event.RegionRestoreEvent;
import com.vibeprivate.api.event.RegionSealEvent;
import com.vibeprivate.api.event.RegionStatusChangeEvent;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionLifecycleState;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.storage.RegionLifecycleRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class RegionLifecycleService {
    private static final String STATUS_PAUSE_PREFIX = "status:";

    private final RegionLifecycleRepository lifecycleRepository;
    private final RegionLifecycleRegionStore regionStore;
    private final RegionEventDispatcher eventDispatcher;
    private final Map<String, RegionLifecycleState> lifecycleStates = new HashMap<>();

    public RegionLifecycleService(RegionLifecycleRepository lifecycleRepository, RegionLifecycleRegionStore regionStore,
                                  RegionEventDispatcher eventDispatcher) {
        this.lifecycleRepository = Objects.requireNonNull(lifecycleRepository, "lifecycleRepository");
        this.regionStore = Objects.requireNonNull(regionStore, "regionStore");
        this.eventDispatcher = Objects.requireNonNull(eventDispatcher, "eventDispatcher");
    }

    public void load() {
        lifecycleStates.clear();
        lifecycleStates.putAll(lifecycleRepository.loadAll());
    }

    public RegionStatus getRegionStatus(String regionId) {
        return stateForExistingRegion(regionId).getStatus();
    }

    public Collection<Region> getRegionsInWorld(String worldName, boolean includeInactive) {
        Objects.requireNonNull(worldName, "worldName");
        return regionStore.getRegions().stream()
                .filter(region -> region.getWorldName().equals(worldName))
                .filter(region -> includeInactive || getRegionStatus(region.getId()) == RegionStatus.ACTIVE)
                .toList();
    }

    public void setRegionStatus(String regionId, RegionStatus status) {
        Objects.requireNonNull(status, "status");
        Region region = requireRegion(regionId);
        RegionLifecycleState current = stateForRegion(region);
        long now = System.currentTimeMillis();

        syncEnabledFlag(region, status, now);

        RegionLifecycleState updated = new RegionLifecycleState(
                regionId,
                status,
                current.getStatusReason(),
                now,
                pausedUntilForStatus(status, current),
                pauseReasonForStatus(status, current)
        );
        saveLifecycle(updated);
        publishStatusEvents(region, current.getStatus(), updated);
    }

    public void pauseUpkeep(String regionId, String reason) {
        String normalizedReason = requireReason(reason);
        Region region = requireRegion(regionId);
        RegionLifecycleState current = stateForRegion(region);
        RegionLifecycleState updated = new RegionLifecycleState(
                regionId,
                current.getStatus(),
                current.getStatusReason(),
                current.getStatusChangedAt(),
                RegionLifecycleState.INDEFINITE_PAUSE,
                normalizedReason
        );
        saveLifecycle(updated);
    }

    public void resumeUpkeep(String regionId, String reason) {
        requireReason(reason);
        RegionLifecycleState current = stateForExistingRegion(regionId);
        if (current.getStatus() == RegionStatus.SEALED || current.getStatus() == RegionStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot resume upkeep while region status is " + current.getStatus().name());
        }

        RegionLifecycleState updated = new RegionLifecycleState(
                regionId,
                current.getStatus(),
                current.getStatusReason(),
                current.getStatusChangedAt(),
                null,
                null
        );
        saveLifecycle(updated);
    }

    public boolean isUpkeepPaused(String regionId) {
        return stateForExistingRegion(regionId).isUpkeepPaused(System.currentTimeMillis());
    }

    private RegionLifecycleState stateForExistingRegion(String regionId) {
        return stateForRegion(requireRegion(regionId));
    }

    private RegionLifecycleState stateForRegion(Region region) {
        RegionLifecycleState stored = lifecycleStates.get(region.getId());
        if (stored != null) {
            return stored;
        }

        return new RegionLifecycleState(
                region.getId(),
                region.isEnabled() ? RegionStatus.ACTIVE : RegionStatus.INACTIVE,
                null,
                region.getCreatedAt(),
                null,
                null
        );
    }

    private Region requireRegion(String regionId) {
        return regionStore.getRegion(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown region id: " + regionId));
    }

    private void syncEnabledFlag(Region region, RegionStatus status, long now) {
        boolean shouldEnable = status == RegionStatus.ACTIVE;
        if (region.isEnabled() == shouldEnable) {
            return;
        }

        region.setEnabled(shouldEnable);
        if (shouldEnable) {
            region.setFuelEmptySince(0L);
        } else if (region.getFuelEmptySince() == 0L) {
            region.setFuelEmptySince(now);
        }
        regionStore.saveRegion(region);
    }

    private Long pausedUntilForStatus(RegionStatus status, RegionLifecycleState current) {
        if (status == RegionStatus.SEALED || status == RegionStatus.ARCHIVED) {
            return RegionLifecycleState.INDEFINITE_PAUSE;
        }

        if (isStatusPause(current.getUpkeepPauseReason())) {
            return null;
        }

        return current.getUpkeepPausedUntil();
    }

    private String pauseReasonForStatus(RegionStatus status, RegionLifecycleState current) {
        if (status == RegionStatus.SEALED || status == RegionStatus.ARCHIVED) {
            return STATUS_PAUSE_PREFIX + status.name();
        }

        if (isStatusPause(current.getUpkeepPauseReason())) {
            return null;
        }

        return current.getUpkeepPauseReason();
    }

    private boolean isStatusPause(String pauseReason) {
        return pauseReason != null && pauseReason.startsWith(STATUS_PAUSE_PREFIX);
    }

    private void saveLifecycle(RegionLifecycleState state) {
        lifecycleStates.put(state.getRegionId(), state);
        lifecycleRepository.save(state);
    }

    private void publishStatusEvents(Region region, RegionStatus previousStatus, RegionLifecycleState updated) {
        RegionStatus newStatus = updated.getStatus();
        if (previousStatus == newStatus) {
            return;
        }

        long changedAt = updated.getStatusChangedAt();
        eventDispatcher.dispatch(new RegionStatusChangeEvent(region.getId(), region.getType(), region.getOwnerId(),
                previousStatus, newStatus, changedAt));
        if (newStatus == RegionStatus.SEALED) {
            eventDispatcher.dispatch(new RegionSealEvent(region.getId(), region.getType(), region.getOwnerId(),
                    previousStatus, newStatus, changedAt));
        }
        if (newStatus == RegionStatus.ARCHIVED) {
            eventDispatcher.dispatch(new RegionArchiveEvent(region.getId(), region.getType(), region.getOwnerId(),
                    previousStatus, newStatus, changedAt));
        }
        if (previousStatus == RegionStatus.ARCHIVED && newStatus != RegionStatus.ARCHIVED) {
            eventDispatcher.dispatch(new RegionRestoreEvent(region.getId(), region.getType(), region.getOwnerId(),
                    previousStatus, newStatus, changedAt));
        }
    }

    private String requireReason(String reason) {
        Objects.requireNonNull(reason, "reason");
        String normalized = reason.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("reason must not be blank.");
        }

        return normalized;
    }
}
