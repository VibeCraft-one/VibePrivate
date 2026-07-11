package com.vibeprivate.service;

import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class RegionDeletionService {
    private static final String BYPASS_PERMISSION = "vibeprivate.admin";

    private final RegionManager regionManager;
    private final RegionUpgradeService upgradeService;
    private final PlayerRegionCache playerRegionCache;
    private final ConfirmationService confirmationService;
    private final Function<Region, Location> dropLocationResolver;
    private final BiFunction<Material, Integer, ItemStack> itemFactory;
    private final ToIntFunction<Material> maxStackSizeProvider;

    public RegionDeletionService(RegionManager regionManager, RegionUpgradeService upgradeService,
                                 PlayerRegionCache playerRegionCache, ConfirmationService confirmationService) {
        this(regionManager, upgradeService, playerRegionCache, confirmationService, null);
    }

    RegionDeletionService(RegionManager regionManager, RegionUpgradeService upgradeService,
                          PlayerRegionCache playerRegionCache, ConfirmationService confirmationService,
                          Function<Region, Location> dropLocationResolver) {
        this(regionManager, upgradeService, playerRegionCache, confirmationService, dropLocationResolver,
                ItemStack::new, Material::getMaxStackSize);
    }

    RegionDeletionService(RegionManager regionManager, RegionUpgradeService upgradeService,
                          PlayerRegionCache playerRegionCache, ConfirmationService confirmationService,
                          Function<Region, Location> dropLocationResolver,
                          BiFunction<Material, Integer, ItemStack> itemFactory,
                          ToIntFunction<Material> maxStackSizeProvider) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.upgradeService = Objects.requireNonNull(upgradeService, "upgradeService");
        this.playerRegionCache = Objects.requireNonNull(playerRegionCache, "playerRegionCache");
        this.confirmationService = Objects.requireNonNull(confirmationService, "confirmationService");
        this.dropLocationResolver = dropLocationResolver == null ? this::getDropLocation : dropLocationResolver;
        this.itemFactory = Objects.requireNonNull(itemFactory, "itemFactory");
        this.maxStackSizeProvider = Objects.requireNonNull(maxStackSizeProvider, "maxStackSizeProvider");
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

        List<Item> droppedDeposits = List.of();
        try {
            Region current = regionManager.getRegion(region.getId()).orElse(null);
            if (current == null) {
                return DeletionResult.status(DeletionStatus.NOT_FOUND);
            }

            Map<Material, Integer> deposits = upgradeService.getDeposits(current.getId());
            Location dropLocation = null;
            if (!deposits.isEmpty()) {
                dropLocation = dropLocationResolver.apply(current);
                if (dropLocation == null || dropLocation.getWorld() == null) {
                    return DeletionResult.status(DeletionStatus.FAILED);
                }
            }

            droppedDeposits = dropDeposits(deposits, dropLocation);
            if (regionManager.removeRegion(current.getId()).isEmpty()) {
                removeDroppedDeposits(droppedDeposits);
                return DeletionResult.status(DeletionStatus.NOT_FOUND);
            }

            upgradeService.forgetLoadedDeposits(current.getId());
            playerRegionCache.clear();
            return DeletionResult.status(DeletionStatus.DELETED);
        } catch (RuntimeException exception) {
            removeDroppedDeposits(droppedDeposits);
            return DeletionResult.status(DeletionStatus.FAILED);
        }
    }

    private List<Item> dropDeposits(Map<Material, Integer> deposits, Location location) {
        List<Item> droppedItems = new ArrayList<>();
        if (location == null || location.getWorld() == null) {
            return droppedItems;
        }

        try {
            for (Map.Entry<Material, Integer> entry : deposits.entrySet()) {
                int remaining = entry.getValue();
                int maxStackSize = maxStackSizeProvider.applyAsInt(entry.getKey());
                while (remaining > 0) {
                    int amount = Math.min(maxStackSize, remaining);
                    Item dropped = location.getWorld().dropItemNaturally(location, itemFactory.apply(entry.getKey(), amount));
                    if (dropped != null) {
                        droppedItems.add(dropped);
                    }
                    remaining -= amount;
                }
            }
            return droppedItems;
        } catch (RuntimeException exception) {
            removeDroppedDeposits(droppedItems);
            throw exception;
        }
    }

    private void removeDroppedDeposits(List<Item> droppedDeposits) {
        for (Item item : droppedDeposits) {
            try {
                item.remove();
            } catch (RuntimeException ignored) {
                // Best-effort rollback after a failed region deletion path.
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
