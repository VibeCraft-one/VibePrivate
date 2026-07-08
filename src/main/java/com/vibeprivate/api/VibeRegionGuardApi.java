package com.vibeprivate.api;

import com.vibeprivate.model.ClanRegionRole;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.SelectionBounds;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.RegionCreationResult;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class VibeRegionGuardApi {
    private final VibePrivateAPI api;

    public VibeRegionGuardApi(VibePrivateAPI api) {
        this.api = Objects.requireNonNull(api, "api");
    }

    public Optional<Region> getRegionAt(Location location) {
        return api.getRegionAt(location);
    }

    public Optional<Region> getRegion(String regionId) {
        return api.getRegion(regionId);
    }

    public Collection<Region> getAllRegions() {
        return api.getAllRegions();
    }

    public Collection<Region> getRegionsInWorld(String worldName) {
        return api.getRegionsInWorld(worldName);
    }

    public Collection<Region> getRegionsInWorld(String worldName, boolean includeInactive) {
        return api.getRegionsInWorld(worldName, includeInactive);
    }

    public List<Region> getRegionsByOwner(String ownerId) {
        return api.getRegionsByOwner(ownerId);
    }

    public List<Region> getRegionsByOwnerAndType(String ownerId, RegionType type) {
        return api.getRegionsByOwnerAndType(ownerId, type);
    }

    public RegionStatus getRegionStatus(String regionId) {
        return api.getRegionStatus(regionId);
    }

    public RegionBounds getRegionBounds(String regionId) {
        return api.getRegionBounds(regionId);
    }

    public boolean isTargetBoundsValid(SelectionBounds bounds) {
        return api.isTargetBoundsValid(bounds);
    }

    public boolean isAreaInsideRegion(String regionId, SelectionBounds bounds) {
        return api.isAreaInsideRegion(regionId, bounds);
    }

    public boolean canMoveRegionToWorld(String regionId, String targetWorld) {
        return api.canMoveRegionToWorld(regionId, targetWorld);
    }

    public Region moveRegionToWorldSameBounds(String regionId, String targetWorld) {
        return api.moveRegionToWorldSameBounds(regionId, targetWorld);
    }

    public boolean canRelocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ) {
        return api.canRelocateRegion(regionId, targetWorld, targetCenterX, targetCenterZ);
    }

    public Region relocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ) {
        return api.relocateRegion(regionId, targetWorld, targetCenterX, targetCenterZ);
    }

    public void setRegionStatus(String regionId, RegionStatus status) {
        api.setRegionStatus(regionId, status);
    }

    public void pauseUpkeep(String regionId, String reason) {
        api.pauseUpkeep(regionId, reason);
    }

    public void resumeUpkeep(String regionId, String reason) {
        api.resumeUpkeep(regionId, reason);
    }

    public RegionCreationResult createPrivateRegion(Player player) {
        return api.createPrivateRegion(player);
    }

    public RegionCreationResult createFarmRegion(Player player) {
        return api.createFarmRegion(player);
    }

    public RegionCreationResult createClanRegion(String clanId, Location location, String name) {
        return api.createClanRegion(clanId, location, name);
    }

    public RegionCreationResult createClanRegion(String clanId, UUID firstLeaderId, Location location, String name) {
        return api.createClanRegion(clanId, firstLeaderId, location, name);
    }

    public Optional<Region> getClanRegion(String clanId) {
        return api.getClanRegion(clanId);
    }

    public boolean syncClanMembers(String clanId, Collection<UUID> members) {
        return api.syncClanMembers(clanId, members);
    }

    public Optional<Region> removeClanRegion(String clanId) {
        return api.removeClanRegion(clanId);
    }

    public boolean isClanRegionLeader(String regionId, UUID playerId) {
        return api.isClanRegionLeader(regionId, playerId);
    }

    public boolean canManageClanRegion(String regionId, UUID playerId) {
        return api.canManageClanRegion(regionId, playerId);
    }

    public Optional<ClanRegionRole> getClanRegionRole(String regionId, UUID playerId) {
        return api.getClanRegionRole(regionId, playerId);
    }

    public void setClanRegionRole(String regionId, UUID playerId, ClanRegionRole role) {
        api.setClanRegionRole(regionId, playerId, role);
    }

    public AdminRegionService adminRegions() {
        return api.adminRegions();
    }

    public void addMember(String regionId, UUID playerId) {
        api.addMember(regionId, playerId);
    }

    public void removeMember(String regionId, UUID playerId) {
        api.removeMember(regionId, playerId);
    }

    public boolean isMember(String regionId, UUID playerId) {
        return api.isMember(regionId, playerId);
    }
}
