package com.vibeprivate.service;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RegionInviteService {
    private static final long INVITE_TTL_MILLIS = 10L * 60L * 1000L;

    private final RegionManager regionManager;
    private final RegionAccessService regionAccessService;
    private final Map<UUID, Map<String, PendingInvite>> invitesByTarget = new HashMap<>();

    public RegionInviteService(RegionManager regionManager, RegionAccessService regionAccessService) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
    }

    public InviteStatus invite(Player owner, Region region, UUID targetId) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(targetId, "targetId");
        cleanupExpired(targetId);

        if (owner.getUniqueId().equals(targetId)) {
            return InviteStatus.SELF;
        }

        if (regionAccessService.isMember(region.getId(), targetId)) {
            return InviteStatus.ALREADY_MEMBER;
        }

        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            return InviteStatus.TARGET_OFFLINE;
        }

        Map<String, PendingInvite> invites = invitesByTarget.computeIfAbsent(targetId, ignored -> new HashMap<>());
        if (invites.containsKey(region.getId())) {
            return InviteStatus.ALREADY_PENDING;
        }

        invites.put(region.getId(), new PendingInvite(
                region.getId(),
                owner.getUniqueId(),
                owner.getName(),
                region.getName(),
                System.currentTimeMillis() + INVITE_TTL_MILLIS
        ));
        return InviteStatus.SENT;
    }

    public AcceptStatus accept(UUID targetId, String regionId) {
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(regionId, "regionId");
        cleanupExpired(targetId);

        PendingInvite invite = removeInvite(targetId, regionId);
        if (invite == null) {
            return AcceptStatus.MISSING;
        }

        Region region = regionManager.getRegion(regionId).orElse(null);
        if (region == null) {
            return AcceptStatus.REGION_MISSING;
        }

        if (regionAccessService.isMember(regionId, targetId)) {
            return AcceptStatus.ALREADY_MEMBER;
        }

        regionAccessService.addMember(regionId, targetId);
        return AcceptStatus.ACCEPTED;
    }

    public boolean decline(UUID targetId, String regionId) {
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(regionId, "regionId");
        cleanupExpired(targetId);
        return removeInvite(targetId, regionId) != null;
    }

    public List<PendingInvite> getInvites(UUID targetId) {
        Objects.requireNonNull(targetId, "targetId");
        cleanupExpired(targetId);
        return invitesByTarget.getOrDefault(targetId, Map.of()).values().stream()
                .sorted(Comparator.comparing(PendingInvite::regionName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private PendingInvite removeInvite(UUID targetId, String regionId) {
        Map<String, PendingInvite> invites = invitesByTarget.get(targetId);
        if (invites == null) {
            return null;
        }

        PendingInvite invite = invites.remove(regionId);
        if (invites.isEmpty()) {
            invitesByTarget.remove(targetId);
        }
        return invite;
    }

    private void cleanupExpired(UUID targetId) {
        Map<String, PendingInvite> invites = invitesByTarget.get(targetId);
        if (invites == null) {
            return;
        }

        long now = System.currentTimeMillis();
        List<String> expiredRegionIds = new ArrayList<>();
        for (Map.Entry<String, PendingInvite> entry : invites.entrySet()) {
            if (entry.getValue().expiresAtMillis() <= now) {
                expiredRegionIds.add(entry.getKey());
            }
        }

        for (String regionId : expiredRegionIds) {
            invites.remove(regionId);
        }

        if (invites.isEmpty()) {
            invitesByTarget.remove(targetId);
        }
    }

    public enum InviteStatus {
        SENT,
        SELF,
        ALREADY_MEMBER,
        ALREADY_PENDING,
        TARGET_OFFLINE
    }

    public enum AcceptStatus {
        ACCEPTED,
        MISSING,
        REGION_MISSING,
        ALREADY_MEMBER
    }

    public record PendingInvite(String regionId, UUID ownerId, String ownerName, String regionName,
                                long expiresAtMillis) {
    }
}
