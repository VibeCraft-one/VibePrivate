package com.vibeprivate.index;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionLookupIndexTest {

    @Test
    void rebuildIndexesRegionsByOwnerAndWorld() {
        Region home = region("home", "owner", "world", RegionType.HOME);
        Region farm = region("farm", "owner", "farm_world", RegionType.FARM);
        Region clan = region("clan", "clan-id", "world", RegionType.CLAN);
        Region other = region("other", "other-owner", "world", RegionType.HOME);
        Region admin = adminRegion("admin", "server", "world");

        RegionLookupIndex index = new RegionLookupIndex();
        index.rebuild(List.of(home, farm, clan, other, admin));

        assertEquals(List.of(home, farm), index.getByOwner("owner"));
        assertEquals(List.of(clan), index.getByOwner("clan-id"));
        assertEquals(List.of(home, clan, other, admin), index.getInWorld("world"));
        assertEquals(List.of(admin), index.getAdminRegions());
        assertEquals(1, index.getAdminRegionCount());
        assertEquals(3, index.getPlayerRegionCount());
        assertEquals(List.of(home, farm, other), index.getPlayerRegions());
        assertEquals(List.of("owner", "other-owner"), index.getPlayerOwnerIds());
        assertEquals(List.of(home, farm), index.getPlayerRegionsByOwner("owner"));
        assertTrue(index.getByOwner("missing").isEmpty());
        assertTrue(index.getInWorld("missing").isEmpty());
        assertTrue(index.getPlayerRegionsByOwner("missing").isEmpty());
    }

    @Test
    void removeClearsRegionFromOwnerAndWorldLookups() {
        Region region = region("home", "owner", "world", RegionType.HOME);

        RegionLookupIndex index = new RegionLookupIndex();
        index.add(region);
        index.remove(region);

        assertTrue(index.getByOwner("owner").isEmpty());
        assertTrue(index.getInWorld("world").isEmpty());
        assertTrue(index.getPlayerRegionsByOwner("owner").isEmpty());
        assertEquals(0, index.getPlayerRegionCount());
    }

    @Test
    void removeClearsAdminRegionLookups() {
        Region region = adminRegion("admin", "server", "world");

        RegionLookupIndex index = new RegionLookupIndex();
        index.add(region);
        index.remove(region);

        assertTrue(index.getAdminRegions().isEmpty());
        assertEquals(0, index.getAdminRegionCount());
    }

    @Test
    void rebuildHandlesSyntheticOnlineScaleWithoutMixingRegionClasses() {
        List<Region> regions = new ArrayList<>();
        for (int index = 0; index < 120; index++) {
            regions.add(region("home-" + index, "owner-" + index, "world-" + index % 3, RegionType.HOME));
        }

        for (int index = 0; index < 10; index++) {
            regions.add(region("clan-" + index, "clan-" + index, "world-0", RegionType.CLAN));
            regions.add(adminRegion("admin-" + index, "server", "world-0"));
        }

        RegionLookupIndex index = new RegionLookupIndex();
        index.rebuild(regions);

        assertEquals(120, index.getPlayerRegionCount());
        assertEquals(10, index.getAdminRegionCount());
        assertEquals(120, index.getPlayerOwnerIds().size());
        assertEquals(1, index.getPlayerRegionsByOwner("owner-42").size());
        assertEquals(10, index.getByOwner("server").size());
        assertEquals(1, index.getByOwner("clan-0").size());
        assertEquals(60, index.getInWorld("world-0").size());
        assertTrue(index.getPlayerRegionsByOwner("clan-0").isEmpty());
    }

    private Region region(String id, String ownerId, String worldName, RegionType type) {
        return Region.radiusRegion(id, id, type, ownerId, worldName)
                .radius(0, 0, 10, 0, 255)
                .build();
    }

    private Region adminRegion(String id, String ownerId, String worldName) {
        return Region.adminRegion(id, id, ownerId, worldName)
                .cuboid(0, 0, 0, 1, 1, 1)
                .build();
    }
}
