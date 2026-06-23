package com.vibeprivate;

import com.vibeprivate.gui.PrivateMenuListener;
import com.vibeprivate.listener.PendingTeleportListener;
import com.vibeprivate.listener.RegionHomeListener;
import com.vibeprivate.listener.RegionProtectionListener;
import com.vibeprivate.listener.RegionTrackingListener;

import java.util.Objects;

final class VibePrivateListenerRegistrar {
    private final VibePrivatePlugin plugin;
    private final VibePrivateServices services;

    VibePrivateListenerRegistrar(VibePrivatePlugin plugin, VibePrivateServices services) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.services = Objects.requireNonNull(services, "services");
    }

    void register() {
        plugin.getServer().getPluginManager().registerEvents(
                new RegionTrackingListener(services.regionManager(), services.playerRegionCache(),
                        services.boundaryVisualizer()), plugin);
        plugin.getServer().getPluginManager().registerEvents(
                new PendingTeleportListener(services.pendingTeleportService()), plugin);
        plugin.getServer().getPluginManager().registerEvents(
                new RegionProtectionListener(services.protectionService(), services.messageService()), plugin);
        plugin.getServer().getPluginManager().registerEvents(
                new RegionHomeListener(services.regionManager(), services.regionHomeService(),
                        services.messageService()), plugin);
        plugin.getServer().getPluginManager().registerEvents(
                new PrivateMenuListener(services.messageService(), services.configService(), services.regionManager(),
                        services.regionAccessService(), services.regionCreationService(), services.fuelService(),
                        services.regionInviteService(), services.regionUpgradeService(), services.regionDeletionService(),
                        services.regionTeleportService(), services.regionHomeService(),
                        services.pendingTeleportService(), services.adminRegionService(),
                        services.adminRegionPresetService(), services.confirmationService()), plugin);
    }
}
