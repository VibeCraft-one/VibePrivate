package com.vibeprivate.listener;

import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.protection.ProtectionDenyNotifier;
import com.vibeprivate.protection.ProtectionEventResolver;
import com.vibeprivate.protection.ProtectionService;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;

final class EntityProtectionHandler {
    private final ProtectionService protectionService;
    private final ProtectionEventResolver eventResolver;
    private final ProtectionDenyNotifier denyNotifier;

    EntityProtectionHandler(ProtectionService protectionService, ProtectionEventResolver eventResolver,
                            ProtectionDenyNotifier denyNotifier) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
        this.eventResolver = Objects.requireNonNull(eventResolver, "eventResolver");
        this.denyNotifier = Objects.requireNonNull(denyNotifier, "denyNotifier");
    }

    void handleEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = eventResolver.findPlayerDamager(event.getDamager());
        if (attacker == null) {
            return;
        }

        Entity target = event.getEntity();
        if (target instanceof Monster && target.getCustomName() == null) {
            return;
        }

        RegionFlag flag = target instanceof Player
                ? eventResolver.flagForPlayerDamage(event.getDamager())
                : eventResolver.flagForEntityDamage(target);
        if (!protectionService.canUse(attacker, target.getLocation(), flag)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(attacker, target.getLocation());
        }
    }

    void handleEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || event instanceof EntityDamageByEntityEvent) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                && !protectionService.canUse(player, player.getLocation(), RegionFlag.FALL_DAMAGE)) {
            event.setCancelled(true);
        }
    }

    void handleProjectileLaunch(ProjectileLaunchEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (!(shooter instanceof Player player)) {
            return;
        }

        if (!protectionService.canUse(player, player.getLocation(), RegionFlag.PROJECTILES)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(player, player.getLocation());
        }
    }

    void handlePlayerDropItem(PlayerDropItemEvent event) {
        if (!protectionService.canUse(event.getPlayer(), event.getPlayer().getLocation(), RegionFlag.ITEM_DROP)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(event.getPlayer(), event.getPlayer().getLocation());
        }
    }

    void handleEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!protectionService.canUse(player, player.getLocation(), RegionFlag.ITEM_PICKUP)) {
            event.setCancelled(true);
        }
    }

    void handlePlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (eventResolver.isLiquidBucketInHand(event.getPlayer(), event.getHand())
                && !protectionService.canUse(event.getPlayer(), event.getRightClicked().getLocation(), RegionFlag.LIQUID_FLOW)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(event.getPlayer(), event.getRightClicked().getLocation());
            return;
        }

        RegionFlag flag = eventResolver.flagForEntityInteraction(event.getRightClicked());
        if (!protectionService.canUse(event.getPlayer(), event.getRightClicked().getLocation(), flag)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(event.getPlayer(), event.getRightClicked().getLocation());
        }
    }

    void handlePlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (eventResolver.isLiquidBucketInHand(event.getPlayer(), event.getHand())
                && !protectionService.canUse(event.getPlayer(), event.getRightClicked().getLocation(), RegionFlag.LIQUID_FLOW)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(event.getPlayer(), event.getRightClicked().getLocation());
            return;
        }

        if (event.getRightClicked() instanceof ArmorStand
                && !protectionService.canUse(event.getPlayer(), event.getRightClicked().getLocation(), RegionFlag.ARMOR_STANDS)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(event.getPlayer(), event.getRightClicked().getLocation());
        }
    }

    void handleHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) {
            return;
        }

        Hanging hanging = event.getEntity();
        RegionFlag flag = hanging instanceof ItemFrame itemFrame && eventResolver.isMapItem(itemFrame)
                ? RegionFlag.ITEM_FRAME_MAPS
                : RegionFlag.ITEM_FRAMES;
        if (!protectionService.canUse(player, hanging.getLocation(), flag)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(player, hanging.getLocation());
        }
    }

    void handleInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Minecart)
                && !(event.getDestination().getHolder() instanceof Minecart)) {
            return;
        }

        Location sourceLocation = eventResolver.holderLocation(event.getSource().getHolder());
        Location destinationLocation = eventResolver.holderLocation(event.getDestination().getHolder());
        if (sourceLocation != null
                && destinationLocation != null
                && protectionService.isDifferentProtectedRegion(sourceLocation, destinationLocation, RegionFlag.HOPPER_MINECART)) {
            event.setCancelled(true);
        }
    }
}
