package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.FlagCatalog;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.storage.RegionAccessRepository;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class RegionAccessService {
    private final RegionAccessRepository accessRepository;
    private Map<String, Set<UUID>> membersByRegion = new HashMap<>();
    private Map<String, Map<UUID, Map<RegionFlag, Boolean>>> flagsByRegion = new HashMap<>();
    private Map<String, Map<RegionFlag, Boolean>> defaultsByRegion = new HashMap<>();

    public RegionAccessService(RegionAccessRepository accessRepository) {
        this.accessRepository = Objects.requireNonNull(accessRepository, "accessRepository");
    }

    public void load() {
        membersByRegion = accessRepository.loadMembers();
        flagsByRegion = accessRepository.loadMemberFlags();
        defaultsByRegion = accessRepository.loadDefaultFlags();
    }

    public boolean isMember(Region region, UUID playerId) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(playerId, "playerId");
        return membersByRegion.getOrDefault(region.getId(), Set.of()).contains(playerId);
    }

    public boolean canUseFlag(Region region, UUID playerId, RegionFlag flag) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(flag, "flag");

        if (!isMember(region, playerId)) {
            return false;
        }

        Map<RegionFlag, Boolean> memberFlags = flagsByRegion
                .getOrDefault(region.getId(), Map.of())
                .get(playerId);
        if (memberFlags != null && memberFlags.containsKey(flag)) {
            return memberFlags.get(flag);
        }

        Map<RegionFlag, Boolean> defaultFlags = defaultsByRegion.getOrDefault(region.getId(), Map.of());
        if (defaultFlags.containsKey(flag)) {
            return defaultFlags.get(flag);
        }

        RegionFlag parent = FlagCatalog.parentFlag(flag);
        if (parent == null) {
            return false;
        }

        if (memberFlags != null && memberFlags.containsKey(parent)) {
            return memberFlags.get(parent);
        }

        return defaultFlags.getOrDefault(parent, false);
    }

    public boolean getDefaultFlag(String regionId, RegionFlag flag) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(flag, "flag");
        Map<RegionFlag, Boolean> defaultFlags = defaultsByRegion.getOrDefault(regionId, Map.of());
        if (defaultFlags.containsKey(flag)) {
            return defaultFlags.get(flag);
        }

        RegionFlag parent = FlagCatalog.parentFlag(flag);
        if (parent == null) {
            return false;
        }

        return defaultFlags.getOrDefault(parent, false);
    }

    public boolean hasDefaultFlag(String regionId, RegionFlag flag) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(flag, "flag");
        return defaultsByRegion.getOrDefault(regionId, Map.of()).containsKey(flag);
    }

    public void setDefaultFlag(String regionId, RegionFlag flag, boolean enabled) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(flag, "flag");
        defaultsByRegion.computeIfAbsent(regionId, ignored -> new EnumMap<>(RegionFlag.class)).put(flag, enabled);
        accessRepository.saveDefaultFlag(regionId, flag, enabled);
    }

    public boolean toggleDefaultFlag(String regionId, RegionFlag flag) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(flag, "flag");

        boolean enabled = !getDefaultFlag(regionId, flag);
        defaultsByRegion.computeIfAbsent(regionId, ignored -> new EnumMap<>(RegionFlag.class)).put(flag, enabled);
        accessRepository.saveDefaultFlag(regionId, flag, enabled);
        return enabled;
    }

    public void addMemberRuntime(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        membersByRegion.computeIfAbsent(regionId, ignored -> new HashSet<>()).add(playerId);
    }

    public void addMember(String regionId, UUID playerId) {
        addMemberRuntime(regionId, playerId);
        accessRepository.addMember(regionId, playerId);
    }

    public void removeMember(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        Set<UUID> members = membersByRegion.get(regionId);
        if (members != null) {
            members.remove(playerId);
            if (members.isEmpty()) {
                membersByRegion.remove(regionId);
            }
        }
        Map<UUID, Map<RegionFlag, Boolean>> regionFlags = flagsByRegion.get(regionId);
        if (regionFlags != null) {
            regionFlags.remove(playerId);
            if (regionFlags.isEmpty()) {
                flagsByRegion.remove(regionId);
            }
        }
        accessRepository.removeMember(regionId, playerId);
    }

    /**
     * Replaces the current member set with the provided desired member set.
     *
     * @param regionId target region id
     * @param playerIds complete desired member set
     */
    public void syncMembers(String regionId, Collection<UUID> playerIds) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerIds, "playerIds");

        Set<UUID> desired = new HashSet<>(playerIds);
        Set<UUID> current = new HashSet<>(membersByRegion.getOrDefault(regionId, Set.of()));

        for (UUID playerId : desired) {
            if (!current.contains(playerId)) {
                addMember(regionId, playerId);
            }
        }

        for (UUID playerId : current) {
            if (!desired.contains(playerId)) {
                removeMember(regionId, playerId);
            }
        }
    }

    public boolean isMember(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        return membersByRegion.getOrDefault(regionId, Set.of()).contains(playerId);
    }

    public Set<UUID> getMembers(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        return Set.copyOf(membersByRegion.getOrDefault(regionId, Set.of()));
    }

    public Set<String> getRegionIdsByMember(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        Set<String> regionIds = new HashSet<>();
        for (Map.Entry<String, Set<UUID>> entry : membersByRegion.entrySet()) {
            if (entry.getValue().contains(playerId)) {
                regionIds.add(entry.getKey());
            }
        }
        return Set.copyOf(regionIds);
    }

    public void setMemberFlagRuntime(String regionId, UUID playerId, RegionFlag flag, boolean enabled) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(flag, "flag");
        flagsByRegion.computeIfAbsent(regionId, ignored -> new HashMap<>())
                .computeIfAbsent(playerId, ignored -> new EnumMap<>(RegionFlag.class))
                .put(flag, enabled);
    }

    public boolean getMemberEffectiveFlag(Region region, UUID playerId, RegionFlag flag) {
        return canUseFlag(region, playerId, flag);
    }

    public boolean hasMemberFlag(String regionId, UUID playerId, RegionFlag flag) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(flag, "flag");
        return flagsByRegion
                .getOrDefault(regionId, Map.of())
                .getOrDefault(playerId, Map.of())
                .containsKey(flag);
    }

    public boolean toggleMemberFlag(Region region, UUID playerId, RegionFlag flag) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(flag, "flag");
        boolean enabled = !canUseFlag(region, playerId, flag);
        setMemberFlag(region.getId(), playerId, flag, enabled);
        return enabled;
    }

    public void setMemberFlag(String regionId, UUID playerId, RegionFlag flag, boolean enabled) {
        setMemberFlagRuntime(regionId, playerId, flag, enabled);
        accessRepository.saveMemberFlag(regionId, playerId, flag, enabled);
    }

}
