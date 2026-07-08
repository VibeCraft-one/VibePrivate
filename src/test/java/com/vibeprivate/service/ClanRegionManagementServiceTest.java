package com.vibeprivate.service;

import com.vibeprivate.model.ClanRegionRole;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import com.vibeprivate.storage.ClanRegionRoleRepository;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClanRegionManagementServiceTest {

    @Test
    void leaderAndOfficerCanManageClanRegionFromStoredRoles() {
        UUID leaderId = UUID.randomUUID();
        UUID officerId = UUID.randomUUID();
        Region clanRegion = clanRegion("clan-region", "clan-id");
        InMemoryClanRegionRoleRepository roleRepository = new InMemoryClanRegionRoleRepository();
        roleRepository.roles.put(clanRegion.getId(), new HashMap<>(Map.of(
                leaderId, ClanRegionRole.LEADER,
                officerId, ClanRegionRole.OFFICER
        )));
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(clanRegion), roleRepository);
        service.load();

        assertTrue(service.isClanRegionLeader(clanRegion.getId(), leaderId));
        assertTrue(service.canManageClanRegion(clanRegion.getId(), leaderId));
        assertFalse(service.isClanRegionLeader(clanRegion.getId(), officerId));
        assertTrue(service.canManageClanRegion(clanRegion.getId(), officerId));
    }

    @Test
    void clanOwnerIdDoesNotGrantPlayerLeaderCompatibility() {
        UUID playerId = UUID.randomUUID();
        Region clanRegion = clanRegion("clan-region", playerId.toString());
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(clanRegion), new InMemoryClanRegionRoleRepository());

        assertFalse(service.isClanRegionLeader(clanRegion.getId(), playerId));
        assertFalse(service.canManageClanRegion(clanRegion.getId(), playerId));
    }

    @Test
    void setMemberRoleRemovesElevatedRole() {
        UUID playerId = UUID.randomUUID();
        Region clanRegion = clanRegion("clan-region", "clan-id");
        InMemoryClanRegionRoleRepository roleRepository = new InMemoryClanRegionRoleRepository();
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(clanRegion), roleRepository);

        service.setClanRegionRole(clanRegion.getId(), playerId, ClanRegionRole.LEADER);
        assertEquals(Optional.of(ClanRegionRole.LEADER), service.getElevatedRole(clanRegion.getId(), playerId));

        service.setClanRegionRole(clanRegion.getId(), playerId, ClanRegionRole.MEMBER);

        assertTrue(service.getElevatedRole(clanRegion.getId(), playerId).isEmpty());
        assertTrue(roleRepository.roles.getOrDefault(clanRegion.getId(), Map.of()).isEmpty());
    }

    @Test
    void clanRegionLeaderReturnsFalseForMissingOrNonClanRegion() {
        UUID playerId = UUID.randomUUID();
        Region homeRegion = homeRegion("home-region", playerId.toString());
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(homeRegion), new InMemoryClanRegionRoleRepository());

        assertFalse(service.isClanRegionLeader("missing", playerId));
        assertFalse(service.canManageClanRegion("missing", playerId));
        assertFalse(service.isClanRegionLeader(homeRegion.getId(), playerId));
        assertFalse(service.canManageClanRegion(homeRegion.getId(), playerId));
        assertThrows(IllegalArgumentException.class,
                () -> service.setClanRegionRole(homeRegion.getId(), playerId, ClanRegionRole.LEADER));
    }

    @Test
    void clanRegionLeaderRejectsNullInputs() {
        UUID playerId = UUID.randomUUID();
        ClanRegionManagementService service = new ClanRegionManagementService(
                new InMemoryClanRegionManagementStore(), new InMemoryClanRegionRoleRepository());

        assertThrows(NullPointerException.class, () -> service.isClanRegionLeader(null, playerId));
        assertThrows(NullPointerException.class, () -> service.isClanRegionLeader("region", null));
        assertThrows(NullPointerException.class, () -> service.canManageClanRegion(null, playerId));
        assertThrows(NullPointerException.class, () -> service.canManageClanRegion("region", null));
        assertThrows(NullPointerException.class, () -> service.setClanRegionRole(null, playerId, ClanRegionRole.LEADER));
        assertThrows(NullPointerException.class, () -> service.setClanRegionRole("region", null, ClanRegionRole.LEADER));
        assertThrows(NullPointerException.class, () -> service.setClanRegionRole("region", playerId, null));
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

    private static final class InMemoryClanRegionRoleRepository extends ClanRegionRoleRepository {
        private final Map<String, Map<UUID, ClanRegionRole>> roles = new HashMap<>();

        @Override
        public Map<String, Map<UUID, ClanRegionRole>> loadAll() {
            Map<String, Map<UUID, ClanRegionRole>> copy = new HashMap<>();
            roles.forEach((regionId, regionRoles) -> copy.put(regionId, new HashMap<>(regionRoles)));
            return copy;
        }

        @Override
        public void save(String regionId, UUID playerId, ClanRegionRole role) {
            roles.computeIfAbsent(regionId, ignored -> new HashMap<>()).put(playerId, role);
        }

        @Override
        public void delete(String regionId, UUID playerId) {
            Map<UUID, ClanRegionRole> regionRoles = roles.get(regionId);
            if (regionRoles == null) {
                return;
            }

            regionRoles.remove(playerId);
            if (regionRoles.isEmpty()) {
                roles.remove(regionId);
            }
        }
    }
}
