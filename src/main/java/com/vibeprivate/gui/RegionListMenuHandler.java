package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

final class RegionListMenuHandler {
    private final MessageService messageService;
    private final RegionManager regionManager;
    private final RegionMenuNavigator navigator;

    RegionListMenuHandler(MessageService messageService, RegionManager regionManager,
                          RegionMenuNavigator navigator) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handle(InventoryClickEvent event, RegionListMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(menu.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getRawSlot() == RegionListMenu.BACK_SLOT) {
            navigator.openMain(player);
            return;
        }

        String regionId = menu.getRegionId(event.getRawSlot());
        if (regionId == null) {
            return;
        }

        regionManager.getRegion(regionId).ifPresentOrElse(region -> {
            if (!region.getOwnerId().equals(player.getUniqueId().toString())) {
                messageService.send(player, "gui.region-detail.not-owner");
                return;
            }

            navigator.openRegionDetail(player, region);
        }, () -> messageService.send(player, "gui.region-detail.missing"));
    }
}
