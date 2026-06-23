package com.vibeprivate.gui;

import com.vibeprivate.service.RegionCreationService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

final class MainMenuHandler {
    private final RegionCreationService regionCreationService;
    private final RegionMenuActionHandler actionHandler;
    private final RegionMenuNavigator navigator;

    MainMenuHandler(RegionCreationService regionCreationService, RegionMenuActionHandler actionHandler,
                    RegionMenuNavigator navigator) {
        this.regionCreationService = Objects.requireNonNull(regionCreationService, "regionCreationService");
        this.actionHandler = Objects.requireNonNull(actionHandler, "actionHandler");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handle(InventoryClickEvent event, PrivateMainMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(menu.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getRawSlot() == PrivateMainMenu.HOME_SLOT) {
            actionHandler.handleCreation(player, regionCreationService.createHomeRegion(player));
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == PrivateMainMenu.FARM_SLOT) {
            actionHandler.handleCreation(player, regionCreationService.createFarmRegion(player));
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == PrivateMainMenu.REGIONS_SLOT) {
            navigator.openRegionList(player);
            return;
        }

        if (event.getRawSlot() == PrivateMainMenu.MEMBER_REGIONS_SLOT) {
            navigator.openMemberRegionList(player);
            return;
        }

        if (event.getRawSlot() == PrivateMainMenu.INVITES_SLOT) {
            navigator.openInvites(player);
            return;
        }

        if (event.getRawSlot() == PrivateMainMenu.HELP_SLOT) {
            navigator.openHelp(player);
        }
    }
}
