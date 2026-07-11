package com.vibeprivate.api.event;

import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionType;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public final class RegionRelocateEvent extends AbstractRegionEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final RegionBounds previousBounds;
    private final RegionBounds newBounds;

    public RegionRelocateEvent(String regionId, RegionType regionType, String ownerId,
                               RegionBounds previousBounds, RegionBounds newBounds) {
        super(regionId, regionType, ownerId);
        this.previousBounds = Objects.requireNonNull(previousBounds, "previousBounds");
        this.newBounds = Objects.requireNonNull(newBounds, "newBounds");
    }

    public RegionBounds getPreviousBounds() {
        return previousBounds;
    }

    public RegionBounds getNewBounds() {
        return newBounds;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
