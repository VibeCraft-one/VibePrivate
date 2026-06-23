package com.vibeprivate.protection;

import com.vibeprivate.model.RegionFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.ProjectileSource;

public final class ProtectionEventResolver {
    public RegionFlag flagForInteraction(Action action, Block block) {
        Material type = block.getType();
        if (type == Material.ENDER_CHEST) {
            return RegionFlag.ENDER_CHESTS;
        }

        if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
            return RegionFlag.CHESTS;
        }

        if (type == Material.BARREL) {
            return RegionFlag.BARRELS;
        }

        if (type.name().endsWith("SHULKER_BOX")) {
            return RegionFlag.SHULKER_BOXES;
        }

        if (isFurnaceLike(type)) {
            return RegionFlag.FURNACES;
        }

        if (isWorkstation(type)) {
            return RegionFlag.WORKSTATIONS;
        }

        if (block.getState() instanceof org.bukkit.inventory.InventoryHolder) {
            return RegionFlag.CONTAINER;
        }

        String name = type.name();
        if (name.endsWith("_DOOR") || name.endsWith("_FENCE_GATE") || name.endsWith("_TRAPDOOR")) {
            return RegionFlag.DOORS;
        }

        if (name.endsWith("_BUTTON") || name.endsWith("_PRESSURE_PLATE")) {
            return name.endsWith("_PRESSURE_PLATE") ? RegionFlag.PRESSURE_PLATES : RegionFlag.REDSTONE;
        }

        if (action == Action.PHYSICAL && type == Material.FARMLAND) {
            return RegionFlag.CROP_TRAMPLE;
        }

        return RegionFlag.INTERACT;
    }

    public RegionFlag flagForEntityInteraction(Entity entity) {
        if (entity instanceof Villager) {
            return RegionFlag.VILLAGER_INTERACT;
        }

        if (entity instanceof ArmorStand) {
            return RegionFlag.ARMOR_STANDS;
        }

        if (entity instanceof ItemFrame itemFrame) {
            return isMapItem(itemFrame) ? RegionFlag.ITEM_FRAME_MAPS : RegionFlag.ITEM_FRAME_ROTATE;
        }

        return RegionFlag.INTERACT;
    }

    public RegionFlag flagForEntityDamage(Entity target) {
        if (target instanceof Villager || target instanceof Animals || target instanceof WaterMob || target instanceof IronGolem) {
            return RegionFlag.MOB_DAMAGE;
        }

        if (target instanceof Monster && target.getCustomName() == null) {
            return RegionFlag.INTERACT;
        }

        if (target instanceof ArmorStand) {
            return RegionFlag.ARMOR_STANDS;
        }

        if (target.getType() == EntityType.ITEM_FRAME || target.getType() == EntityType.GLOW_ITEM_FRAME) {
            return RegionFlag.ITEM_FRAMES;
        }

        return RegionFlag.ENTITY_DAMAGE;
    }

    public RegionFlag flagForPlayerDamage(Entity damager) {
        return damager instanceof Projectile ? RegionFlag.PROJECTILE_DAMAGE : RegionFlag.PVP;
    }

    public Player findPlayerDamager(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    public Location holderLocation(Object holder) {
        if (holder instanceof Entity entity) {
            return entity.getLocation();
        }

        if (holder instanceof org.bukkit.block.BlockState blockState) {
            return blockState.getLocation();
        }

        return null;
    }

    public boolean isLiquid(Material material) {
        return material == Material.WATER || material == Material.LAVA;
    }

    public boolean isFire(Material material) {
        return material == Material.FIRE || material == Material.SOUL_FIRE;
    }

    public boolean isLiquidBucketInHand(Player player, EquipmentSlot hand) {
        if (hand == EquipmentSlot.OFF_HAND) {
            return isLiquidBucket(player.getInventory().getItemInOffHand().getType());
        }

        return isLiquidBucket(player.getInventory().getItemInMainHand().getType());
    }

    public boolean isMapItem(ItemFrame itemFrame) {
        Material material = itemFrame.getItem().getType();
        return material == Material.FILLED_MAP || material == Material.MAP;
    }

    private boolean isLiquidBucket(Material material) {
        return material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET;
    }

    private boolean isFurnaceLike(Material material) {
        return material == Material.FURNACE
                || material == Material.BLAST_FURNACE
                || material == Material.SMOKER;
    }

    private boolean isWorkstation(Material material) {
        return material == Material.CRAFTING_TABLE
                || material == Material.STONECUTTER
                || material == Material.CARTOGRAPHY_TABLE
                || material == Material.SMITHING_TABLE
                || material == Material.FLETCHING_TABLE
                || material == Material.GRINDSTONE
                || material == Material.LOOM
                || material == Material.ENCHANTING_TABLE
                || material == Material.BREWING_STAND
                || material == Material.ANVIL
                || material == Material.CHIPPED_ANVIL
                || material == Material.DAMAGED_ANVIL;
    }
}
