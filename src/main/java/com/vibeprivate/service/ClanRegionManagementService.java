package com.vibeprivate.service;

import com.vibeprivate.model.ClanRegionRole;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.storage.ClanRegionRoleRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ClanRegionManagementService {
    private final ClanRegionManagementRegionStore regionStore;
    private final ClanRegionRoleRepository roleRepository;
    private Map<String, Map<UUID, ClanRegionRole>> rolesByRegion = new HashMap<>();

    public ClanRegionManagementService(ClanRegionManagementRegionStore regionStore,
                                       ClanRegionRoleRepository roleRepository) {
        this.regionStore = Objects.requireNonNull(regionStore, "regionStore");
        this.roleRepository = Objects.requireNonNull(roleRepository, "roleRepository");
    }

    public void load() {
        rolesByRegion = roleRepository.loadAll();
    }

    public boolean isClanRegionLeader(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        return getElevatedRole(regionId, playerId)
                .filter(role -> role == ClanRegionRole.LEADER)
                .isPresent();
    }

    public boolean canManageClanRegion(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        return getElevatedRole(regionId, playerId)
                .filter(ClanRegionRole::canManageClanRegion)
                .isPresent();
    }

    public Optional<ClanRegionRole> getElevatedRole(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        if (!isClanRegion(regionId)) {
            return Optional.empty();
        }

        return Optional.ofNullable(rolesByRegion.getOrDefault(regionId, Map.of()).get(playerId));
    }

    public void setClanRegionRole(String regionId, UUID playerId, ClanRegionRole role) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(role, "role");
        requireClanRegion(regionId);

        if (!role.isElevated()) {
            Map<UUID, ClanRegionRole> roles = rolesByRegion.get(regionId);
            if (roles != null) {
                roles.remove(playerId);
                if (roles.isEmpty()) {
                    rolesByRegion.remove(regionId);
                }
            }
            roleRepository.delete(regionId, playerId);
            return;
        }

        rolesByRegion.computeIfAbsent(regionId, ignored -> new HashMap<>()).put(playerId, role);
        roleRepository.save(regionId, playerId, role);
    }

    public boolean isClanRegion(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        return regionStore.getRegion(regionId)
                .filter(region -> region.getType() == RegionType.CLAN)
                .isPresent();
    }

    private void requireClanRegion(String regionId) {
        if (!isClanRegion(regionId)) {
            throw new IllegalArgumentException("Unknown clan region: " + regionId);
        }
    }
}
