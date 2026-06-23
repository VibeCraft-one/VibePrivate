package com.vibeprivate;

import com.vibeprivate.api.VibePrivateAPI;
import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.gui.GuiIconRegistry;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.protection.ProtectionService;
import com.vibeprivate.storage.DatabaseService;
import com.vibeprivate.storage.RegionAccessRepository;
import com.vibeprivate.storage.RegionDepositRepository;
import com.vibeprivate.storage.RegionHomeRepository;
import com.vibeprivate.storage.RegionRepository;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.AdminRegionPresetService;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.PendingTeleportService;
import com.vibeprivate.service.RegionDeletionService;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionTeleportService;
import com.vibeprivate.service.RegionUpgradeService;
import com.vibeprivate.visualization.RegionBoundaryVisualizer;
import org.bukkit.plugin.java.JavaPlugin;

public final class VibePrivatePlugin extends JavaPlugin {
    private VibePrivateServices services;

    @Override
    public void onEnable() {
        services = VibePrivateServices.create(this);
        new VibePrivateCommandRegistrar(this, services).register();
        services.commandMapOverrideService().claimPrimaryCommands("home", "sethome");
        new VibePrivateListenerRegistrar(this, services).register();
        services.startRuntimeTasks();
        getLogger().info(services.messageService().plain("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        if (services == null) {
            return;
        }

        services.stopRuntimeTasks();
        services.closeStorage();
        getLogger().info(services.messageService().plain("plugin.disabled"));
    }

    public ConfigService getConfigService() {
        return services.configService();
    }

    public MessageService getMessageService() {
        return services.messageService();
    }

    public DatabaseService getDatabaseService() {
        return services.databaseService();
    }

    public RegionRepository getRegionRepository() {
        return services.regionRepository();
    }

    public RegionAccessRepository getRegionAccessRepository() {
        return services.regionAccessRepository();
    }

    public RegionDepositRepository getRegionDepositRepository() {
        return services.regionDepositRepository();
    }

    public RegionHomeRepository getRegionHomeRepository() {
        return services.regionHomeRepository();
    }

    public RegionAccessService getRegionAccessService() {
        return services.regionAccessService();
    }

    public RegionManager getRegionManager() {
        return services.regionManager();
    }

    public PlayerRegionCache getPlayerRegionCache() {
        return services.playerRegionCache();
    }

    public ProtectionService getProtectionService() {
        return services.protectionService();
    }

    public RegionCreationService getRegionCreationService() {
        return services.regionCreationService();
    }

    public FuelService getFuelService() {
        return services.fuelService();
    }

    public AdminRegionService getAdminRegionService() {
        return services.adminRegionService();
    }

    public AdminRegionPresetService getAdminRegionPresetService() {
        return services.adminRegionPresetService();
    }

    public RegionUpgradeService getRegionUpgradeService() {
        return services.regionUpgradeService();
    }

    public RegionDeletionService getRegionDeletionService() {
        return services.regionDeletionService();
    }

    public RegionHomeService getRegionHomeService() {
        return services.regionHomeService();
    }

    public RegionTeleportService getRegionTeleportService() {
        return services.regionTeleportService();
    }

    public PendingTeleportService getPendingTeleportService() {
        return services.pendingTeleportService();
    }

    public RegionBoundaryVisualizer getBoundaryVisualizer() {
        return services.boundaryVisualizer();
    }

    public VibePrivateAPI getApi() {
        return services.api();
    }

    public GuiIconRegistry getGuiIconRegistry() {
        return services.guiIconRegistry();
    }
}
