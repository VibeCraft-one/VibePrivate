package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.SelectionBounds;
import com.vibeprivate.model.VisualizationMode;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegionSelectionValidatorTest {

    @Test
    void returnsTrueWhenSelectionIsFullyInsideRegion() {
        Region region = testRegion("r-inside", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        boolean result = validator.isAreaInsideRegion(region.getId(),
                new SelectionBounds("world", -3, 3, 70, 80, -2, 2));

        assertTrue(result);
    }

    @Test
    void returnsFalseWhenSelectionExceedsRegionBounds() {
        Region region = testRegion("r-outside", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        boolean result = validator.isAreaInsideRegion(region.getId(),
                new SelectionBounds("world", -9, 3, 70, 80, -2, 2));

        assertFalse(result);
    }

    @Test
    void returnsFalseWhenSelectionWorldDoesNotMatchRegionWorld() {
        Region region = testRegion("r-world", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        boolean result = validator.isAreaInsideRegion(region.getId(),
                new SelectionBounds("nether", -3, 3, 70, 80, -2, 2));

        assertFalse(result);
    }

    @Test
    void returnsFalseWhenSelectionIsNull() {
        Region region = testRegion("r-null", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        assertFalse(validator.isAreaInsideRegion(region.getId(), null));
    }

    @Test
    void returnsNormalizedRegionBounds() {
        Region region = testRegion("r-bounds", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        RegionBounds bounds = validator.getRegionBounds(region.getId());

        assertEquals("world", bounds.getWorldName());
        assertEquals(-8, bounds.getMinX());
        assertEquals(8, bounds.getMaxX());
        assertEquals(-8, bounds.getMinZ());
        assertEquals(8, bounds.getMaxZ());
    }

    @Test
    void rejectsInvalidSelectionWorldAtModelBoundary() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new SelectionBounds(" ", 0, 0, 64, 64, 0, 0));

        assertTrue(error.getMessage().contains("worldName"));
    }

    @Test
    void returnsFalseWhenSelectionIsBelowWorldMinHeight() {
        Region region = testRegion("r-below", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        boolean result = validator.isAreaInsideRegion(region.getId(),
                new SelectionBounds("world", -3, 3, 63, 80, -2, 2));

        assertFalse(result);
    }

    @Test
    void returnsFalseWhenSelectionIsAboveWorldMaxHeight() {
        Region region = testRegion("r-above", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        boolean result = validator.isAreaInsideRegion(region.getId(),
                new SelectionBounds("world", -3, 3, 70, 101, -2, 2));

        assertFalse(result);
    }

    @Test
    void returnsFalseWhenSelectionOverlapsAnotherNonAdminRegion() {
        Region region = testRegion("r-owner", "world");
        Region otherRegion = Region.radiusRegion("r-other", "other", RegionType.FARM, "other-owner", "world")
                .radius(4, 0, 4, 64, 100)
                .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region, otherRegion);

        boolean result = validator.isAreaInsideRegion(region.getId(),
                new SelectionBounds("world", 1, 6, 70, 80, -2, 2));

        assertFalse(result);
    }

    @Test
    void overlapOnlyWithOwnRegionDoesNotCountAsForeignOverlap() {
        Region region = testRegion("r-own-only", "world");
        RegionSelectionValidator validator = validator(new RegionSelectionWorldHeight(64, 100), region);

        boolean result = validator.isAreaInsideRegion(region.getId(),
                new SelectionBounds("world", -8, 8, 64, 100, -8, 8));

        assertTrue(result);
    }

    private static RegionSelectionValidator validator(RegionSelectionWorldHeight worldHeight, Region... regions) {
        return new RegionSelectionValidator(new InMemoryRegionStore(regions),
                worldName -> Optional.of(worldHeight));
    }

    private static Region testRegion(String id, String worldName) {
        return Region.radiusRegion(id, id, RegionType.HOME, "owner", worldName)
                .radius(0, 0, 8, 64, 100)
                .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }

    private static final class InMemoryRegionStore implements RegionSelectionRegionStore {
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
    }
}
