package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionRelocationServiceTest {

    @Test
    void moveRegionToWorldSameBoundsPreservesRadiusGeometryAndState() {
        Region region = testRegion("r-move", "world", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"), region);
        RegionRelocationService service = new RegionRelocationService(store);

        assertTrue(service.canMoveRegionToWorld(region.getId(), "world_nether"));

        Region moved = service.moveRegionToWorldSameBounds(region.getId(), "world_nether");

        assertEquals("world_nether", moved.getWorldName());
        assertEquals(region.getCenterX(), moved.getCenterX());
        assertEquals(region.getCenterZ(), moved.getCenterZ());
        assertEquals(region.getRadius(), moved.getRadius());
        assertEquals(region.getMinY(), moved.getMinY());
        assertEquals(region.getMaxY(), moved.getMaxY());
        assertEquals(region.getOwnerId(), moved.getOwnerId());
        assertEquals(region.getType(), moved.getType());
        assertEquals(region.isEnabled(), moved.isEnabled());
        assertEquals(region.getFuelExpiresAt(), moved.getFuelExpiresAt());
        assertEquals(region.getFuelEmptySince(), moved.getFuelEmptySince());
        assertEquals(region.getLastFuelDrainAt(), moved.getLastFuelDrainAt());
        assertEquals(region.getUpgradeLevel(), moved.getUpgradeLevel());
        assertEquals(region.getVisualizationMode(), moved.getVisualizationMode());
        assertEquals(region.getCreatedAt(), moved.getCreatedAt());
        assertEquals(1, store.replaceCalls);
        assertNotSame(region, moved);
    }

    @Test
    void canMoveRegionToWorldReturnsFalseForUnknownRegionAndActionThrows() {
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world"));
        RegionRelocationService service = new RegionRelocationService(store);

        assertFalse(service.canMoveRegionToWorld("missing", "world"));
        assertThrows(IllegalArgumentException.class,
                () -> service.moveRegionToWorldSameBounds("missing", "world"));
    }

    @Test
    void canMoveRegionToWorldReturnsFalseForDisallowedWorldAndDoesNotMutateState() {
        Region region = testRegion("r-disallowed", "world", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world"), region);
        RegionRelocationService service = new RegionRelocationService(store);

        assertFalse(service.canMoveRegionToWorld(region.getId(), "world_the_end"));
        assertEquals(0, store.replaceCalls);
        assertThrows(IllegalArgumentException.class,
                () -> service.moveRegionToWorldSameBounds(region.getId(), "world_the_end"));
    }

    @Test
    void canMoveRegionToWorldReturnsFalseForForeignOverlap() {
        Region region = testRegion("r-overlap", "world", 0, 0, 8);
        Region foreignRegion = testFarmRegion("r-foreign", "world_nether", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"),
                region, foreignRegion);
        RegionRelocationService service = new RegionRelocationService(store);

        assertFalse(service.canMoveRegionToWorld(region.getId(), "world_nether"));
        assertThrows(IllegalStateException.class,
                () -> service.moveRegionToWorldSameBounds(region.getId(), "world_nether"));
    }

    @Test
    void relocateRegionPreservesRadiusSizeAndState() {
        Region region = testRegion("r-relocate", "world", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"), region);
        RegionRelocationService service = new RegionRelocationService(store);

        assertTrue(service.canRelocateRegion(region.getId(), "world_nether", 32, -48));

        Region relocated = service.relocateRegion(region.getId(), "world_nether", 32, -48);

        assertEquals("world_nether", relocated.getWorldName());
        assertEquals(32, relocated.getCenterX());
        assertEquals(-48, relocated.getCenterZ());
        assertEquals(region.getRadius(), relocated.getRadius());
        assertEquals(region.getMinY(), relocated.getMinY());
        assertEquals(region.getMaxY(), relocated.getMaxY());
        assertEquals(region.getOwnerId(), relocated.getOwnerId());
        assertEquals(region.getType(), relocated.getType());
        assertEquals(region.isEnabled(), relocated.isEnabled());
        assertEquals(region.getFuelExpiresAt(), relocated.getFuelExpiresAt());
        assertEquals(region.getFuelEmptySince(), relocated.getFuelEmptySince());
        assertEquals(region.getLastFuelDrainAt(), relocated.getLastFuelDrainAt());
        assertEquals(region.getUpgradeLevel(), relocated.getUpgradeLevel());
        assertEquals(region.getVisualizationMode(), relocated.getVisualizationMode());
        assertEquals(region.getCreatedAt(), relocated.getCreatedAt());
        assertEquals(1, store.replaceCalls);
    }

    @Test
    void canRelocateRegionReturnsFalseForAdminRegionAndActionThrows() {
        Region adminRegion = Region.adminRegion("r-admin", "ADMIN", "owner", "admin_world")
                .cuboid(-5, 64, -5, 5, 100, 5)
                .state(true, 40L, 0L, 5L, 2, VisualizationMode.ALL, 200L)
                .build();
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"), adminRegion);
        RegionRelocationService service = new RegionRelocationService(store);

        assertFalse(service.canRelocateRegion(adminRegion.getId(), "world_nether", 10, 10));
        assertEquals(0, store.replaceCalls);
        assertThrows(UnsupportedOperationException.class,
                () -> service.relocateRegion(adminRegion.getId(), "world_nether", 10, 10));
    }

    @Test
    void canRelocateRegionReturnsFalseForForeignOverlapAndDoesNotMutateState() {
        Region region = testRegion("r-overlap-relocate", "world", 0, 0, 8);
        Region foreignRegion = testFarmRegion("r-foreign-relocate", "world_nether", 25, -48, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"),
                region, foreignRegion);
        RegionRelocationService service = new RegionRelocationService(store);

        assertFalse(service.canRelocateRegion(region.getId(), "world_nether", 32, -48));
        assertEquals(0, store.replaceCalls);
        assertThrows(IllegalStateException.class,
                () -> service.relocateRegion(region.getId(), "world_nether", 32, -48));
    }

    @Test
    void canMethodsDoNotMutateState() {
        Region region = testRegion("r-can", "world", 0, 0, 8);
        InMemoryRegionRelocationStore store = new InMemoryRegionRelocationStore(Set.of("world", "world_nether"), region);
        RegionRelocationService service = new RegionRelocationService(store);

        assertTrue(service.canMoveRegionToWorld(region.getId(), "world_nether"));
        assertTrue(service.canRelocateRegion(region.getId(), "world_nether", 48, 48));
        assertEquals(0, store.replaceCalls);
        assertSame(region, store.requireRegion(region.getId()));
    }

    private static Region testRegion(String id, String worldName, int centerX, int centerZ, int radius) {
        return Region.radiusRegion(id, id, RegionType.HOME, "owner", worldName)
                .radius(centerX, centerZ, radius, 64, 100)
                .state(true, 40L, 0L, 5L, 2, VisualizationMode.ALL, 200L)
                .build();
    }

    private static Region testFarmRegion(String id, String worldName, int centerX, int centerZ, int radius) {
        return Region.radiusRegion(id, id, RegionType.FARM, "other-owner", worldName)
                .radius(centerX, centerZ, radius, 64, 100)
                .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }

    private static final class InMemoryRegionRelocationStore implements RegionRelocationRegionStore {
        private final Map<String, Region> regions = new HashMap<>();
        private final Set<String> allowedWorlds;
        private int replaceCalls;

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
            replaceCalls++;
            regions.put(region.getId(), region);
        }

        private Region requireRegion(String regionId) {
            Region region = regions.get(regionId);
            if (region == null) {
                throw new IllegalArgumentException("Missing region in test store: " + regionId);
            }

            return region;
        }
    }
}
