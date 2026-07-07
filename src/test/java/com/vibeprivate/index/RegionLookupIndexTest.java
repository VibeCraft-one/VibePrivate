package com.vibeprivate.index;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionLookupIndexTest {

    @Test
    void rebuildIndexesRegionsByOwnerAndWorld() {
        Region home = region("home", "owner", "world", RegionType.HOME);
        Region farm = region("farm", "owner", "farm_world", RegionType.FARM);
        Region other = region("other", "other-owner", "world", RegionType.HOME);

        RegionLookupIndex index = new RegionLookupIndex();
        index.rebuild(List.of(home, farm, other));

        assertEquals(List.of(home, farm), index.getByOwner("owner"));
        assertEquals(List.of(home, other), index.getInWorld("world"));
        assertTrue(index.getByOwner("missing").isEmpty());
        assertTrue(index.getInWorld("missing").isEmpty());
    }

    @Test
    void removeClearsRegionFromOwnerAndWorldLookups() {
        Region region = region("home", "owner", "world", RegionType.HOME);

        RegionLookupIndex index = new RegionLookupIndex();
        index.add(region);
        index.remove(region);

        assertTrue(index.getByOwner("owner").isEmpty());
        assertTrue(index.getInWorld("world").isEmpty());
    }

    private Region region(String id, String ownerId, String worldName, RegionType type) {
        return Region.radiusRegion(id, id, type, ownerId, worldName)
                .radius(0, 0, 10, 0, 255)
                .build();
    }
}
