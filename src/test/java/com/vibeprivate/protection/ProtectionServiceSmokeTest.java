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

    private record ProtectionFixture(DatabaseService databaseService, RegionManager regionManager,
                                     RegionAccessService accessService, ProtectionService protectionService) {
        void close() {
            databaseService.close();
        }
    }
}
