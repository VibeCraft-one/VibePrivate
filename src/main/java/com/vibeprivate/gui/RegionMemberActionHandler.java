package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionInviteService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class RegionMemberActionHandler {
    private final MessageService messageService;
    private final RegionAccessService regionAccessService;
    private final RegionInviteService regionInviteService;

    RegionMemberActionHandler(MessageService messageService, RegionAccessService regionAccessService,
                              RegionInviteService regionInviteService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
        this.regionInviteService = Objects.requireNonNull(regionInviteService, "regionInviteService");
    }

    void addMember(Player owner, Region region, UUID playerId) {
        String targetName = playerName(playerId);
        RegionInviteService.InviteStatus status = regionInviteService.invite(owner, region, playerId);
        switch (status) {
            case SENT -> {
                messageService.send(owner, "members.invite.sent", Map.of("player", targetName));
                notifyOnline(playerId, "members.invite.received", owner, region);
            }
            case ALREADY_MEMBER -> messageService.send(owner, "members.invite.already-member", Map.of("player", targetName));
            case ALREADY_PENDING -> messageService.send(owner, "members.invite.already-pending", Map.of("player", targetName));
            case TARGET_OFFLINE -> messageService.send(owner, "members.invite.offline", Map.of("player", targetName));
            case SELF -> messageService.send(owner, "members.invite.self");
        }
    }

    void acceptInvite(Player player, String regionId) {
        RegionInviteService.AcceptStatus status = regionInviteService.accept(player.getUniqueId(), regionId);
        switch (status) {
            case ACCEPTED -> messageService.send(player, "members.invite.accepted");
            case ALREADY_MEMBER -> messageService.send(player, "members.invite.already-joined");
            case REGION_MISSING -> messageService.send(player, "members.invite.region-missing");
            case MISSING -> messageService.send(player, "members.invite.missing");
        }
    }

    void declineInvite(Player player, String regionId) {
        boolean declined = regionInviteService.decline(player.getUniqueId(), regionId);
        messageService.send(player, declined ? "members.invite.declined" : "members.invite.missing");
    }

    void removeMember(Player owner, Region region, UUID playerId) {
        regionAccessService.removeMember(region.getId(), playerId);
        String targetName = playerName(playerId);
        messageService.send(owner, "members.remove.success", Map.of("player", targetName));
        notifyOnline(playerId, "members.remove.notify", owner, region);
    }

    void toggleFlag(Player owner, Region region, UUID memberId, RegionFlag flag) {
        boolean enabled = regionAccessService.toggleMemberFlag(region, memberId, flag);
        messageService.send(owner, enabled ? "members.flag.enabled" : "members.flag.disabled", Map.of(
                "player", playerName(memberId),
                "flag", messageService.get(FlagView.nameKey(flag))
        ));
    }

    String playerName(UUID playerId) {
        Player online = Bukkit.getPlayer(playerId);
        if (online != null) {
            return online.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        String name = offlinePlayer.getName();
        return name == null ? playerId.toString().substring(0, 8) : name;
    }

    private void notifyOnline(UUID playerId, String messageKey, Player owner, Region region) {
        Player target = Bukkit.getPlayer(playerId);
        if (target == null) {
            return;
        }

        messageService.send(target, messageKey, Map.of(
                "owner", owner.getName(),
                "region", region.getName()
        ));
    }
}
