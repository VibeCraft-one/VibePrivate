package com.vibeprivate.service;

import com.vibeprivate.api.event.RegionArchiveEvent;
import com.vibeprivate.api.event.RegionRestoreEvent;
import com.vibeprivate.api.event.RegionSealEvent;
import com.vibeprivate.api.event.RegionStatusChangeEvent;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionLifecycleState;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import com.vibeprivate.storage.RegionLifecycleRepository;
import org.bukkit.event.Event;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionLifecycleServiceEventTest {

    @Test
    void sealingRegionPublishesStatusAndSealEvents() {
        Region region = testRegion("r-seal", true);
        CapturingRegionEventDispatcher dispatcher = new CapturingRegionEventDispatcher();
        RegionLifecycleService service = new RegionLifecycleService(new InMemoryRegionLifecycleRepository(),
                new InMemoryRegionStore(region), dispatcher);

        service.setRegionStatus(region.getId(), RegionStatus.SEALED);

        assertEquals(2, dispatcher.events.size());
        assertTrue(dispatcher.events.get(0) instanceof RegionStatusChangeEvent);
        assertTrue(dispatcher.events.get(1) instanceof RegionSealEvent);
        RegionStatusChangeEvent changeEvent = (RegionStatusChangeEvent) dispatcher.events.get(0);
        assertEquals(RegionStatus.ACTIVE, changeEvent.getPreviousStatus());
        assertEquals(RegionStatus.SEALED, changeEvent.getNewStatus());
    }

    @Test
    void archivingRegionPublishesStatusAndArchiveEvents() {
        Region region = testRegion("r-archive", true);
        CapturingRegionEventDispatcher dispatcher = new CapturingRegionEventDispatcher();
        RegionLifecycleService service = new RegionLifecycleService(new InMemoryRegionLifecycleRepository(),
                new InMemoryRegionStore(region), dispatcher);

        service.setRegionStatus(region.getId(), RegionStatus.ARCHIVED);

        assertEquals(2, dispatcher.events.size());
        assertTrue(dispatcher.events.get(0) instanceof RegionStatusChangeEvent);
        assertTrue(dispatcher.events.get(1) instanceof RegionArchiveEvent);
    }

    @Test
    void restoringArchivedRegionPublishesStatusAndRestoreEvents() {
        Region region = testRegion("r-restore", false);
        InMemoryRegionLifecycleRepository repository = new InMemoryRegionLifecycleRepository();
        repository.states.put(region.getId(), new RegionLifecycleState(region.getId(), RegionStatus.ARCHIVED,
                null, 100L, RegionLifecycleState.INDEFINITE_PAUSE, "status:ARCHIVED"));
        CapturingRegionEventDispatcher dispatcher = new CapturingRegionEventDispatcher();
        RegionLifecycleService service = new RegionLifecycleService(repository, new InMemoryRegionStore(region),
                dispatcher);
        service.load();

        service.setRegionStatus(region.getId(), RegionStatus.ACTIVE);

        assertEquals(2, dispatcher.events.size());
        assertTrue(dispatcher.events.get(0) instanceof RegionStatusChangeEvent);
        assertTrue(dispatcher.events.get(1) instanceof RegionRestoreEvent);
        RegionRestoreEvent restoreEvent = (RegionRestoreEvent) dispatcher.events.get(1);
        assertEquals(RegionStatus.ARCHIVED, restoreEvent.getPreviousStatus());
        assertEquals(RegionStatus.ACTIVE, restoreEvent.getNewStatus());
    }

    @Test
    void unchangedStatusDoesNotPublishEvents() {
        Region region = testRegion("r-noop", true);
        CapturingRegionEventDispatcher dispatcher = new CapturingRegionEventDispatcher();
        RegionLifecycleService service = new RegionLifecycleService(new InMemoryRegionLifecycleRepository(),
                new InMemoryRegionStore(region), dispatcher);

        service.setRegionStatus(region.getId(), RegionStatus.ACTIVE);

        assertTrue(dispatcher.events.isEmpty());
    }

    private static Region testRegion(String id, boolean enabled) {
        return Region.radiusRegion(id, id, RegionType.HOME, "owner", "world")
                .radius(0, 0, 8, -64, 320)
                .state(enabled, 0L, enabled ? 0L : 50L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }

    private static final class CapturingRegionEventDispatcher implements RegionEventDispatcher {
        private final List<Event> events = new ArrayList<>();

        @Override
        public void dispatch(Event event) {
            events.add(event);
        }
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
        public Collection<Region> getRegionsInWorld(String worldName) {
            return regions.values().stream()
                    .filter(region -> region.getWorldName().equals(worldName))
                    .toList();
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
    }
}
