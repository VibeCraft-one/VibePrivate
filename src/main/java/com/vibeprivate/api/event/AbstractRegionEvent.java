package com.vibeprivate.api.event;

import com.vibeprivate.model.RegionType;
import org.bukkit.event.Event;

import java.util.Objects;

public abstract class AbstractRegionEvent extends Event {
    private final String regionId;
    private final RegionType regionType;
    private final String ownerId;

    protected AbstractRegionEvent(String regionId, RegionType regionType, String ownerId) {
        this.regionId = requireText(regionId, "regionId");
        this.regionType = Objects.requireNonNull(regionType, "regionType");
        this.ownerId = requireText(ownerId, "ownerId");
    }

    public String getRegionId() {
        return regionId;
    }

    public RegionType getRegionType() {
        return regionType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }

        return value;
    }
}
