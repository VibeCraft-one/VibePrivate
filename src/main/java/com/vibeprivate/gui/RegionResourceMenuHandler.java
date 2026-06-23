package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

final class RegionResourceMenuHandler {
    private final RegionManager regionManager;
    private final RegionMenuActionHandler actionHandler;
    private final RegionMenuNavigator navigator;

    RegionResourceMenuHandler(RegionManager regionManager, RegionMenuActionHandler actionHandler,
                              RegionMenuNavigator navigator) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.actionHandler = Objects.requireNonNull(actionHandler, "actionHandler");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handleFuel(InventoryClickEvent event, RegionFuelMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !RegionMenuPermissions.canManageRegion(player, menu.getRegion())) {
            return;
        }

        Region region = menu.getRegion();
        if (event.getRawSlot() == RegionFuelMenu.BACK_SLOT) {
            navigator.openRegionDetail(player, region);
            return;
        }

        if (event.getRawSlot() == RegionFuelMenu.ADD_SLOT) {
            actionHandler.handleFuel(player, region);
            navigator.openFuel(player, region);
        }
    }

    void handleDeposit(InventoryClickEvent event, RegionDepositMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !RegionMenuPermissions.canManageRegion(player, menu.getRegion())) {
            return;
        }

        Region region = menu.getRegion();
        if (event.getRawSlot() == RegionDepositMenu.BACK_SLOT) {
            navigator.openRegionDetail(player, region);
            return;
        }

        if (event.getRawSlot() == RegionDepositMenu.ADD_SLOT) {
            actionHandler.handleUpgrade(player, region);
            Region refreshed = regionManager.getRegion(region.getId()).orElse(region);
            navigator.openDeposit(player, refreshed);
            return;
        }

        if (event.getRawSlot() == RegionDepositMenu.WITHDRAW_SLOT) {
            actionHandler.handleWithdraw(player, region);
            navigator.openDeposit(player, region);
        }
    }
}
