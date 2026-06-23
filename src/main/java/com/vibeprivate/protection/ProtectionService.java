package com.vibeprivate.protection;

import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.service.RegionAccessService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class ProtectionService {
    private static final String BYPASS_PERMISSION = "vibeprivate.bypass";

    private final RegionManager regionManager;
    private final PlayerRegionCache playerRegionCache;
    private final RegionAccessService accessService;

    public ProtectionService(RegionManager regionManager, PlayerRegionCache playerRegionCache,
                             RegionAccessService accessService) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.playerRegionCache = Objects.requireNonNull(playerRegionCache, "playerRegionCache");
        this.accessService = Objects.requireNonNull(accessService, "accessService");
    }

    public boolean canUse(Player player, Location location, RegionFlag flag) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(flag, "flag");

        Region region = resolveRegion(player, location);
        if (region == null || !region.isEnabled()) {
            return true;
        }

        if (player.isOp() || player.hasPermission(BYPASS_PERMISSION)) {
            return true;
        }

        UUID playerId = player.getUniqueId();
        if (region.getOwnerId().equals(playerId.toString())) {
            return true;
        }

        if (region.isAdmin()) {
            return getAdminFlag(region, flag);
        }

        return accessService.canUseFlag(region, playerId, flag);
    }

    public Region resolveRegion(Player player, Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(location, "location");

        Region cachedRegion = playerRegionCache.get(player.getUniqueId());
        if (cachedRegion != null && contains(cachedRegion, location) && !cachedRegion.isAdmin()) {
            return cachedRegion;
        }

        Region region = regionManager.getRegionAtOrNull(location);
        playerRegionCache.set(player.getUniqueId(), region);
        return region;
    }

    public boolean isDifferentProtectedRegion(Location from, Location to, RegionFlag flag) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(flag, "flag");

        Region fromRegion = regionManager.getRegionAtOrNull(from);
        Region toRegion = regionManager.getRegionAtOrNull(to);
        if (fromRegion == toRegion) {
            return isAdminFlowBlocked(fromRegion, flag);
        }

        if (toRegion != null && toRegion.isEnabled()) {
            return true;
        }

        return isAdminFlowBlocked(fromRegion, flag);
    }

    public boolean canEnvironmentUse(Location location, RegionFlag flag) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(flag, "flag");

        Region region = regionManager.getRegionAtOrNull(location);
        if (region == null || !region.isEnabled()) {
            return true;
        }

        if (region.isAdmin()) {
            return getAdminFlag(region, flag);
        }

        return accessService.getDefaultFlag(region.getId(), flag);
    }

    public boolean shouldNotifyDenied(Player player, Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(location, "location");

        Region region = resolveRegion(player, location);
        return region == null || !region.isAdmin();
    }

    private boolean contains(Region region, Location location) {
        if (location.getWorld() == null) {
            return false;
        }

        return region.contains(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    private boolean isAdminFlowBlocked(Region region, RegionFlag flag) {
        return region != null
                && region.isEnabled()
                && region.isAdmin()
                && !accessService.getDefaultFlag(region.getId(), flag);
    }

    private boolean getAdminFlag(Region region, RegionFlag flag) {
        if (accessService.getDefaultFlag(region.getId(), flag)) {
            return true;
        }

        if (flag == RegionFlag.HUNGER) {
            return true;
        }

        if (flag == RegionFlag.FALL_DAMAGE) {
            if (!accessService.hasDefaultFlag(region.getId(), RegionFlag.FALL_DAMAGE)) {
                return true;
            }

            return !canDisableFallDamage(region);
        }

        return flag == RegionFlag.PROJECTILES
                && accessService.getDefaultFlag(region.getId(), RegionFlag.PROJECTILE_DAMAGE);
    }

    private boolean canDisableFallDamage(Region region) {
        return !accessService.getDefaultFlag(region.getId(), RegionFlag.PVP)
                && !accessService.getDefaultFlag(region.getId(), RegionFlag.PROJECTILES)
                && !accessService.getDefaultFlag(region.getId(), RegionFlag.PROJECTILE_DAMAGE)
                && !accessService.getDefaultFlag(region.getId(), RegionFlag.MOB_DAMAGE)
                && !accessService.getDefaultFlag(region.getId(), RegionFlag.ENTITY_DAMAGE);
    }
}
