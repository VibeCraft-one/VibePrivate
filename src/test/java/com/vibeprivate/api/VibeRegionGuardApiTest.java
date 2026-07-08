package com.vibeprivate.api;

import com.vibeprivate.model.ClanRegionRole;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

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
    void facadeExposesClanRoleApiSurface() throws NoSuchMethodException {
        assertEquals("createClanRegion", VibeRegionGuardApi.class
                .getMethod("createClanRegion", String.class, UUID.class, Location.class, String.class)
                .getName());
        assertEquals(Optional.class, VibeRegionGuardApi.class
                .getMethod("getClanRegionRole", String.class, UUID.class)
                .getReturnType());
        assertEquals(void.class, VibeRegionGuardApi.class
                .getMethod("setClanRegionRole", String.class, UUID.class, ClanRegionRole.class)
                .getReturnType());
    }
}
