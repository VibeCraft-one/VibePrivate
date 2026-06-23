package com.vibeprivate.listener;

import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.protection.ProtectionDenyNotifier;
import com.vibeprivate.protection.ProtectionEventResolver;
import com.vibeprivate.protection.ProtectionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

final class BlockProtectionHandler {
    private final ProtectionService protectionService;
    private final ProtectionEventResolver eventResolver;
    private final ProtectionDenyNotifier denyNotifier;

    BlockProtectionHandler(ProtectionService protectionService, ProtectionEventResolver eventResolver,
                           ProtectionDenyNotifier denyNotifier) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
        this.eventResolver = Objects.requireNonNull(eventResolver, "eventResolver");
        this.denyNotifier = Objects.requireNonNull(denyNotifier, "denyNotifier");
    }

    void handleBlockBreak(BlockBreakEvent event) {
        denyIfNeeded(event.getPlayer(), event.getBlock().getLocation(), RegionFlag.BREAK, event);
    }

    void handleBlockPlace(BlockPlaceEvent event) {
        denyIfNeeded(event.getPlayer(), event.getBlockPlaced().getLocation(), RegionFlag.BUILD, event);
    }

    void handleBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            denyIfNeeded(player, event.getBlock().getLocation(), RegionFlag.IGNITE, event);
            return;
        }

        if (!protectionService.canEnvironmentUse(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD)) {
            event.setCancelled(true);
        }
    }

    void handleBlockSpread(BlockSpreadEvent event) {
        if (eventResolver.isFire(event.getSource().getType())
                && !protectionService.canEnvironmentUse(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD)) {
            event.setCancelled(true);
        }
    }

    void handleBlockBurn(BlockBurnEvent event) {
        if (!protectionService.canEnvironmentUse(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD)) {
            event.setCancelled(true);
        }
    }

    void handleBucketEmpty(PlayerBucketEmptyEvent event) {
        Block targetBlock = event.getBlockClicked().getRelative(event.getBlockFace());
        denyIfNeeded(event.getPlayer(), targetBlock.getLocation(), RegionFlag.LIQUID_FLOW, event);
    }

    void handlePlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        RegionFlag flag = eventResolver.flagForInteraction(event.getAction(), block);
        denyIfNeeded(event.getPlayer(), block.getLocation(), flag, event);
    }

    void handleEntityInteract(EntityInteractEvent event) {
        if (event.getBlock().getType() != Material.FARMLAND) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            denyIfNeeded(player, event.getBlock().getLocation(), RegionFlag.CROP_TRAMPLE, event);
        }
    }

    void handleBlockFromTo(BlockFromToEvent event) {
        if (eventResolver.isLiquid(event.getBlock().getType())
                && crossesRegion(event.getBlock(), event.getToBlock(), RegionFlag.LIQUID_FLOW)) {
            event.setCancelled(true);
        }
    }

    void handlePistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (crossesRegion(block, block.getRelative(event.getDirection()), RegionFlag.PISTON_FLOW)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    void handlePistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (crossesRegion(block, block.getRelative(event.getDirection()), RegionFlag.PISTON_FLOW)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void denyIfNeeded(Player player, Location location, RegionFlag flag, Cancellable event) {
        if (!protectionService.canUse(player, location, flag)) {
            event.setCancelled(true);
            denyNotifier.sendDenied(player, location);
        }
    }

    private boolean crossesRegion(Block from, Block to, RegionFlag flag) {
        return protectionService.isDifferentProtectedRegion(from.getLocation(), to.getLocation(), flag);
    }
}
