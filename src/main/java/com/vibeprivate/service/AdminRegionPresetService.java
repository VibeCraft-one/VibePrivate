package com.vibeprivate.service;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class AdminRegionPresetService {
    private final RegionAccessService regionAccessService;

    public AdminRegionPresetService(RegionAccessService regionAccessService) {
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
    }

    public void applyPreset(Region region, AdminRegionPreset preset) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(preset, "preset");
        if (!region.isAdmin()) {
            return;
        }

        Map<RegionFlag, Boolean> flags = flagsFor(preset);
        for (Map.Entry<RegionFlag, Boolean> entry : flags.entrySet()) {
            regionAccessService.setDefaultFlag(region.getId(), entry.getKey(), entry.getValue());
        }
    }

    private Map<RegionFlag, Boolean> flagsFor(AdminRegionPreset preset) {
        Map<RegionFlag, Boolean> flags = defaultDeniedFlags();
        switch (preset) {
            case SPAWN_SAFE -> flags.put(RegionFlag.FALL_DAMAGE, false);
            case PVP_ARENA -> {
                flags.put(RegionFlag.PVP, true);
                flags.put(RegionFlag.PROJECTILES, true);
                flags.put(RegionFlag.PROJECTILE_DAMAGE, true);
                flags.put(RegionFlag.FALL_DAMAGE, true);
            }
            case BOSS_ZONE -> {
                flags.put(RegionFlag.MOB_DAMAGE, true);
                flags.put(RegionFlag.ENTITY_DAMAGE, true);
                flags.put(RegionFlag.PROJECTILES, true);
                flags.put(RegionFlag.PROJECTILE_DAMAGE, true);
                flags.put(RegionFlag.FALL_DAMAGE, true);
                flags.put(RegionFlag.ITEM_PICKUP, true);
            }
            case SNOWBALL_EVENT -> {
                flags.put(RegionFlag.PROJECTILES, true);
                flags.put(RegionFlag.PROJECTILE_DAMAGE, true);
                flags.put(RegionFlag.PVP, false);
                flags.put(RegionFlag.FALL_DAMAGE, true);
                flags.put(RegionFlag.ITEM_PICKUP, false);
                flags.put(RegionFlag.ITEM_DROP, false);
            }
            case INTERACT_ZONE -> {
                flags.put(RegionFlag.INTERACT, true);
                flags.put(RegionFlag.REDSTONE, true);
                flags.put(RegionFlag.DOORS, true);
                flags.put(RegionFlag.PRESSURE_PLATES, true);
                flags.put(RegionFlag.WORKSTATIONS, true);
                flags.put(RegionFlag.ITEM_FRAME_MAPS, true);
                flags.put(RegionFlag.ITEM_FRAME_ROTATE, true);
                flags.put(RegionFlag.VILLAGER_INTERACT, true);
                flags.put(RegionFlag.FALL_DAMAGE, true);
            }
            case NO_CLAIM -> {
                flags.put(RegionFlag.NO_CLAIM, true);
                flags.put(RegionFlag.FALL_DAMAGE, true);
            }
        }

        return flags;
    }

    private Map<RegionFlag, Boolean> defaultDeniedFlags() {
        Map<RegionFlag, Boolean> flags = new EnumMap<>(RegionFlag.class);
        for (RegionFlag flag : RegionFlag.values()) {
            flags.put(flag, false);
        }

        return flags;
    }
}
