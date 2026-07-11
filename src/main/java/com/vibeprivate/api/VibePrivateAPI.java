package com.vibeprivate.api;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.ClanRegionRole;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionHome;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.SelectionBounds;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.ClanRegionManagementService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationResult;
import com.vibeprivate.service.RegionCreationStatus;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.RegionLifecycleService;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionRelocationService;
import com.vibeprivate.service.RegionSelectionValidator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class VibePrivateAPI {
    private final RegionManager regionManager;
    private final RegionCreationService regionCreationService;
    private final AdminRegionService adminRegionService;
    private final ClanRegionManagementService clanRegionManagementService;
    private final RegionAccessService regionAccessService;
    private final RegionHomeService regionHomeService;
    private final RegionLifecycleService regionLifecycleService;
    private final RegionRelocationService regionRelocationService;
    private final RegionSelectionValidator regionSelectionValidator;

    public VibePrivateAPI(RegionManager regionManager, RegionCreationService regionCreationService,
                          AdminRegionService adminRegionService, ClanRegionManagementService clanRegionManagementService,
                          RegionAccessService regionAccessService, RegionHomeService regionHomeService,
                          RegionLifecycleService regionLifecycleService, RegionRelocationService regionRelocationService,
                          RegionSelectionValidator regionSelectionValidator) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionCreationService = Objects.requireNonNull(regionCreationService, "regionCreationService");
        this.adminRegionService = Objects.requireNonNull(adminRegionService, "adminRegionService");
        this.clanRegionManagementService = Objects.requireNonNull(clanRegionManagementService,
                "clanRegionManagementService");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
        this.regionHomeService = Objects.requireNonNull(regionHomeService, "regionHomeService");
        this.regionLifecycleService = Objects.requireNonNull(regionLifecycleService, "regionLifecycleService");
        this.regionRelocationService = Objects.requireNonNull(regionRelocationService, "regionRelocationService");
        this.regionSelectionValidator = Objects.requireNonNull(regionSelectionValidator, "regionSelectionValidator");
    }

    public Optional<Region> getRegionAt(Location location) {
        return regionManager.getRegionAt(location);
    }

    public Optional<Region> getRegion(String regionId) {
        return regionManager.getRegion(regionId);
    }

    public Collection<Region> getAllRegions() {
        return regionManager.getRegions();
    }

    public Collection<Region> getRegionsInWorld(String worldName) {
        return getRegionsInWorld(worldName, false);
    }

    public Collection<Region> getRegionsInWorld(String worldName, boolean includeInactive) {
        return regionLifecycleService.getRegionsInWorld(worldName, includeInactive);
    }

    public List<Region> getRegionsByOwner(String ownerId) {
        return regionManager.getRegionsByOwner(ownerId);
    }

    public List<Region> getRegionsByOwnerAndType(String ownerId, RegionType type) {
        return regionManager.getRegionsByOwnerAndType(ownerId, type);
    }

    public RegionStatus getRegionStatus(String regionId) {
        return regionLifecycleService.getRegionStatus(regionId);
    }

    public RegionBounds getRegionBounds(String regionId) {
        return regionSelectionValidator.getRegionBounds(regionId);
    }

    public Optional<RegionHome> getRegionHome(String regionId) {
        return regionHomeService.getHome(regionId);
    }

    public boolean setRegionHome(RegionHome home) {
        return regionHomeService.setHome(home);
    }

    public boolean isTargetBoundsValid(SelectionBounds bounds) {
        return regionSelectionValidator.isTargetBoundsValid(bounds);
    }

    public boolean isAreaInsideRegion(String regionId, SelectionBounds bounds) {
        return regionSelectionValidator.isAreaInsideRegion(regionId, bounds);
    }

    public boolean canMoveRegionToWorld(String regionId, String targetWorld) {
        return regionRelocationService.canMoveRegionToWorld(regionId, targetWorld);
    }

    public Region moveRegionToWorldSameBounds(String regionId, String targetWorld) {
        return regionRelocationService.moveRegionToWorldSameBounds(regionId, targetWorld);
    }

    public boolean canRelocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ) {
        return regionRelocationService.canRelocateRegion(regionId, targetWorld, targetCenterX, targetCenterZ);
    }

    public Region relocateRegion(String regionId, String targetWorld, int targetCenterX, int targetCenterZ) {
        return regionRelocationService.relocateRegion(regionId, targetWorld, targetCenterX, targetCenterZ);
    }

    public void setRegionStatus(String regionId, RegionStatus status) {
        regionLifecycleService.setRegionStatus(regionId, status);
    }

    public void pauseUpkeep(String regionId, String reason) {
        regionLifecycleService.pauseUpkeep(regionId, reason);
    }

    public void resumeUpkeep(String regionId, String reason) {
        regionLifecycleService.resumeUpkeep(regionId, reason);
    }

    public RegionCreationResult createPrivateRegion(Player player) {
        return regionCreationService.createHomeRegion(player);
    }

    public RegionCreationResult createFarmRegion(Player player) {
        return regionCreationService.createFarmRegion(player);
    }

    public RegionCreationResult createClanRegion(String clanId, Location location, String name) {
        return regionCreationService.createClanRegion(clanId, location, name);
    }

    public RegionCreationResult createClanRegion(String clanId, UUID firstLeaderId, Location location, String name) {
        Objects.requireNonNull(firstLeaderId, "firstLeaderId");
        RegionCreationResult result = regionCreationService.createClanRegion(clanId, location, name);
        Optional<Region> createdRegion = result.getRegion();
        if (createdRegion.isEmpty()) {
            return result;
        }

        String regionId = createdRegion.get().getId();
        try {
            regionAccessService.addMember(regionId, firstLeaderId);
            clanRegionManagementService.setClanRegionRole(regionId, firstLeaderId, ClanRegionRole.LEADER);
            return result;
        } catch (RuntimeException exception) {
            cleanupFailedClanLeaderBootstrap(regionId, firstLeaderId);
            return RegionCreationResult.fail(RegionCreationStatus.FAILED);
        }
    }

    /**
     * Returns the clan region owned by the provided clan id, if it exists.
     *
     * @param clanId current clan identifier stored in the region owner field
     * @return clan region owned by the clan
     */
    public Optional<Region> getClanRegion(String clanId) {
        Objects.requireNonNull(clanId, "clanId");
        String ownerId = clanId.trim();
        if (ownerId.isEmpty()) {
            return Optional.empty();
        }

        return regionManager.getRegionsByOwnerAndType(ownerId, RegionType.CLAN).stream().findFirst();
    }

    /**
     * Synchronizes the VibePrivate member list for a clan region.
     *
     * @param clanId current clan identifier stored in the region owner field
     * @param members complete desired member set
     * @return true when the clan region exists and the sync was applied
     */
    public boolean syncClanMembers(String clanId, Collection<UUID> members) {
        Objects.requireNonNull(members, "members");
        Optional<Region> region = getClanRegion(clanId);
        if (region.isEmpty()) {
            return false;
        }

        String regionId = region.get().getId();
        Set<UUID> desiredMembers = new HashSet<>(members);
        for (UUID currentMember : regionAccessService.getMembers(regionId)) {
            if (!desiredMembers.contains(currentMember)) {
                clanRegionManagementService.removeClanRegionRole(regionId, currentMember);
            }
        }

        regionAccessService.syncMembers(regionId, members);
        return true;
    }

    /**
     * Removes the clan region owned by the provided clan id, if it exists.
     *
     * @param clanId current clan identifier stored in the region owner field
     * @return removed region
     */
    public Optional<Region> removeClanRegion(String clanId) {
        Optional<Region> region = getClanRegion(clanId);
        return region.flatMap(value -> regionManager.removeRegion(value.getId()));
    }

    public boolean isClanRegionLeader(String regionId, UUID playerId) {
        return clanRegionManagementService.isClanRegionLeader(regionId, playerId);
    }

    public boolean canManageClanRegion(String regionId, UUID playerId) {
        return clanRegionManagementService.canManageClanRegion(regionId, playerId);
    }

    public Optional<ClanRegionRole> getClanRegionRole(String regionId, UUID playerId) {
        if (!clanRegionManagementService.isClanRegion(regionId)) {
            return Optional.empty();
        }

        if (!regionAccessService.isMember(regionId, playerId)) {
            return Optional.empty();
        }

        Optional<ClanRegionRole> elevatedRole = clanRegionManagementService.getElevatedRole(regionId, playerId);
        if (elevatedRole.isPresent()) {
            return elevatedRole;
        }

        return Optional.of(ClanRegionRole.MEMBER);
    }

    public void setClanRegionRole(String regionId, UUID playerId, ClanRegionRole role) {
        Objects.requireNonNull(role, "role");
        if (role == ClanRegionRole.MEMBER) {
            clanRegionManagementService.setClanRegionRole(regionId, playerId, ClanRegionRole.MEMBER);
            regionAccessService.addMember(regionId, playerId);
            return;
        }

        clanRegionManagementService.setClanRegionRole(regionId, playerId, role);
        try {
            regionAccessService.addMember(regionId, playerId);
        } catch (RuntimeException exception) {
            clanRegionManagementService.setClanRegionRole(regionId, playerId, ClanRegionRole.MEMBER);
            throw exception;
        }
    }

    public AdminRegionService adminRegions() {
        return adminRegionService;
    }

    public void addMember(String regionId, UUID playerId) {
        regionAccessService.addMember(regionId, playerId);
    }

    public void removeMember(String regionId, UUID playerId) {
        if (clanRegionManagementService.isClanRegion(regionId)) {
            clanRegionManagementService.removeClanRegionRole(regionId, playerId);
        }
        regionAccessService.removeMember(regionId, playerId);
    }

    public boolean isMember(String regionId, UUID playerId) {
        return regionAccessService.isMember(regionId, playerId);
    }

    private void cleanupFailedClanLeaderBootstrap(String regionId, UUID playerId) {
        try {
            clanRegionManagementService.setClanRegionRole(regionId, playerId, ClanRegionRole.MEMBER);
        } catch (RuntimeException ignored) {
            // Best-effort cleanup before removing the just-created region.
        }

        try {
            regionAccessService.removeMember(regionId, playerId);
        } catch (RuntimeException ignored) {
            // Best-effort cleanup before removing the just-created region.
        }

        regionManager.removeRegion(regionId);
    }
}
