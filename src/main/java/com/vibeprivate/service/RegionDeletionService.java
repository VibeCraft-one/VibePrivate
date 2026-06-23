package com.vibeprivate.service;

import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

public final class RegionDeletionService {
    private static final String BYPASS_PERMISSION = "vibeprivate.admin";

    private final RegionManager regionManager;
    private final RegionUpgradeService upgradeService;
    private final PlayerRegionCache playerRegionCache;
    private final ConfirmationService confirmationService;

    public RegionDeletionService(RegionManager regionManager, RegionUpgradeService upgradeService,
                                 PlayerRegionCache playerRegionCache, ConfirmationService confirmationService) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.upgradeService = Objects.requireNonNull(upgradeService, "upgradeService");
        this.playerRegionCache = Objects.requireNonNull(playerRegionCache, "playerRegionCache");
        this.confirmationService = Objects.requireNonNull(confirmationService, "confirmationService");
    }

    public DeletionResult deleteOwned(Player player, Region region) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");
        if (!region.getOwnerId().equals(player.getUniqueId().toString())) {
            return DeletionResult.status(DeletionStatus.NOT_ALLOWED);
        }

        return deleteWithConfirmation(player, region);
    }

    public DeletionResult deleteAsAdmin(Player player, Region region) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");
        if (!player.isOp() && !player.hasPermission(BYPASS_PERMISSION)) {
            return DeletionResult.status(DeletionStatus.NOT_ALLOWED);
        }

        return deleteWithConfirmation(player, region);
    }

    private DeletionResult deleteWithConfirmation(Player player, Region region) {
        int remaining = confirmationService.confirm(player.getUniqueId(), "delete-region", region.getId());
        if (remaining > 0) {
            return DeletionResult.confirmRequired(remaining);
        }

        try {
            Region current = regionManager.getRegion(region.getId()).orElse(null);
            if (current == null) {
                return DeletionResult.status(DeletionStatus.NOT_FOUND);
            }

            dropDeposits(current);
            upgradeService.clearDeposits(current.getId());
            regionManager.removeRegion(current.getId());
            playerRegionCache.clear();
            return DeletionResult.status(DeletionStatus.DELETED);
        } catch (RuntimeException exception) {
            return DeletionResult.status(DeletionStatus.FAILED);
        }
    }

    private void dropDeposits(Region region) {
        Map<Material, Integer> deposits = upgradeService.getDeposits(region.getId());
        if (deposits.isEmpty()) {
            return;
        }

        Location location = getDropLocation(region);
        if (location == null || location.getWorld() == null) {
            return;
        }

        for (Map.Entry<Material, Integer> entry : deposits.entrySet()) {
            int remaining = entry.getValue();
            int maxStackSize = entry.getKey().getMaxStackSize();
            while (remaining > 0) {
                int amount = Math.min(maxStackSize, remaining);
                location.getWorld().dropItemNaturally(location, new ItemStack(entry.getKey(), amount));
                remaining -= amount;
            }
        }
    }

    private Location getDropLocation(Region region) {
        World world = Bukkit.getWorld(region.getWorldName());
        if (world == null) {
            return null;
        }

        if (region.getCenterX() != null && region.getCenterZ() != null) {
            int y = world.getHighestBlockYAt(region.getCenterX(), region.getCenterZ()) + 1;
            return new Location(world, region.getCenterX() + 0.5, y, region.getCenterZ() + 0.5);
        }

        int x = Math.floorDiv(region.getBounds().getMinX() + region.getBounds().getMaxX(), 2);
        int z = Math.floorDiv(region.getBounds().getMinZ() + region.getBounds().getMaxZ(), 2);
        int y = world.getHighestBlockYAt(x, z) + 1;
        return new Location(world, x + 0.5, y, z + 0.5);
    }
}
