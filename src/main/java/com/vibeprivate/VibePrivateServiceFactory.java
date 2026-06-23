package com.vibeprivate;

import com.vibeprivate.api.VibePrivateAPI;
import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.command.CommandMapOverrideService;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.gui.GuiIconRegistry;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.protection.ProtectionService;
import com.vibeprivate.service.AdminRegionPresetService;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.ChunkProtectionService;
import com.vibeprivate.service.CommandCooldownService;
import com.vibeprivate.service.ConfirmationService;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.PendingTeleportService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.RegionDeletionService;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionInviteService;
import com.vibeprivate.service.RegionTeleportService;
import com.vibeprivate.service.RegionUpgradeService;
import com.vibeprivate.storage.DatabaseService;
import com.vibeprivate.storage.ProtectedChunkRepository;
import com.vibeprivate.storage.RegionAccessRepository;
import com.vibeprivate.storage.RegionDepositRepository;
import com.vibeprivate.storage.RegionHomeRepository;
import com.vibeprivate.storage.RegionRepository;
import com.vibeprivate.visualization.RegionBoundaryVisualizer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

final class VibePrivateServiceFactory {
    private VibePrivateServiceFactory() {
    }

    static VibePrivateServices create(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        VibePrivateServices.Builder builder = new VibePrivateServices.Builder();

        loadConfig(plugin, builder);
        openStorage(plugin, builder);
        loadRegions(plugin, builder);
        createRuntimeServices(plugin, builder);
        createFeatureServices(builder);

        return new VibePrivateServices(builder);
    }

    private static void loadConfig(JavaPlugin plugin, VibePrivateServices.Builder builder) {
        builder.configService = new ConfigService(plugin);
        builder.configService.load();

        builder.messageService = new MessageService(plugin, builder.configService);
        builder.messageService.load();
    }

    private static void openStorage(JavaPlugin plugin, VibePrivateServices.Builder builder) {
        builder.databaseService = new DatabaseService(plugin, builder.configService);
        builder.databaseService.open();
        builder.databaseService.migrate();

        builder.regionRepository = new RegionRepository(builder.databaseService);
        builder.regionAccessRepository = new RegionAccessRepository(builder.databaseService);
        builder.regionDepositRepository = new RegionDepositRepository(builder.databaseService);
        builder.regionHomeRepository = new RegionHomeRepository(builder.databaseService);
        builder.protectedChunkRepository = new ProtectedChunkRepository(builder.databaseService);
    }

    private static void loadRegions(JavaPlugin plugin, VibePrivateServices.Builder builder) {
        builder.regionAccessService = new RegionAccessService(builder.regionAccessRepository);
        builder.regionAccessService.load();

        builder.regionManager = new RegionManager(builder.regionRepository, builder.configService);
        builder.regionManager.load();

        builder.chunkProtectionService = new ChunkProtectionService(plugin, builder.configService,
                builder.protectedChunkRepository);
        builder.regionManager.setChunkProtectionService(builder.chunkProtectionService);
        builder.chunkProtectionService.protectExistingRegions(builder.regionManager.getRegions());
    }

    private static void createRuntimeServices(JavaPlugin plugin, VibePrivateServices.Builder builder) {
        builder.playerRegionCache = new PlayerRegionCache();
        builder.protectionService = new ProtectionService(builder.regionManager, builder.playerRegionCache,
                builder.regionAccessService);
        builder.fuelService = new FuelService(plugin, builder.regionManager, builder.configService);
        builder.commandCooldownService = new CommandCooldownService();
        builder.regionTeleportService = new RegionTeleportService();
        builder.pendingTeleportService = new PendingTeleportService(plugin, builder.messageService,
                builder.regionTeleportService);
        builder.boundaryVisualizer = new RegionBoundaryVisualizer(plugin, builder.configService);
        builder.commandMapOverrideService = new CommandMapOverrideService(plugin);
    }

    private static void createFeatureServices(VibePrivateServices.Builder builder) {
        builder.regionCreationService = new RegionCreationService(builder.regionManager, builder.configService,
                builder.regionAccessService);
        builder.regionInviteService = new RegionInviteService(builder.regionManager, builder.regionAccessService);
        builder.adminRegionService = new AdminRegionService(builder.regionManager, builder.regionAccessService);
        builder.adminRegionPresetService = new AdminRegionPresetService(builder.regionAccessService);
        builder.regionUpgradeService = new RegionUpgradeService(builder.regionManager, builder.configService,
                builder.regionDepositRepository);
        builder.regionUpgradeService.load();
        builder.confirmationService = new ConfirmationService();
        builder.regionDeletionService = new RegionDeletionService(builder.regionManager, builder.regionUpgradeService,
                builder.playerRegionCache, builder.confirmationService);
        builder.regionHomeService = new RegionHomeService(builder.regionManager, builder.regionHomeRepository);
        builder.guiIconRegistry = GuiIconRegistry.defaults();
        builder.api = new VibePrivateAPI(builder.regionManager, builder.regionCreationService,
                builder.adminRegionService, builder.regionAccessService);
    }
}
