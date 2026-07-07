package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;

import java.util.Objects;
import java.util.UUID;

public final class ClanRegionManagementService {
    private final ClanRegionManagementRegionStore regionStore;

    public ClanRegionManagementService(ClanRegionManagementRegionStore regionStore) {
        this.regionStore = Objects.requireNonNull(regionStore, "regionStore");
    }

    public boolean isClanRegionLeader(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        return regionStore.getRegion(regionId)
                .filter(region -> region.getType() == RegionType.CLAN)
                .map(Region::getOwnerId)
                .filter(ownerId -> ownerId.equals(playerId.toString()))
                .isPresent();
    }

    public boolean canManageClanRegion(String regionId, UUID playerId) {
        return isClanRegionLeader(regionId, playerId);
    }
}
