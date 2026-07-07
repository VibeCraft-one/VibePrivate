package com.vibeprivate.api.event;

import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public final class RegionArchiveEvent extends AbstractRegionEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final RegionStatus previousStatus;
    private final RegionStatus newStatus;
    private final long changedAt;

    public RegionArchiveEvent(String regionId, RegionType regionType, String ownerId,
                              RegionStatus previousStatus, RegionStatus newStatus, long changedAt) {
        super(regionId, regionType, ownerId);
        this.previousStatus = Objects.requireNonNull(previousStatus, "previousStatus");
        this.newStatus = Objects.requireNonNull(newStatus, "newStatus");
        this.changedAt = Math.max(0L, changedAt);
    }

    public RegionStatus getPreviousStatus() {
        return previousStatus;
    }

    public RegionStatus getNewStatus() {
        return newStatus;
    }

    public long getChangedAt() {
        return changedAt;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
