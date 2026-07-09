package com.vibeprivate.api;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.ClanRegionRole;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.SelectionBounds;
import com.vibeprivate.model.VisualizationMode;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.ClanRegionManagementService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.RegionLifecycleService;
import com.vibeprivate.service.RegionRelocationService;
import com.vibeprivate.service.RegionSelectionValidator;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VibeRegionGuardApiTest {

    @Test
    void facadeRejectsNullApi() {
        assertThrows(NullPointerException.class, () -> new VibeRegionGuardApi(null));
    }

    @Test
    void facadeExposesExternalTransferApiSurface() throws NoSuchMethodException {
        assertMethod(Collection.class, "getRegionsInWorld", String.class);
        assertMethod(Collection.class, "getRegionsInWorld", String.class, boolean.class);
        assertMethod(List.class, "getRegionsByOwner", String.class);
        assertMethod(List.class, "getRegionsByOwnerAndType", String.class, RegionType.class);
        assertMethod(RegionStatus.class, "getRegionStatus", String.class);
        assertMethod(RegionBounds.class, "getRegionBounds", String.class);
        assertMethod(boolean.class, "isTargetBoundsValid", SelectionBounds.class);
        assertMethod(boolean.class, "isAreaInsideRegion", String.class, SelectionBounds.class);
        assertMethod(boolean.class, "canMoveRegionToWorld", String.class, String.class);
        assertMethod(Region.class, "moveRegionToWorldSameBounds", String.class, String.class);
        assertMethod(boolean.class, "canRelocateRegion", String.class, String.class, int.class, int.class);
        assertMethod(Region.class, "relocateRegion", String.class, String.class, int.class, int.class);
        assertMethod(void.class, "pauseUpkeep", String.class, String.class);
        assertMethod(void.class, "resumeUpkeep", String.class, String.class);
        assertMethod(com.vibeprivate.service.RegionCreationResult.class, "createPrivateRegion", Player.class);
        assertMethod(com.vibeprivate.service.RegionCreationResult.class, "createFarmRegion", Player.class);
    }

    @Test
    void facadeExposesClanRoleApiSurface() throws NoSuchMethodException {
        assertMethod(com.vibeprivate.service.RegionCreationResult.class,
                "createClanRegion", String.class, UUID.class, Location.class, String.class);
        assertMethod(Optional.class, "getClanRegionRole", String.class, UUID.class);
        assertMethod(void.class, "setClanRegionRole", String.class, UUID.class, ClanRegionRole.class);
    }

    @Test
    void facadeDelegatesTransferReadAndMoveBehaviorToLegacyApi() {
        RegionManager regionManager = mock(RegionManager.class);
        RegionCreationService regionCreationService = mock(RegionCreationService.class);
        AdminRegionService adminRegionService = mock(AdminRegionService.class);
        ClanRegionManagementService clanRegionManagementService = mock(ClanRegionManagementService.class);
        RegionAccessService regionAccessService = mock(RegionAccessService.class);
        RegionLifecycleService regionLifecycleService = mock(RegionLifecycleService.class);
        RegionRelocationService regionRelocationService = mock(RegionRelocationService.class);
        RegionSelectionValidator regionSelectionValidator = mock(RegionSelectionValidator.class);
        VibeRegionGuardApi facade = new VibeRegionGuardApi(new VibePrivateAPI(regionManager, regionCreationService,
                adminRegionService, clanRegionManagementService, regionAccessService, regionLifecycleService,
                regionRelocationService, regionSelectionValidator));
        Region region = testRegion("api-home", "world", 0, 0);
        Region moved = testRegion("api-home", "world_nether", 0, 0);
        SelectionBounds targetBounds = new SelectionBounds("world_nether", -8, 8, 60, 120, -8, 8);

        when(regionLifecycleService.getRegionsInWorld("world", false)).thenReturn(List.of(region));
        when(regionSelectionValidator.getRegionBounds(region.getId())).thenReturn(region.getBounds());
        when(regionSelectionValidator.isTargetBoundsValid(targetBounds)).thenReturn(true);
        when(regionRelocationService.canMoveRegionToWorld(region.getId(), "world_nether")).thenReturn(true);
        when(regionRelocationService.moveRegionToWorldSameBounds(region.getId(), "world_nether")).thenReturn(moved);

        assertEquals(List.of(region), facade.getRegionsInWorld("world"));
        assertSame(region.getBounds(), facade.getRegionBounds(region.getId()));
        assertTrue(facade.isTargetBoundsValid(targetBounds));
        assertTrue(facade.canMoveRegionToWorld(region.getId(), "world_nether"));
        assertSame(moved, facade.moveRegionToWorldSameBounds(region.getId(), "world_nether"));

        verify(regionLifecycleService).getRegionsInWorld("world", false);
        verify(regionSelectionValidator).getRegionBounds(region.getId());
        verify(regionSelectionValidator).isTargetBoundsValid(targetBounds);
        verify(regionRelocationService).canMoveRegionToWorld(region.getId(), "world_nether");
        verify(regionRelocationService).moveRegionToWorldSameBounds(region.getId(), "world_nether");
    }

    private static void assertMethod(Class<?> returnType, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        assertEquals(returnType, VibeRegionGuardApi.class.getMethod(name, parameterTypes).getReturnType());
    }

    private static Region testRegion(String id, String worldName, int centerX, int centerZ) {
        return Region.radiusRegion(id, id, RegionType.HOME, UUID.randomUUID().toString(), worldName)
                .radius(centerX, centerZ, 8, 60, 120)
                .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }
}
