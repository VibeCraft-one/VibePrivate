package com.vibeprivate.model;

import java.util.Arrays;

public final class FlagCatalog {
    private static final RegionFlag[] PLAYER_REGION_FLAGS = {
            RegionFlag.BUILD,
            RegionFlag.BREAK,
            RegionFlag.INTERACT,
            RegionFlag.CONTAINER,
            RegionFlag.CHESTS,
            RegionFlag.BARRELS,
            RegionFlag.ENDER_CHESTS,
            RegionFlag.SHULKER_BOXES,
            RegionFlag.FURNACES,
            RegionFlag.WORKSTATIONS,
            RegionFlag.PVP,
            RegionFlag.PROJECTILE_DAMAGE,
            RegionFlag.MOB_DAMAGE,
            RegionFlag.CROP_TRAMPLE,
            RegionFlag.REDSTONE,
            RegionFlag.DOORS,
            RegionFlag.PRESSURE_PLATES,
            RegionFlag.ITEM_FRAMES,
            RegionFlag.ITEM_FRAME_MAPS,
            RegionFlag.ITEM_FRAME_ROTATE,
            RegionFlag.ARMOR_STANDS,
            RegionFlag.LEASH,
            RegionFlag.HOPPER_MINECART,
            RegionFlag.PISTON_FLOW,
            RegionFlag.LIQUID_FLOW,
            RegionFlag.IGNITE,
            RegionFlag.FIRE_SPREAD,
            RegionFlag.PROJECTILES,
            RegionFlag.ITEM_PICKUP,
            RegionFlag.ITEM_DROP,
            RegionFlag.VILLAGER_INTERACT,
            RegionFlag.ENTITY_DAMAGE
    };

    private static final RegionFlag[] ADMIN_REGION_FLAGS = withExtra(PLAYER_REGION_FLAGS, RegionFlag.NO_CLAIM);

    private FlagCatalog() {
    }

    public static RegionFlag[] playerRegionFlags() {
        return PLAYER_REGION_FLAGS.clone();
    }

    public static RegionFlag[] adminRegionFlags() {
        return ADMIN_REGION_FLAGS.clone();
    }

    public static RegionFlag[] memberFlags() {
        return playerRegionFlags();
    }

    public static RegionFlag parentFlag(RegionFlag flag) {
        return switch (flag) {
            case CHESTS, BARRELS, ENDER_CHESTS, SHULKER_BOXES, FURNACES -> RegionFlag.CONTAINER;
            case WORKSTATIONS -> RegionFlag.INTERACT;
            case ITEM_FRAME_MAPS, ITEM_FRAME_ROTATE -> RegionFlag.ITEM_FRAMES;
            default -> null;
        };
    }

    private static RegionFlag[] withExtra(RegionFlag[] flags, RegionFlag extra) {
        RegionFlag[] extended = Arrays.copyOf(flags, flags.length + 1);
        extended[flags.length] = extra;
        return extended;
    }
}
