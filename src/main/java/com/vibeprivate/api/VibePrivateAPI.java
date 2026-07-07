package com.vibeprivate.api;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.RegionStatus;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.SelectionBounds;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.ClanRegionManagementService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationResult;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.RegionLifecycleService;
import com.vibeprivate.service.RegionRelocationService;
import com.vibeprivate.service.RegionSelectionValidator;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class VibePrivateAPI {
    private final RegionManager regionManager;
    private final RegionCreationService regionCreationService;
    private final AdminRegionService adminRegionService;
    private final ClanRegionManagementService clanRegionManagementService;
    private final RegionAccessService regionAccessService;
    private final RegionLifecycleService regionLifecycleService;
    private final RegionRelocationService regionRelocationService;
    private final RegionSelectionValidator regionSelectionValidator;

    public VibePrivateAPI(RegionManager regionManager, RegionCreationService regionCreationService,
                          AdminRegionService adminRegionService, ClanRegionManagementService clanRegionManagementService,
                          RegionAccessService regionAccessService, RegionLifecycleService regionLifecycleService,
                          RegionRelocationService regionRelocationService, RegionSelectionValidator regionSelectionValidator) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionCreationService = Objects.requireNonNull(regionCreationService, "regionCreationService");
        this.adminRegionService = Objects.requireNonNull(adminRegionService, "adminRegionService");
        this.clanRegionManagementService = Objects.requireNonNull(clanRegionManagementService,
                "clanRegionManagementService");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
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

        regionAccessService.syncMembers(region.get().getId(), members);
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

    public AdminRegionService adminRegions() {
        return adminRegionService;
    }

    public void addMember(String regionId, UUID playerId) {
        regionAccessService.addMember(regionId, playerId);
    }

    public void removeMember(String regionId, UUID playerId) {
        regionAccessService.removeMember(regionId, playerId);
    }

    public boolean isMember(String regionId, UUID playerId) {
        return regionAccessService.isMember(regionId, playerId);
    }
}
