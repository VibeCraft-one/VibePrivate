package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.service.RegionAccessService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;
import java.util.UUID;

final class RegionMembersMenuHandler {
    private final MessageService messageService;
    private final RegionAccessService regionAccessService;
    private final RegionMemberActionHandler memberActionHandler;
    private final RegionMenuNavigator navigator;

    RegionMembersMenuHandler(MessageService messageService, RegionAccessService regionAccessService,
                             RegionMemberActionHandler memberActionHandler, RegionMenuNavigator navigator) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
        this.memberActionHandler = Objects.requireNonNull(memberActionHandler, "memberActionHandler");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
    }

    void handleMembers(InventoryClickEvent event, RegionMembersMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(menu.getOwner().getUniqueId())) {
            return;
        }

        Region region = menu.getRegion();
        if (!RegionMenuPermissions.isPlayerManagedRegion(region)) {
            messageService.send(player, "members.unsupported-region");
            navigator.openRegionDetail(player, region);
            return;
        }

        if (event.getRawSlot() == RegionMembersMenu.BACK_SLOT) {
            navigator.openRegionDetail(player, region);
            return;
        }

        UUID editPlayerId = menu.getEditPlayerId(event.getRawSlot());
        if (editPlayerId != null) {
            navigator.openMemberFlags(player, region, editPlayerId);
            return;
        }

        UUID addPlayerId = menu.getAddPlayerId(event.getRawSlot());
        if (addPlayerId != null) {
            memberActionHandler.addMember(player, region, addPlayerId);
            navigator.openMembers(player, region);
            return;
        }

        UUID removePlayerId = menu.getRemovePlayerId(event.getRawSlot());
        if (removePlayerId != null) {
            memberActionHandler.removeMember(player, region, removePlayerId);
            navigator.openMembers(player, region);
        }
    }

    void handleMemberFlags(InventoryClickEvent event, RegionMemberFlagsMenu menu) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || !player.getUniqueId().equals(menu.getOwner().getUniqueId())) {
            return;
        }

        Region region = menu.getRegion();
        if (!RegionMenuPermissions.isPlayerManagedRegion(region)) {
            messageService.send(player, "members.unsupported-region");
            navigator.openRegionDetail(player, region);
            return;
        }

        UUID memberId = menu.getMemberId();
        if (!regionAccessService.isMember(region.getId(), memberId)) {
            messageService.send(player, "members.missing");
            navigator.openMembers(player, region);
            return;
        }

        if (event.getRawSlot() == RegionMemberFlagsMenu.BACK_SLOT) {
            navigator.openMembers(player, region);
            return;
        }

        if (event.getRawSlot() == RegionMemberFlagsMenu.REMOVE_SLOT) {
            memberActionHandler.removeMember(player, region, memberId);
            navigator.openMembers(player, region);
            return;
        }

        RegionFlag flag = menu.getFlag(event.getRawSlot());
        if (flag == null) {
            return;
        }

        memberActionHandler.toggleFlag(player, region, memberId, flag);
        navigator.openMemberFlags(player, region, memberId);
    }
}
