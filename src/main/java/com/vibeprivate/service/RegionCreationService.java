package com.vibeprivate.service;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.model.RegionType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class RegionCreationService {
    private final RegionManager regionManager;
    private final ConfigService configService;
    private final RegionAccessService regionAccessService;

    public RegionCreationService(RegionManager regionManager, ConfigService configService,
                                 RegionAccessService regionAccessService) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
    }

    public RegionCreationResult createHomeRegion(Player player) {
        Objects.requireNonNull(player, "player");
        String ownerId = player.getUniqueId().toString();
        if (regionManager.getRegionsByOwnerAndType(ownerId, RegionType.HOME).size() >= configService.getHomeMaxPerPlayer(player)) {
            return RegionCreationResult.fail(RegionCreationStatus.HOME_LIMIT_REACHED);
        }

        return createPlayerRegion(player, RegionType.HOME, player.getName(), configService.getHomeStartRadius());
    }

    public RegionCreationResult createFarmRegion(Player player) {
        Objects.requireNonNull(player, "player");
        String ownerId = player.getUniqueId().toString();
        int existingFarms = regionManager.getRegionsByOwnerAndType(ownerId, RegionType.FARM).size();
        if (existingFarms >= configService.getFarmMaxPerPlayer(player)) {
            return RegionCreationResult.fail(RegionCreationStatus.FARM_LIMIT_REACHED);
        }

        return createPlayerRegion(player, RegionType.FARM, player.getName() + " FARM " + (existingFarms + 1),
                configService.getFarmStartRadius());
    }

    public RegionCreationResult createClanRegion(String clanId, Location location, String name) {
        Objects.requireNonNull(clanId, "clanId");
        Objects.requireNonNull(location, "location");
        String ownerId = clanId.trim();
        if (regionManager.getRegionsByOwnerAndType(ownerId, RegionType.CLAN).size() >= configService.getClanMaxPerClan()) {
            return RegionCreationResult.fail(RegionCreationStatus.CLAN_LIMIT_REACHED);
        }

        World world = location.getWorld();
        if (world == null || !configService.getAllowedWorlds().contains(world.getName())) {
            return RegionCreationResult.fail(RegionCreationStatus.WORLD_NOT_ALLOWED);
        }

        Region region = createDisabledRadiusRegion(RegionType.CLAN, safeName(name, ownerId), ownerId, location,
                configService.getClanStartRadius());
        return addCreatedRegion(region);
    }

    private RegionCreationResult createPlayerRegion(Player player, RegionType type, String name, int radius) {
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null || !configService.getAllowedWorlds().contains(world.getName())) {
            return RegionCreationResult.fail(RegionCreationStatus.WORLD_NOT_ALLOWED);
        }

        Region region = createDisabledRadiusRegion(type, name, player.getUniqueId().toString(), location, radius);
        return addCreatedRegion(region);
    }

    private Region createDisabledRadiusRegion(RegionType type, String name, String ownerId, Location location, int radius) {
        World world = Objects.requireNonNull(location.getWorld(), "world");
        Region region = Region.radiusRegion(UUID.randomUUID().toString(), name, type, ownerId, world.getName())
                .radius(location.getBlockX(), location.getBlockZ(), radius,
                        configService.getRegionMinY(), configService.getRegionMaxY())
                .build();
        region.setEnabled(false);
        region.setFuelEmptySince(System.currentTimeMillis());
        return region;
    }

    private RegionCreationResult addCreatedRegion(Region region) {
        if (isClaimBlocked(region)) {
            return RegionCreationResult.fail(RegionCreationStatus.CLAIM_BLOCKED);
        }

        try {
            regionManager.addRegion(region);
            return RegionCreationResult.success(region);
        } catch (IllegalArgumentException exception) {
            return RegionCreationResult.fail(RegionCreationStatus.REGION_OVERLAP);
        } catch (RuntimeException exception) {
            return RegionCreationResult.fail(RegionCreationStatus.FAILED);
        }
    }

    private String safeName(String name, String fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }

        return name.trim();
    }

    private boolean isClaimBlocked(Region region) {
        return regionManager.getRegions().stream()
                .filter(Region::isAdmin)
                .filter(admin -> admin.getBounds().intersects(region.getBounds()))
                .anyMatch(admin -> regionAccessService.hasDefaultFlag(admin.getId(), RegionFlag.NO_CLAIM)
                        && regionAccessService.getDefaultFlag(admin.getId(), RegionFlag.NO_CLAIM));
    }
}
