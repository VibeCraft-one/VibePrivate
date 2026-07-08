package com.vibeprivate.storage;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegionDepositCascadeTest {

    @TempDir
    Path tempDir;

    @Test
    void deletingRegionCascadesPersistedDeposits() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        ConfigService configService = mock(ConfigService.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("RegionDepositCascadeTest"));
        when(configService.getDatabaseType()).thenReturn("sqlite");
        when(configService.getDatabaseFile()).thenReturn("cascade-test.db");

        DatabaseService databaseService = new DatabaseService(plugin, configService);
        try {
            databaseService.open();
            databaseService.migrate();

            RegionRepository regionRepository = new RegionRepository(databaseService);
            RegionDepositRepository depositRepository = new RegionDepositRepository(databaseService);
            Region region = Region.radiusRegion("cascade-region", "cascade-region", RegionType.HOME,
                            UUID.randomUUID().toString(), "world")
                    .radius(0, 0, 8, 64, 100)
                    .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                    .build();

            regionRepository.save(region);
            depositRepository.saveDeposit(region.getId(), Material.DIAMOND, 3);

            regionRepository.delete(region.getId());

            Map<String, Map<Material, Integer>> deposits = depositRepository.loadAll();
            assertFalse(deposits.containsKey(region.getId()));
        } finally {
            databaseService.close();
        }
    }
}
