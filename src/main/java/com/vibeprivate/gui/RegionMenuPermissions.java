package com.vibeprivate.gui;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import org.bukkit.entity.Player;

final class RegionMenuPermissions {
    private static final String ADMIN_PERMISSION = "vibeprivate.admin";

    private RegionMenuPermissions() {
    }

    static boolean isAdminOperator(Player player) {
        return player.isOp() || player.hasPermission(ADMIN_PERMISSION);
    }

    static boolean isPlayerManagedRegion(Region region) {
        return region.getType() == RegionType.HOME || region.getType() == RegionType.FARM;
    }

    static boolean canManageRegion(Player player, Region region) {
        return region.getOwnerId().equals(player.getUniqueId().toString())
                || region.isAdmin() && isAdminOperator(player);
    }
}
