package com.vibeprivate.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

final class RegionInviteMenuHandler {
    private final RegionMemberActionHandler memberActionHandler;
    private final RegionMenuNavigator navigator;

    RegionInviteMenuHandler(RegionMemberActionHandler memberActionHandler, RegionMenuNavigator navigator) {
        this.memberActionHandler = Objects.requireNonNull(memberActionHandler, "memberActionHandler");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handleMemberRegionListMenu(InventoryClickEvent event, MemberRegionListMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(menu.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getRawSlot() == MemberRegionListMenu.BACK_SLOT) {
            navigator.openMain(player);
        }
    }

    void handleInvitesMenu(InventoryClickEvent event, RegionInvitesMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(menu.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getRawSlot() == RegionInvitesMenu.BACK_SLOT) {
            navigator.openMain(player);
            return;
        }

        String regionId = menu.getRegionId(event.getRawSlot());
        if (regionId == null) {
            return;
        }

        if (event.isRightClick()) {
            memberActionHandler.declineInvite(player, regionId);
        } else {
            memberActionHandler.acceptInvite(player, regionId);
        }
        navigator.openInvites(player);
    }
}
