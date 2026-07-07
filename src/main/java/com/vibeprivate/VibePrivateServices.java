package com.vibeprivate;

import com.vibeprivate.api.VibePrivateAPI;
import com.vibeprivate.api.VibeRegionGuardApi;
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
import com.vibeprivate.service.EconomyService;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.PendingTeleportService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.RegionDeletionService;
import com.vibeprivate.service.RegionEventDispatcher;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionInviteService;
import com.vibeprivate.service.RegionLifecycleService;
import com.vibeprivate.service.RegionRelocationService;
import com.vibeprivate.service.RegionSelectionValidator;
import com.vibeprivate.service.RegionTeleportService;
import com.vibeprivate.service.RegionUpgradeService;
import com.vibeprivate.service.UpkeepService;
import com.vibeprivate.storage.DatabaseService;
import com.vibeprivate.storage.ProtectedChunkRepository;
import com.vibeprivate.storage.RegionAccessRepository;
import com.vibeprivate.storage.RegionDepositRepository;
import com.vibeprivate.storage.RegionHomeRepository;
import com.vibeprivate.storage.RegionLifecycleRepository;
import com.vibeprivate.storage.RegionRepository;
import com.vibeprivate.storage.UpkeepRepository;
import com.vibeprivate.visualization.RegionBoundaryVisualizer;
import org.bukkit.plugin.java.JavaPlugin;

final class VibePrivateServices {
    private final ConfigService configService;
    private final MessageService messageService;
    private final DatabaseService databaseService;
    private final RegionRepository regionRepository;
    private final RegionAccessRepository regionAccessRepository;
    private final RegionDepositRepository regionDepositRepository;
    private final RegionHomeRepository regionHomeRepository;
    private final RegionAccessService regionAccessService;
    private final RegionManager regionManager;
    private final PlayerRegionCache playerRegionCache;
    private final ProtectionService protectionService;
    private final RegionCreationService regionCreationService;
    private final RegionInviteService regionInviteService;
    private final FuelService fuelService;
    private final UpkeepService upkeepService;
    private final AdminRegionService adminRegionService;
    private final AdminRegionPresetService adminRegionPresetService;
    private final RegionUpgradeService regionUpgradeService;
    private final ConfirmationService confirmationService;
    private final RegionDeletionService regionDeletionService;
    private final RegionHomeService regionHomeService;
    private final CommandCooldownService commandCooldownService;
    private final RegionTeleportService regionTeleportService;
    private final PendingTeleportService pendingTeleportService;
    private final RegionBoundaryVisualizer boundaryVisualizer;
    private final CommandMapOverrideService commandMapOverrideService;
    private final GuiIconRegistry guiIconRegistry;
    private final VibePrivateAPI api;
    private final VibeRegionGuardApi vibeRegionGuardApi;

    VibePrivateServices(Builder builder) {
        configService = builder.configService;
        messageService = builder.messageService;
        databaseService = builder.databaseService;
        regionRepository = builder.regionRepository;
        regionAccessRepository = builder.regionAccessRepository;
        regionDepositRepository = builder.regionDepositRepository;
        regionHomeRepository = builder.regionHomeRepository;
        regionAccessService = builder.regionAccessService;
        regionManager = builder.regionManager;
        playerRegionCache = builder.playerRegionCache;
        protectionService = builder.protectionService;
        regionCreationService = builder.regionCreationService;
        regionInviteService = builder.regionInviteService;
        fuelService = builder.fuelService;
        upkeepService = builder.upkeepService;
        adminRegionService = builder.adminRegionService;
        adminRegionPresetService = builder.adminRegionPresetService;
        regionUpgradeService = builder.regionUpgradeService;
        confirmationService = builder.confirmationService;
        regionDeletionService = builder.regionDeletionService;
        regionHomeService = builder.regionHomeService;
        commandCooldownService = builder.commandCooldownService;
        regionTeleportService = builder.regionTeleportService;
        pendingTeleportService = builder.pendingTeleportService;
        boundaryVisualizer = builder.boundaryVisualizer;
        commandMapOverrideService = builder.commandMapOverrideService;
        guiIconRegistry = builder.guiIconRegistry;
        api = builder.api;
        vibeRegionGuardApi = builder.vibeRegionGuardApi;
    }

    static VibePrivateServices create(JavaPlugin plugin) {
        return VibePrivateServiceFactory.create(plugin);
    }

