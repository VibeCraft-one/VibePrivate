package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionLifecycleState;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import com.vibeprivate.storage.RegionLifecycleRepository;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionLifecycleServiceTest {

    @Test
    void fallbackEnabledRegionIsActive() {
        Region region = testRegion("r-active", true);
        InMemoryRegionLifecycleRepository repository = new InMemoryRegionLifecycleRepository();
        RegionLifecycleService service = new RegionLifecycleService(repository, new InMemoryRegionStore(region));

        assertEquals(RegionStatus.ACTIVE, service.getRegionStatus(region.getId()));
    }

    @Test
    void fallbackDisabledRegionIsInactive() {
        Region region = testRegion("r-inactive", false);
        InMemoryRegionLifecycleRepository repository = new InMemoryRegionLifecycleRepository();
        RegionLifecycleService service = new RegionLifecycleService(repository, new InMemoryRegionStore(region));

        assertEquals(RegionStatus.INACTIVE, service.getRegionStatus(region.getId()));
    }

    @Test
    void sealedStatusCreatesIndefiniteUpkeepPause() {
        Region region = testRegion("r-sealed", true);
        InMemoryRegionLifecycleRepository repository = new InMemoryRegionLifecycleRepository();
        RegionLifecycleService service = new RegionLifecycleService(repository, new InMemoryRegionStore(region));

        service.setRegionStatus(region.getId(), RegionStatus.SEALED);

        RegionLifecycleState saved = repository.requireSaved(region.getId());
        assertEquals(RegionStatus.SEALED, saved.getStatus());
        assertEquals(RegionLifecycleState.INDEFINITE_PAUSE, saved.getUpkeepPausedUntil());
        assertEquals("status:SEALED", saved.getUpkeepPauseReason());
        assertFalse(region.isEnabled());
    }

    @Test
    void resumeUpkeepIsRejectedForSealedAndArchivedStatuses() {
        InMemoryRegionLifecycleRepository sealedRepository = new InMemoryRegionLifecycleRepository();
        Region sealedRegion = testRegion("r-resume-sealed", false);
        sealedRepository.states.put(sealedRegion.getId(), lifecycleState(sealedRegion.getId(), RegionStatus.SEALED));
        RegionLifecycleService sealedService = new RegionLifecycleService(sealedRepository, new InMemoryRegionStore(sealedRegion));
        sealedService.load();

        IllegalStateException sealedError = assertThrows(IllegalStateException.class,
                () -> sealedService.resumeUpkeep(sealedRegion.getId(), "manual"));
        assertTrue(sealedError.getMessage().contains("SEALED"));

        InMemoryRegionLifecycleRepository archivedRepository = new InMemoryRegionLifecycleRepository();
        Region archivedRegion = testRegion("r-resume-archived", false);
        archivedRepository.states.put(archivedRegion.getId(), lifecycleState(archivedRegion.getId(), RegionStatus.ARCHIVED));
        RegionLifecycleService archivedService = new RegionLifecycleService(archivedRepository, new InMemoryRegionStore(archivedRegion));
        archivedService.load();

        IllegalStateException archivedError = assertThrows(IllegalStateException.class,
                () -> archivedService.resumeUpkeep(archivedRegion.getId(), "manual"));
        assertTrue(archivedError.getMessage().contains("ARCHIVED"));
    }

    @Test
    void activeStatusClearsPauseCreatedByStatus() {
        Region region = testRegion("r-unseal", false);
        InMemoryRegionLifecycleRepository repository = new InMemoryRegionLifecycleRepository();
        repository.states.put(region.getId(), new RegionLifecycleState(
                region.getId(),
                RegionStatus.SEALED,
                null,
                111L,
                RegionLifecycleState.INDEFINITE_PAUSE,
                "status:SEALED"
        ));
        RegionLifecycleService service = new RegionLifecycleService(repository, new InMemoryRegionStore(region));
        service.load();

        service.setRegionStatus(region.getId(), RegionStatus.ACTIVE);

        RegionLifecycleState saved = repository.requireSaved(region.getId());
        assertEquals(RegionStatus.ACTIVE, saved.getStatus());
        assertNull(saved.getUpkeepPausedUntil());
        assertNull(saved.getUpkeepPauseReason());
        assertTrue(region.isEnabled());
        assertEquals(0L, region.getFuelEmptySince());
    }

    private static Region testRegion(String id, boolean enabled) {
        return Region.radiusRegion(id, id, RegionType.HOME, "owner", "world")
                .radius(0, 0, 8, -64, 320)
                .state(enabled, 0L, enabled ? 0L : 50L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }

    private static RegionLifecycleState lifecycleState(String regionId, RegionStatus status) {
        return new RegionLifecycleState(regionId, status, null, 100L,
                RegionLifecycleState.INDEFINITE_PAUSE, "status:" + status.name());
    }

    private static final class InMemoryRegionStore implements RegionLifecycleRegionStore {
        private final Map<String, Region> regions = new HashMap<>();

        private InMemoryRegionStore(Region... initialRegions) {
            for (Region region : initialRegions) {
                regions.put(region.getId(), region);
            }
        }

        @Override
        public Optional<Region> getRegion(String regionId) {
            return Optional.ofNullable(regions.get(regionId));
        }

        @Override
        public Collection<Region> getRegions() {
            return regions.values();
        }

        @Override
        public void saveRegion(Region region) {
            regions.put(region.getId(), region);
        }
    }

    private static final class InMemoryRegionLifecycleRepository extends RegionLifecycleRepository {
        private final Map<String, RegionLifecycleState> states = new HashMap<>();

        @Override
        public Map<String, RegionLifecycleState> loadAll() {
            return new HashMap<>(states);
        }

        @Override
        public void save(RegionLifecycleState state) {
            states.put(state.getRegionId(), state);
        }

        RegionLifecycleState requireSaved(String regionId) {
            RegionLifecycleState state = states.get(regionId);
            assertNotNull(state, "Expected saved state for " + regionId);
            return state;
        }
    }
}
