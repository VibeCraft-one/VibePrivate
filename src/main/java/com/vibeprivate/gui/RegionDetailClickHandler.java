package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.service.AdminRegionPreset;
import com.vibeprivate.service.AdminRegionPresetService;
import com.vibeprivate.service.ConfirmationService;
import com.vibeprivate.service.RegionAccessService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;
import java.util.Objects;

final class RegionDetailClickHandler {
    private final MessageService messageService;
    private final RegionManager regionManager;
    private final RegionAccessService regionAccessService;
    private final AdminRegionPresetService adminRegionPresetService;
    private final ConfirmationService confirmationService;
    private final RegionMenuActionHandler actionHandler;
    private final RegionMenuTeleportHandler teleportHandler;
    private final RegionMenuNavigator navigator;

    RegionDetailClickHandler(MessageService messageService, RegionManager regionManager,
                             RegionAccessService regionAccessService,
                             AdminRegionPresetService adminRegionPresetService,
                             ConfirmationService confirmationService,
                             RegionMenuActionHandler actionHandler,
                             RegionMenuTeleportHandler teleportHandler,
                             RegionMenuNavigator navigator) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
        this.adminRegionPresetService = Objects.requireNonNull(adminRegionPresetService, "adminRegionPresetService");
        this.confirmationService = Objects.requireNonNull(confirmationService, "confirmationService");
        this.actionHandler = Objects.requireNonNull(actionHandler, "actionHandler");
        this.teleportHandler = Objects.requireNonNull(teleportHandler, "teleportHandler");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handle(InventoryClickEvent event, RegionDetailMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Region region = menu.getRegion();
        if (!RegionMenuPermissions.canManageRegion(player, region)) {
            messageService.send(player, "gui.region-detail.not-owner");
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        if (slot == RegionDetailMenu.BACK_SLOT) {
            openBack(player, region);
            return;
        }

        if (slot == RegionDetailMenu.HOME_SLOT) {
            teleportHandler.handleTeleport(player, region);
            player.closeInventory();
            return;
        }

        if (slot == RegionDetailMenu.MEMBERS_SLOT) {
            openMembers(player, region);
            return;
        }

        if (slot == RegionDetailMenu.FUEL_SLOT) {
            handleFuel(player, region);
            return;
        }

        if (slot == RegionDetailMenu.UPGRADE_SLOT) {
            handleUpgrade(player, region);
            return;
        }

        if (slot == RegionDetailMenu.SET_HOME_SLOT) {
            teleportHandler.handleSetHome(player, region);
            navigator.openRegionDetail(player, region);
            return;
        }

        if (slot == RegionDetailMenu.WITHDRAW_SLOT) {
            handleWithdraw(player, region);
            return;
        }

        if (slot == RegionDetailMenu.DELETE_SLOT) {
            handleDelete(player, region);
            return;
        }

        AdminRegionPreset preset = menu.getPreset(slot);
        if (preset != null && region.isAdmin()) {
            handlePreset(player, region, preset);
            return;
        }

        RegionFlag flag = menu.getFlag(slot);
        if (flag != null) {
            toggleFlag(player, region, flag);
        }
    }

    private void openBack(Player player, Region region) {
        if (region.isAdmin() && RegionMenuPermissions.isAdminOperator(player)) {
            navigator.openAdminRegionList(player);
        } else {
            navigator.openRegionList(player);
        }
    }

    private void openMembers(Player player, Region region) {
        if (region.isAdmin()) {
            messageService.send(player, "admin.flags.guests");
            return;
        }

        if (!RegionMenuPermissions.isPlayerManagedRegion(region)) {
            messageService.send(player, "members.unsupported-region");
            return;
        }

        navigator.openMembers(player, region);
    }

    private void handleFuel(Player player, Region region) {
        if (region.isAdmin()) {
            return;
        }

        navigator.openFuel(player, region);
    }

    private void handleUpgrade(Player player, Region region) {
        if (region.isAdmin()) {
            return;
        }

        navigator.openDeposit(player, region);
    }

    private void handleWithdraw(Player player, Region region) {
        if (region.isAdmin()) {
            return;
        }

        actionHandler.handleWithdraw(player, region);
        navigator.openRegionDetail(player, region);
    }

    private void handleDelete(Player player, Region region) {
        actionHandler.handleDelete(player, region, RegionMenuPermissions.isAdminOperator(player));
        if (regionManager.getRegion(region.getId()).isPresent()) {
            navigator.openRegionDetail(player, region);
        } else if (region.isAdmin() && RegionMenuPermissions.isAdminOperator(player)) {
            navigator.openAdminRegionList(player);
        } else {
            navigator.openRegionList(player);
        }
    }

    private void handlePreset(Player player, Region region, AdminRegionPreset preset) {
        String presetName = messageService.get("gui.admin.preset." + preset.name().toLowerCase() + ".name");
        int remaining = confirmationService.confirm(player.getUniqueId(), "admin-preset",
                region.getId() + ":" + preset.name(), 2);
        if (remaining > 0) {
            messageService.send(player, "admin.preset.confirm", Map.of("preset", presetName));
            return;
        }

        adminRegionPresetService.applyPreset(region, preset);
        messageService.send(player, "admin.preset.applied", Map.of("preset", presetName));
        navigator.openRegionDetail(player, region);
    }

    private void toggleFlag(Player player, Region region, RegionFlag flag) {
        boolean enabled = regionAccessService.toggleDefaultFlag(region.getId(), flag);
        String messageKey = region.isAdmin()
                ? (enabled ? "gui.region-detail.flag-now-guest-enabled" : "gui.region-detail.flag-now-guest-disabled")
                : (enabled ? "gui.region-detail.flag-now-enabled" : "gui.region-detail.flag-now-disabled");
        messageService.send(player, messageKey,
                Map.of("flag", messageService.get(FlagView.nameKey(flag))));
        navigator.openRegionDetail(player, region);
    }
}
