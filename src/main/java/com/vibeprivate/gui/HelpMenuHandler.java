package com.vibeprivate.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

final class HelpMenuHandler {
    private final RegionMenuNavigator navigator;

    HelpMenuHandler(RegionMenuNavigator navigator) {
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handle(InventoryClickEvent event, HelpMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(menu.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getRawSlot() == HelpMenu.BACK_SLOT) {
            navigator.openMain(player);
        }
    }
}
