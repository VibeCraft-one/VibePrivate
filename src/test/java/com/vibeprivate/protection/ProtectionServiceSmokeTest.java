package com.vibeprivate.protection;

import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.storage.DatabaseService;
import com.vibeprivate.storage.RegionAccessRepository;
import com.vibeprivate.storage.RegionRepository;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProtectionServiceSmokeTest {

    @TempDir
    Path tempDir;

    @Test
    void homeRegionProtectsGuestsAndAllowsOwnerAndConfiguredMember() {
        ProtectionFixture fixture = openFixture();
        try {
            UUID ownerId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UUID guestId = UUID.randomUUID();
            Region home = homeRegion("home", ownerId, 100, 100);
            fixture.regionManager.addRegion(home);
            fixture.accessService.addMember(home.getId(), memberId);
            fixture.accessService.setDefaultFlag(home.getId(), RegionFlag.BUILD, true);

            Location location = location("world", 100, 80, 100);

            assertTrue(fixture.protectionService.canUse(player(ownerId), location, RegionFlag.BUILD));
            assertTrue(fixture.protectionService.canUse(player(memberId), location, RegionFlag.BUILD));
            assertFalse(fixture.protectionService.canUse(player(guestId), location, RegionFlag.BUILD));
        } finally {
            fixture.close();
        }
    }

    @Test
    void adminRegionTakesPriorityOverOverlappingHomeRegion() {
        ProtectionFixture fixture = openFixture();
        try {
            UUID ownerId = UUID.randomUUID();
            Region home = homeRegion("home-overlap", ownerId, 0, 0);
            Region admin = Region.adminRegion("admin-overlap", "admin-overlap", "server", "world")
                    .cuboid(-5, 60, -5, 5, 120, 5)
                    .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                    .build();
            fixture.regionManager.addRegion(home);
            fixture.regionManager.addRegion(admin);

            assertFalse(fixture.protectionService.canUse(player(ownerId), location("world", 0, 80, 0), RegionFlag.BUILD));
        } finally {
            fixture.close();
        }
    }

    @Test
    void cachedHomeRegionDoesNotBypassLaterAdminOverlap() {
        ProtectionFixture fixture = openFixture();
        try {
            UUID ownerId = UUID.randomUUID();
            Player owner = player(ownerId);
            Location location = location("world", 0, 80, 0);
            Region home = homeRegion("home-cached-overlap", ownerId, 0, 0);
            fixture.regionManager.addRegion(home);

            assertTrue(fixture.protectionService.canUse(owner, location, RegionFlag.BUILD));

            Region admin = Region.adminRegion("admin-cached-overlap", "admin-cached-overlap", "server", "world")
                    .cuboid(-5, 60, -5, 5, 120, 5)
                    .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                    .build();
            fixture.regionManager.addRegion(admin);

            assertFalse(fixture.protectionService.canUse(owner, location, RegionFlag.BUILD));
        } finally {
            fixture.close();
        }
    }

    @Test
    void opAndBypassPermissionCanUseProtectedHomeRegion() {
        ProtectionFixture fixture = openFixture();
        try {
            UUID ownerId = UUID.randomUUID();
            Region home = homeRegion("home-bypass", ownerId, 200, 200);
            fixture.regionManager.addRegion(home);
            Location location = location("world", 200, 80, 200);

            assertTrue(fixture.protectionService.canUse(opPlayer(UUID.randomUUID()), location, RegionFlag.BUILD));
            assertTrue(fixture.protectionService.canUse(bypassPlayer(UUID.randomUUID()), location, RegionFlag.BUILD));
        } finally {
            fixture.close();
        }
    }

    @Test
    void environmentUseFollowsRegionDefaultFlags() {
        ProtectionFixture fixture = openFixture();
        try {
            UUID ownerId = UUID.randomUUID();
            Region home = homeRegion("home-environment", ownerId, 300, 300);
            fixture.regionManager.addRegion(home);
            Location location = location("world", 300, 80, 300);

            assertFalse(fixture.protectionService.canEnvironmentUse(location, RegionFlag.FIRE_SPREAD));

            fixture.accessService.setDefaultFlag(home.getId(), RegionFlag.FIRE_SPREAD, true);

            assertTrue(fixture.protectionService.canEnvironmentUse(location, RegionFlag.FIRE_SPREAD));
        } finally {
            fixture.close();
        }
    }

    @Test
    void adminEnvironmentUsesAdminFlagRules() {
        ProtectionFixture fixture = openFixture();
        try {
            Region admin = adminRegion("admin-environment", 400, 400);
            fixture.regionManager.addRegion(admin);
            Location location = location("world", 400, 80, 400);

            assertFalse(fixture.protectionService.canEnvironmentUse(location, RegionFlag.PROJECTILES));
            assertTrue(fixture.protectionService.canEnvironmentUse(location, RegionFlag.HUNGER));

            fixture.accessService.setDefaultFlag(admin.getId(), RegionFlag.PROJECTILE_DAMAGE, true);

            assertTrue(fixture.protectionService.canEnvironmentUse(location, RegionFlag.PROJECTILES));
        } finally {
            fixture.close();
        }
    }

    @Test
    void adminFallDamageDefaultIsAllowedUnlessExplicitlyDisabled() {
        ProtectionFixture fixture = openFixture();
        try {
            Region admin = adminRegion("admin-fall-damage", 500, 500);
            fixture.regionManager.addRegion(admin);
            Location location = location("world", 500, 80, 500);

            assertTrue(fixture.protectionService.canEnvironmentUse(location, RegionFlag.FALL_DAMAGE));

            fixture.accessService.setDefaultFlag(admin.getId(), RegionFlag.FALL_DAMAGE, false);

            assertFalse(fixture.protectionService.canEnvironmentUse(location, RegionFlag.FALL_DAMAGE));
        } finally {
            fixture.close();
        }
    }

    private ProtectionFixture openFixture() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        FileConfiguration config = mock(FileConfiguration.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("ProtectionServiceSmokeTest"));
        when(plugin.getConfig()).thenReturn(config);
        when(config.getString("database.type", "sqlite")).thenReturn("sqlite");
        when(config.getString("database.file", "vibeprivate.db")).thenReturn(UUID.randomUUID() + ".db");
        when(config.getStringList("regions.allowed-worlds")).thenReturn(List.of("world"));

        ConfigService configService = new ConfigService(plugin);
        configService.load();
        DatabaseService databaseService = new DatabaseService(plugin, configService);
        databaseService.open();
        databaseService.migrate();

        RegionManager regionManager = new RegionManager(new RegionRepository(databaseService), configService);
        RegionAccessService accessService = new RegionAccessService(new RegionAccessRepository(databaseService));
        accessService.load();
        ProtectionService protectionService = new ProtectionService(regionManager, new PlayerRegionCache(), accessService);
        return new ProtectionFixture(databaseService, regionManager, accessService, protectionService);
    }

    private Region homeRegion(String id, UUID ownerId, int centerX, int centerZ) {
        return Region.radiusRegion(id, id, RegionType.HOME, ownerId.toString(), "world")
                .radius(centerX, centerZ, 8, 60, 120)
                .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }

    private Region adminRegion(String id, int centerX, int centerZ) {
        return Region.adminRegion(id, id, "server", "world")
                .cuboid(centerX - 5, 60, centerZ - 5, centerX + 5, 120, centerZ + 5)
                .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }

    private Location location(String worldName, int x, int y, int z) {
        World world = mock(World.class);
        when(world.getName()).thenReturn(worldName);
        return new Location(world, x, y, z);
    }

    private Player player(UUID playerId) {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission("vibeprivate.bypass")).thenReturn(false);
        return player;
    }

    private Player opPlayer(UUID playerId) {
        Player player = player(playerId);
        when(player.isOp()).thenReturn(true);
        return player;
    }

    private Player bypassPlayer(UUID playerId) {
        Player player = player(playerId);
        when(player.hasPermission("vibeprivate.bypass")).thenReturn(true);
        return player;
    }

    private record ProtectionFixture(DatabaseService databaseService, RegionManager regionManager,
                                     RegionAccessService accessService, ProtectionService protectionService) {
        void close() {
            databaseService.close();
        }
    }
}
