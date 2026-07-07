package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClanRegionManagementServiceTest {

    @Test
    void isClanRegionLeaderReturnsTrueOnlyWhenClanOwnerMatchesPlayerUuid() {
        UUID leaderId = UUID.randomUUID();
        Region clanRegion = clanRegion("clan-region", leaderId.toString());
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(clanRegion));

        assertTrue(service.isClanRegionLeader(clanRegion.getId(), leaderId));
        assertTrue(service.canManageClanRegion(clanRegion.getId(), leaderId));
    }

    @Test
    void clanRegionLeaderReturnsFalseWhenOwnerIsNotPlayerUuid() {
        UUID playerId = UUID.randomUUID();
        Region clanRegion = clanRegion("clan-region", "external-clan-id");
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(clanRegion));

        assertFalse(service.isClanRegionLeader(clanRegion.getId(), playerId));
        assertFalse(service.canManageClanRegion(clanRegion.getId(), playerId));
    }

    @Test
    void clanRegionLeaderReturnsFalseForMissingOrNonClanRegion() {
        UUID playerId = UUID.randomUUID();
        Region homeRegion = homeRegion("home-region", playerId.toString());
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(homeRegion));

        assertFalse(service.isClanRegionLeader("missing", playerId));
        assertFalse(service.canManageClanRegion("missing", playerId));
        assertFalse(service.isClanRegionLeader(homeRegion.getId(), playerId));
        assertFalse(service.canManageClanRegion(homeRegion.getId(), playerId));
    }

    @Test
    void clanRegionLeaderRejectsNullInputs() {
        UUID playerId = UUID.randomUUID();
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore());

        assertThrows(NullPointerException.class, () -> service.isClanRegionLeader(null, playerId));
        assertThrows(NullPointerException.class, () -> service.isClanRegionLeader("region", null));
        assertThrows(NullPointerException.class, () -> service.canManageClanRegion(null, playerId));
        assertThrows(NullPointerException.class, () -> service.canManageClanRegion("region", null));
    }

    private static Region clanRegion(String id, String ownerId) {
        return Region.radiusRegion(id, "Clan", RegionType.CLAN, ownerId, "world")
                .radius(0, 0, 8, 64, 100)
                .state(true, 40L, 0L, 5L, 2, VisualizationMode.ALL, 200L)
                .build();
    }

    private static Region homeRegion(String id, String ownerId) {
        return Region.radiusRegion(id, "Home", RegionType.HOME, ownerId, "world")
                .radius(0, 0, 8, 64, 100)
                .state(true, 40L, 0L, 5L, 2, VisualizationMode.ALL, 200L)
                .build();
    }

    private static final class InMemoryClanRegionManagementStore implements ClanRegionManagementRegionStore {
        private final Map<String, Region> regions = new HashMap<>();

        private InMemoryClanRegionManagementStore(Region... initialRegions) {
            for (Region region : initialRegions) {
                regions.put(region.getId(), region);
            }
        }

        @Override
        public Optional<Region> getRegion(String regionId) {
            return Optional.ofNullable(regions.get(regionId));
        }
    }
}