    void startRuntimeTasks() {
        fuelService.start();
        upkeepService.start();
    }

    void stopRuntimeTasks() {
        fuelService.stop();
        upkeepService.stop();
        pendingTeleportService.stop();
    }

    void closeStorage() {
        databaseService.close();
    }

    ConfigService configService() {
        return configService;
    }

    MessageService messageService() {
        return messageService;
    }

    DatabaseService databaseService() {
        return databaseService;
    }

    RegionRepository regionRepository() {
        return regionRepository;
    }

    RegionAccessRepository regionAccessRepository() {
        return regionAccessRepository;
    }

    RegionDepositRepository regionDepositRepository() {
        return regionDepositRepository;
    }

    RegionHomeRepository regionHomeRepository() {
        return regionHomeRepository;
    }

    RegionAccessService regionAccessService() {
        return regionAccessService;
    }

    RegionManager regionManager() {
        return regionManager;
    }

    PlayerRegionCache playerRegionCache() {
        return playerRegionCache;
    }

    ProtectionService protectionService() {
        return protectionService;
    }

    RegionCreationService regionCreationService() {
        return regionCreationService;
    }

    RegionInviteService regionInviteService() {
        return regionInviteService;
    }

    FuelService fuelService() {
        return fuelService;
    }

    UpkeepService upkeepService() {
        return upkeepService;
    }

    AdminRegionService adminRegionService() {
        return adminRegionService;
    }

    AdminRegionPresetService adminRegionPresetService() {
        return adminRegionPresetService;
    }

    RegionUpgradeService regionUpgradeService() {
        return regionUpgradeService;
    }

    RegionDeletionService regionDeletionService() {
        return regionDeletionService;
    }

    RegionHomeService regionHomeService() {
        return regionHomeService;
    }

    RegionTeleportService regionTeleportService() {
        return regionTeleportService;
    }

    PendingTeleportService pendingTeleportService() {
        return pendingTeleportService;
    }

    RegionBoundaryVisualizer boundaryVisualizer() {
        return boundaryVisualizer;
    }

    CommandCooldownService commandCooldownService() {
        return commandCooldownService;
    }

    ConfirmationService confirmationService() {
        return confirmationService;
    }

    CommandMapOverrideService commandMapOverrideService() {
        return commandMapOverrideService;
    }

    GuiIconRegistry guiIconRegistry() {
        return guiIconRegistry;
    }

    VibePrivateAPI api() {
        return api;
    }

    VibeRegionGuardApi vibeRegionGuardApi() {
        return vibeRegionGuardApi;
    }

    static final class Builder {
        ConfigService configService;
        MessageService messageService;
        DatabaseService databaseService;
        RegionRepository regionRepository;
        RegionAccessRepository regionAccessRepository;
        RegionDepositRepository regionDepositRepository;
        RegionHomeRepository regionHomeRepository;
        RegionLifecycleRepository regionLifecycleRepository;
        UpkeepRepository upkeepRepository;
        ProtectedChunkRepository protectedChunkRepository;
        RegionAccessService regionAccessService;
        RegionManager regionManager;
        PlayerRegionCache playerRegionCache;
        ProtectionService protectionService;
        RegionCreationService regionCreationService;
        RegionInviteService regionInviteService;
        RegionLifecycleService regionLifecycleService;
        RegionRelocationService regionRelocationService;
        RegionSelectionValidator regionSelectionValidator;
        FuelService fuelService;
        EconomyService economyService;
        UpkeepService upkeepService;
        AdminRegionService adminRegionService;
        AdminRegionPresetService adminRegionPresetService;
        RegionUpgradeService regionUpgradeService;
        ConfirmationService confirmationService;
        RegionDeletionService regionDeletionService;
        RegionEventDispatcher regionEventDispatcher;
        RegionHomeService regionHomeService;
        CommandCooldownService commandCooldownService;
        RegionTeleportService regionTeleportService;
        PendingTeleportService pendingTeleportService;
        ChunkProtectionService chunkProtectionService;
        RegionBoundaryVisualizer boundaryVisualizer;
        CommandMapOverrideService commandMapOverrideService;
        GuiIconRegistry guiIconRegistry;
        VibePrivateAPI api;
        VibeRegionGuardApi vibeRegionGuardApi;
    }
}
