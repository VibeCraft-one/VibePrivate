package com.vibeprivate.cache;

import com.vibeprivate.model.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PlayerRegionCache {
    private final Map<UUID, Region> regionsByPlayer = new HashMap<>();

    public Region get(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        return regionsByPlayer.get(playerId);
    }

    public void set(UUID playerId, Region region) {
        Objects.requireNonNull(playerId, "playerId");

        if (region == null) {
            regionsByPlayer.remove(playerId);
        } else {
            regionsByPlayer.put(playerId, region);
        }
    }

    public void remove(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        regionsByPlayer.remove(playerId);
    }

    public void clear() {
        regionsByPlayer.clear();
    }
}
