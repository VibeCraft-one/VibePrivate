package com.vibeprivate.api;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationResult;
import com.vibeprivate.service.RegionCreationService;
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
    private final RegionAccessService regionAccessService;

    public VibePrivateAPI(RegionManager regionManager, RegionCreationService regionCreationService,
                          AdminRegionService adminRegionService, RegionAccessService regionAccessService) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionCreationService = Objects.requireNonNull(regionCreationService, "regionCreationService");
        this.adminRegionService = Objects.requireNonNull(adminRegionService, "adminRegionService");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
    }

    public Optional<Region> getRegionAt(Location location) {
        return regionManager.getRegionAt(location);
    }

    public Optional<Region> getRegion(String regionId) {
        return regionManager.getRegion(regionId);
    }

    public List<Region> getRegionsByOwner(String ownerId) {
        return regionManager.getRegionsByOwner(ownerId);
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
     * @param clanId stable clan identifier from an external clan plugin
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
     * @param clanId stable clan identifier from an external clan plugin
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
     * @param clanId stable clan identifier from an external clan plugin
     * @return removed region
     */
    public Optional<Region> removeClanRegion(String clanId) {
        Optional<Region> region = getClanRegion(clanId);
        return region.flatMap(value -> regionManager.removeRegion(value.getId()));
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
