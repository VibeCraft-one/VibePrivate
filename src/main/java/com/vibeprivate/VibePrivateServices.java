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
import com.vibeprivate.service.EconomyService;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.PendingTeleportService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.RegionDeletionService;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionInviteService;
import com.vibeprivate.service.RegionLifecycleService;
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

public final class VibePrivateServices {
    private final ConfigService configService;
    private final MessageService messageService;
    private final DatabaseService databaseService;
    private final RegionRepository regionRepository;
    private final RegionAccessRepository regionAccessRepository;
    private final RegionDepositRepository regionDepositRepository;
    private final RegionHomeRepository regionHomeRepository;
    private final RegionLifecycleRepository regionLifecycleRepository;
    private final UpkeepRepository upkeepRepository;
    private final ProtectedChunkRepository protectedChunkRepository;
    private final RegionAccessService regionAccessService;
    private final RegionManager regionManager;
    private final PlayerRegionCache playerRegionCache;
    private final ProtectionService protectionService;
    private final RegionCreationService regionCreationService;
    private final RegionInviteService regionInviteService;
    private final RegionLifecycleService regionLifecycleService;
    private final RegionSelectionValidator regionSelectionValidator;
    private final FuelService fuelService;
    private final EconomyService economyService;
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
    private final ChunkProtectionService chunkProtectionService;
    private final RegionBoundaryVisualizer boundaryVisualizer;
    private final CommandMapOverrideService commandMapOverrideService;
    private final GuiIconRegistry guiIconRegistry;
    private final VibePrivateAPI api;

    VibePrivateServices(Builder builder) {
        configService = builder.configService;
        messageService = builder.messageService;
        databaseService = builder.databaseService;
        regionRepository = builder.regionRepository;
        regionAccessRepository = builder.regionAccessRepository;
        regionDepositRepository = builder.regionDepositRepository;
        regionHomeRepository = builder.regionHomeRepository;
        regionLifecycleRepository = builder.regionLifecycleRepository;
        upkeepRepository = builder.upkeepRepository;
        protectedChunkRepository = builder.protectedChunkRepository;
        regionAccessService = builder.regionAccessService;
        regionManager = builder.regionManager;
        playerRegionCache = builder.playerRegionCache;
        protectionService = builder.protectionService;
        regionCreationService = builder.regionCreationService;
        regionInviteService = builder.regionInviteService;
        regionLifecycleService = builder.regionLifecycleService;
        regionSelectionValidator = builder.regionSelectionValidator;
        fuelService = builder.fuelService;
        economyService = builder.economyService;
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
        chunkProtectionService = builder.chunkProtectionService;
        boundaryVisualizer = builder.boundaryVisualizer;
        commandMapOverrideService = builder.commandMapOverrideService;
        guiIconRegistry = builder.guiIconRegistry;
        api = builder.api;
    }

    public static VibePrivateServices create(JavaPlugin plugin) {
        return VibePrivateServiceFactory.create(plugin);
    }

    public void startRuntimeTasks() {
        fuelService.start();
        upkeepService.start();
    }

    public void stopRuntimeTasks() {
        fuelService.stop();
        upkeepService.stop();
        pendingTeleportService.stop();
    }

    public void closeStorage() {
        databaseService.close();
    }

    public ConfigService configService() {
        return configService;
    }

    public MessageService messageService() {
        return messageService;
    }

    public DatabaseService databaseService() {
        return databaseService;
    }

    public RegionRepository regionRepository() {
        return regionRepository;
    }

    public RegionAccessRepository regionAccessRepository() {
        return regionAccessRepository;
    }

    public RegionDepositRepository regionDepositRepository() {
        return regionDepositRepository;
    }

    public RegionHomeRepository regionHomeRepository() {
        return regionHomeRepository;
    }

    public UpkeepRepository upkeepRepository() {
        return upkeepRepository;
    }

    public RegionLifecycleRepository regionLifecycleRepository() {
        return regionLifecycleRepository;
    }

    public RegionAccessService regionAccessService() {
        return regionAccessService;
    }

    public RegionManager regionManager() {
        return regionManager;
    }

    public PlayerRegionCache playerRegionCache() {
        return playerRegionCache;
    }

    public ProtectionService protectionService() {
        return protectionService;
    }

    public RegionCreationService regionCreationService() {
        return regionCreationService;
    }

    public RegionInviteService regionInviteService() {
        return regionInviteService;
    }

    public RegionLifecycleService regionLifecycleService() {
        return regionLifecycleService;
    }

    public RegionSelectionValidator regionSelectionValidator() {
        return regionSelectionValidator;
    }

    public FuelService fuelService() {
        return fuelService;
    }

    public EconomyService economyService() {
        return economyService;
    }

    public UpkeepService upkeepService() {
        return upkeepService;
    }

    public AdminRegionService adminRegionService() {
        return adminRegionService;
    }

    public AdminRegionPresetService adminRegionPresetService() {
        return adminRegionPresetService;
    }

    public RegionUpgradeService regionUpgradeService() {
        return regionUpgradeService;
    }

    public RegionDeletionService regionDeletionService() {
        return regionDeletionService;
    }

    public RegionHomeService regionHomeService() {
        return regionHomeService;
    }

    public RegionTeleportService regionTeleportService() {
        return regionTeleportService;
    }

    public PendingTeleportService pendingTeleportService() {
        return pendingTeleportService;
    }

    public RegionBoundaryVisualizer boundaryVisualizer() {
        return boundaryVisualizer;
    }

    public CommandCooldownService commandCooldownService() {
        return commandCooldownService;
    }

    public ConfirmationService confirmationService() {
        return confirmationService;
    }

    public CommandMapOverrideService commandMapOverrideService() {
        return commandMapOverrideService;
    }

    public GuiIconRegistry guiIconRegistry() {
        return guiIconRegistry;
    }

    public VibePrivateAPI api() {
        return api;
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
        RegionSelectionValidator regionSelectionValidator;
        FuelService fuelService;
        EconomyService economyService;
        UpkeepService upkeepService;
        AdminRegionService adminRegionService;
        AdminRegionPresetService adminRegionPresetService;
        RegionUpgradeService regionUpgradeService;
        ConfirmationService confirmationService;
        RegionDeletionService regionDeletionService;
        RegionHomeService regionHomeService;
        CommandCooldownService commandCooldownService;
        RegionTeleportService regionTeleportService;
        PendingTeleportService pendingTeleportService;
        ChunkProtectionService chunkProtectionService;
        RegionBoundaryVisualizer boundaryVisualizer;
        CommandMapOverrideService commandMapOverrideService;
        GuiIconRegistry guiIconRegistry;
        VibePrivateAPI api;
    }
}
