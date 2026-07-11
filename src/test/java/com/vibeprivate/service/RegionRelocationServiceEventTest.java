package com.vibeprivate.service;

import com.vibeprivate.api.event.RegionRelocateEvent;
import com.vibeprivate.api.event.RegionWorldMoveEvent;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionHome;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import org.bukkit.event.Event;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionRelocationServiceEventTest {

    @Test
    void worldMovePublishesTypedMoveEventAfterSuccessfulRemap() {
        Region region = testRegion("r-move", "world", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"),
                region);
        store.putHome(new RegionHome(region.getId(), "world", 1.5, 70.0, 2.5, 0.0f, 0.0f));
        CapturingRegionEventDispatcher dispatcher = new CapturingRegionEventDispatcher();
        RegionRelocationService service = new RegionRelocationService(store, dispatcher);

        Region moved = service.moveRegionToWorldSameBounds(region.getId(), "world_nether");

        assertEquals(1, dispatcher.events.size());
        assertTrue(dispatcher.events.get(0) instanceof RegionWorldMoveEvent);
        RegionWorldMoveEvent event = (RegionWorldMoveEvent) dispatcher.events.get(0);
        assertEquals("world", event.getPreviousBounds().getWorldName());
        assertEquals("world_nether", event.getNewBounds().getWorldName());
        assertEquals(moved.getBounds().getWorldName(), event.getNewBounds().getWorldName());
    }

    @Test
    void relocatePublishesTypedRelocateEvent() {
        Region region = testRegion("r-relocate", "world", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"),
                region);
        CapturingRegionEventDispatcher dispatcher = new CapturingRegionEventDispatcher();
        RegionRelocationService service = new RegionRelocationService(store, dispatcher);

        Region relocated = service.relocateRegion(region.getId(), "world_nether", 32, -48);

        assertEquals(1, dispatcher.events.size());
        assertTrue(dispatcher.events.get(0) instanceof RegionRelocateEvent);
        RegionRelocateEvent event = (RegionRelocateEvent) dispatcher.events.get(0);
        assertEquals("world", event.getPreviousBounds().getWorldName());
        assertEquals("world_nether", event.getNewBounds().getWorldName());
        assertEquals(relocated.getBounds().getMinX(), event.getNewBounds().getMinX());
    }

    @Test
    void canChecksAndNoOpMoveDoNotPublishEvents() {
        Region region = testRegion("r-noop", "world", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"),
                region);
        CapturingRegionEventDispatcher dispatcher = new CapturingRegionEventDispatcher();
        RegionRelocationService service = new RegionRelocationService(store, dispatcher);

        assertTrue(service.canMoveRegionToWorld(region.getId(), "world_nether"));
        assertTrue(service.canRelocateRegion(region.getId(), "world_nether", 20, 20));
        service.moveRegionToWorldSameBounds(region.getId(), "world");

        assertTrue(dispatcher.events.isEmpty());
    }

    private static Region testRegion(String id, String worldName, int centerX, int centerZ, int radius) {
        return Region.radiusRegion(id, id, RegionType.HOME, "owner", worldName)
                .radius(centerX, centerZ, radius, 64, 100)
                .state(true, 40L, 0L, 5L, 2, VisualizationMode.ALL, 200L)
                .build();
    }

    private static final class CapturingRegionEventDispatcher implements RegionEventDispatcher {
        private final List<Event> events = new ArrayList<>();

        @Override
        public void dispatch(Event event) {
            events.add(event);
        }
    }

    private static final class InMemoryRegionRelocationStore implements RegionRelocationRegionStore {
        private final Map<String, Region> regions = new HashMap<>();
        private final Map<String, RegionHome> homes = new HashMap<>();
        private final Set<String> allowedWorlds;

        private InMemoryRegionRelocationStore(Set<String> allowedWorlds, Region... initialRegions) {
            this.allowedWorlds = allowedWorlds;
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
        public boolean isAllowedWorld(String worldName) {
            return allowedWorlds.contains(worldName);
        }

        @Override
        public void replaceRegion(Region region) {
            regions.put(region.getId(), region);
        }

        @Override
        public Optional<RegionHome> getHome(String regionId) {
            return Optional.ofNullable(homes.get(regionId));
        }

        @Override
        public void saveHome(RegionHome home) {
            homes.put(home.regionId(), home);
        }

        private void putHome(RegionHome home) {
            homes.put(home.regionId(), home);
        }
    }
}
