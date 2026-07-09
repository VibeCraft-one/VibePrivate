package com.vibeprivate.api;

import com.vibeprivate.model.ClanRegionRole;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.SelectionBounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private static void assertMethod(Class<?> returnType, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        assertEquals(returnType, VibeRegionGuardApi.class.getMethod(name, parameterTypes).getReturnType());
    }
}
