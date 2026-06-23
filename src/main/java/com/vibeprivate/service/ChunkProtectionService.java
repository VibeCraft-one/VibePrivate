package com.vibeprivate.service;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.model.Region;
import com.vibeprivate.storage.ProtectedChunkRepository;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Objects;

public final class ChunkProtectionService {
    private final Plugin plugin;
    private final ConfigService configService;
    private final ProtectedChunkRepository protectedChunkRepository;

    public ChunkProtectionService(Plugin plugin, ConfigService configService,
                                  ProtectedChunkRepository protectedChunkRepository) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.protectedChunkRepository = Objects.requireNonNull(protectedChunkRepository, "protectedChunkRepository");
    }

    public void protectRegion(Region region) {
        Objects.requireNonNull(region, "region");
        if (!configService.isChunkKeeperEnabled() || region.isAdmin()) {
            return;
        }

        try {
            int protectedChunks = protectedChunkRepository.protectRegionChunks(
                    region,
                    configService.getChunkKeeperBufferChunks(),
                    configService.getChunkKeeperMaxChunksPerRegion()
            );
            plugin.getLogger().fine("[ChunkKeeper] protected " + protectedChunks + " chunks for region " + region.getId());
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("[ChunkKeeper] Failed to protect chunks for region " + region.getId()
                    + ": " + exception.getMessage());
        }
    }

    public void unprotectRegion(Region region) {
        Objects.requireNonNull(region, "region");
        if (!configService.isChunkKeeperEnabled() || region.isAdmin()) {
            return;
        }

        try {
            int deletedChunks = protectedChunkRepository.deleteRegionChunks(region.getId());
            plugin.getLogger().fine("[ChunkKeeper] released " + deletedChunks + " chunks for region " + region.getId());
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("[ChunkKeeper] Failed to release chunks for region " + region.getId()
                    + ": " + exception.getMessage());
        }
    }

    public void protectExistingRegions(Collection<Region> regions) {
        Objects.requireNonNull(regions, "regions");
        if (!configService.isChunkKeeperEnabled()) {
            return;
        }

        int regionsProtected = 0;
        for (Region region : regions) {
            if (region.isAdmin()) {
                continue;
            }
            if (protectedChunkRepository.hasProtectedChunks(region.getId())) {
                continue;
            }
            protectRegion(region);
            regionsProtected++;
        }

        plugin.getLogger().info("[ChunkKeeper] Synced protected chunks for " + regionsProtected + " VibePrivate regions.");
    }
}
