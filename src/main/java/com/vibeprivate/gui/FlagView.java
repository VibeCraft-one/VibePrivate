package com.vibeprivate.gui;

import com.vibeprivate.model.RegionFlag;
import org.bukkit.Material;

public final class FlagView {
    private FlagView() {
    }

    public static Material material(RegionFlag flag) {
        return switch (flag) {
            case BUILD -> Material.BRICKS;
            case BREAK -> Material.IRON_PICKAXE;
            case INTERACT -> Material.OAK_BUTTON;
            case CONTAINER -> Material.CHEST;
            case CHESTS -> Material.CHEST;
            case BARRELS -> Material.BARREL;
            case ENDER_CHESTS -> Material.ENDER_CHEST;
            case SHULKER_BOXES -> Material.SHULKER_BOX;
            case FURNACES -> Material.FURNACE;
            case WORKSTATIONS -> Material.CRAFTING_TABLE;
            case PVP -> Material.IRON_SWORD;
            case PROJECTILE_DAMAGE -> Material.ARROW;
            case ENTITY_DAMAGE -> Material.BOW;
            case MOB_DAMAGE -> Material.BEEF;
            case VILLAGER_INTERACT -> Material.EMERALD;
            case CROP_TRAMPLE -> Material.WHEAT_SEEDS;
            case REDSTONE -> Material.REDSTONE;
            case DOORS -> Material.OAK_DOOR;
            case PRESSURE_PLATES -> Material.STONE_PRESSURE_PLATE;
            case ITEM_FRAMES -> Material.ITEM_FRAME;
            case ITEM_FRAME_MAPS -> Material.FILLED_MAP;
            case ITEM_FRAME_ROTATE -> Material.GLOW_ITEM_FRAME;
            case ARMOR_STANDS -> Material.ARMOR_STAND;
            case LEASH -> Material.LEAD;
            case HOPPER_MINECART -> Material.HOPPER_MINECART;
            case PISTON_FLOW -> Material.PISTON;
            case LIQUID_FLOW -> Material.WATER_BUCKET;
            case IGNITE -> Material.FLINT_AND_STEEL;
            case FIRE_SPREAD -> Material.FIRE_CHARGE;
            case PROJECTILES -> Material.SNOWBALL;
            case FALL_DAMAGE -> Material.FEATHER;
            case ITEM_PICKUP -> Material.HOPPER;
            case ITEM_DROP -> Material.DROPPER;
            case HUNGER -> Material.COOKED_BEEF;
            case NO_CLAIM -> Material.BARRIER;
        };
    }

    public static String nameKey(RegionFlag flag) {
        return "flag." + flag.name().toLowerCase() + ".name";
    }

    public static String descriptionKey(RegionFlag flag) {
        return "flag." + flag.name().toLowerCase() + ".description";
    }
}
