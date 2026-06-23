package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.service.AdminRegionService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

final class AdminMenuHandler {
    private final MessageService messageService;
    private final RegionManager regionManager;
    private final AdminRegionService adminRegionService;
    private final RegionMenuActionHandler actionHandler;
    private final RegionMenuNavigator navigator;

    AdminMenuHandler(MessageService messageService, RegionManager regionManager,
                     AdminRegionService adminRegionService, RegionMenuActionHandler actionHandler,
                     RegionMenuNavigator navigator) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.adminRegionService = Objects.requireNonNull(adminRegionService, "adminRegionService");
        this.actionHandler = Objects.requireNonNull(actionHandler, "actionHandler");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handleMain(InventoryClickEvent event, AdminMainMenu menu) {
        event.setCancelled(true);
        if (!isAllowedAdmin(event, menu.getPlayer())) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getRawSlot() == AdminMainMenu.POS1_SLOT) {
            adminRegionService.setPos1(player);
            messageService.send(player, "admin.pos1-set");
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == AdminMainMenu.POS2_SLOT) {
            adminRegionService.setPos2(player);
            messageService.send(player, "admin.pos2-set");
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == AdminMainMenu.CREATE_SLOT) {
            actionHandler.handleAdminCreate(player, adminRegionService.create(player, "ADMIN"));
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == AdminMainMenu.NO_CLAIM_SLOT) {
            actionHandler.handleAdminNoClaim(player, adminRegionService.createNoClaim(player, 150), 150);
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == AdminMainMenu.REGIONS_SLOT) {
            navigator.openAdminRegionList(player);
            return;
        }

        if (event.getRawSlot() == AdminMainMenu.PLAYER_REGIONS_SLOT) {
            navigator.openAdminPlayerList(player);
            return;
        }

        if (event.getRawSlot() == AdminMainMenu.PLAYER_MENU_SLOT) {
            navigator.openMain(player);
            return;
        }

        if (event.getRawSlot() == AdminMainMenu.HELP_SLOT) {
            navigator.openHelp(player);
        }
    }

    void handleRegionList(InventoryClickEvent event, AdminRegionListMenu menu) {
        event.setCancelled(true);
        if (!isAllowedAdmin(event, menu.getPlayer())) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getRawSlot() == AdminRegionListMenu.BACK_SLOT) {
            navigator.openAdminMain(player);
            return;
        }

        String regionId = menu.getRegionId(event.getRawSlot());
        if (regionId == null) {
            return;
        }

        regionManager.getRegion(regionId).ifPresentOrElse(region -> {
            if (!region.isAdmin()) {
                messageService.send(player, "gui.region-detail.missing");
                return;
            }

            navigator.openRegionDetail(player, region);
        }, () -> messageService.send(player, "gui.region-detail.missing"));
    }

    void handlePlayerList(InventoryClickEvent event, AdminPlayerListMenu menu) {
        event.setCancelled(true);
        if (!isAllowedAdmin(event, menu.getPlayer())) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getRawSlot() == AdminPlayerListMenu.BACK_SLOT) {
            navigator.openAdminMain(player);
            return;
        }

        String ownerId = menu.getOwnerId(event.getRawSlot());
        if (ownerId != null) {
            navigator.openAdminPlayerRegionList(player, ownerId);
        }
    }

    void handlePlayerRegionList(InventoryClickEvent event, AdminPlayerRegionListMenu menu) {
        event.setCancelled(true);
        if (!isAllowedAdmin(event, menu.getPlayer())) {
            return;
        }

        if (event.getRawSlot() == AdminPlayerRegionListMenu.BACK_SLOT) {
            navigator.openAdminPlayerList((Player) event.getWhoClicked());
        }
    }

    private boolean isAllowedAdmin(InventoryClickEvent event, Player menuPlayer) {
        return event.getWhoClicked() instanceof Player player
                && player.getUniqueId().equals(menuPlayer.getUniqueId())
                && RegionMenuPermissions.isAdminOperator(player);
    }
}
