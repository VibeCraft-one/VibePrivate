package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.service.PendingTeleportService;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionTeleportService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

final class RegionMenuTeleportHandler {
    private final MessageService messageService;
    private final ConfigService configService;
    private final RegionTeleportService regionTeleportService;
    private final RegionHomeService regionHomeService;
    private final PendingTeleportService pendingTeleportService;

    RegionMenuTeleportHandler(MessageService messageService, ConfigService configService, RegionTeleportService regionTeleportService,
                              RegionHomeService regionHomeService, PendingTeleportService pendingTeleportService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.regionTeleportService = Objects.requireNonNull(regionTeleportService, "regionTeleportService");
        this.regionHomeService = Objects.requireNonNull(regionHomeService, "regionHomeService");
        this.pendingTeleportService = Objects.requireNonNull(pendingTeleportService, "pendingTeleportService");
    }

    void handleTeleport(Player player, Region region) {
        if (region.isAdmin()) {
            boolean teleported = regionTeleportService.teleportHome(player, region);
            messageService.send(player, teleported ? "region.home.success" : "region.home.failed");
            return;
        }

        if (region.getType() == RegionType.FARM) {
            if (!isInsideOwnHome(player)) {
                messageService.send(player, "farm.teleport.home-required");
                return;
            }

            pendingTeleportService.start(player, region, null, 10,
                    "farm.teleport.start", "farm.teleport.success", "farm.teleport.failed");
            return;
        }

        pendingTeleportService.start(player, region,
                region.getType() == RegionType.HOME ? regionHomeService.getHome(region).orElse(null) : null,
                configService.getHomeTeleportDelaySeconds(player),
                "region.teleport.start", "region.home.success", "region.home.failed");
    }

    void handleSetHome(Player player, Region region) {
        if (region.getType() != RegionType.HOME) {
            messageService.send(player, "home.set.failed");
            return;
        }

        messageService.send(player, regionHomeService.setHome(player, region)
                ? "home.set.success"
                : "home.set.failed");
    }

    private boolean isInsideOwnHome(Player player) {
        Optional<Region> homeRegion = regionHomeService.getPlayerHomeRegion(player);
        if (homeRegion.isEmpty()) {
            return false;
        }

        Location location = player.getLocation();
        World world = location.getWorld();
        return world != null && homeRegion.get().getBounds().contains(world.getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}
