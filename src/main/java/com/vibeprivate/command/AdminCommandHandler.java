package com.vibeprivate.command;

import com.vibeprivate.gui.AdminMainMenu;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.service.AdminRegionResult;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.AdminRegionStatus;
import com.vibeprivate.service.DeletionResult;
import com.vibeprivate.service.DeletionStatus;
import com.vibeprivate.service.RegionDeletionService;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Objects;

final class AdminCommandHandler {
    static final String PERMISSION = "vibeprivate.admin";

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final AdminRegionService adminRegionService;
    private final RegionDeletionService regionDeletionService;

    AdminCommandHandler(MessageService messageService, RegionManager regionManager,
                        AdminRegionService adminRegionService, RegionDeletionService regionDeletionService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.adminRegionService = Objects.requireNonNull(adminRegionService, "adminRegionService");
        this.regionDeletionService = Objects.requireNonNull(regionDeletionService, "regionDeletionService");
    }

    void handle(Player player, String[] args) {
        if (!isAllowed(player)) {
            messageService.send(player, "command.no-permission");
            return;
        }

        if (args.length < 2) {
            player.openInventory(new AdminMainMenu(messageService, regionManager, player).getInventory());
            return;
        }

        switch (args[1].toLowerCase(java.util.Locale.ROOT)) {
            case "pos1" -> handlePos1(player);
            case "pos2" -> handlePos2(player);
            case "create" -> handleCreate(player, args);
            case "deletehere" -> handleDeleteHere(player);
            case "noclaim" -> handleNoClaim(player, args);
            default -> messageService.send(player, "command.admin.usage");
        }
    }

    boolean isAllowed(Player player) {
        return player.isOp() || player.hasPermission(PERMISSION);
    }

    private void handlePos1(Player player) {
        adminRegionService.setPos1(player);
        messageService.send(player, "admin.pos1-set");
    }

    private void handlePos2(Player player) {
        adminRegionService.setPos2(player);
        messageService.send(player, "admin.pos2-set");
    }

    private void handleCreate(Player player, String[] args) {
        String name = args.length >= 3 ? String.join(" ", List.of(args).subList(2, args.length)) : "ADMIN";
        AdminRegionResult result = adminRegionService.create(player, name);
        if (result.getStatus() == AdminRegionStatus.SUCCESS) {
            result.getRegion().ifPresent(region -> messageService.send(player, "admin.create.success",
                    Map.of("name", region.getName())));
            return;
        }

        messageService.send(player, switch (result.getStatus()) {
            case MISSING_POSITIONS -> "admin.create.missing-positions";
            case DIFFERENT_WORLDS -> "admin.create.different-worlds";
            case INVALID_RADIUS -> "admin.create.invalid-radius";
            case FAILED -> "admin.create.failed";
            case SUCCESS -> "admin.create.success";
        });
    }

    private void handleNoClaim(Player player, String[] args) {
        if (args.length < 3) {
            messageService.send(player, "admin.noclaim.usage");
            return;
        }

        int radius;
        try {
            radius = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            messageService.send(player, "admin.create.invalid-radius");
            return;
        }

        AdminRegionResult result = adminRegionService.createNoClaim(player, radius);
        if (result.getStatus() == AdminRegionStatus.SUCCESS) {
            result.getRegion().ifPresent(region -> messageService.send(player, "admin.noclaim.success",
                    Map.of("radius", Integer.toString(radius))));
            return;
        }

        messageService.send(player, result.getStatus() == AdminRegionStatus.INVALID_RADIUS
                ? "admin.create.invalid-radius"
                : "admin.create.failed");
    }

    private void handleDeleteHere(Player player) {
        Region region = regionManager.getRegionIncludingDisabledAtOrNull(player.getLocation());
        if (region == null) {
            messageService.send(player, "region.delete.not-found");
            return;
        }

        DeletionResult result = regionDeletionService.deleteAsAdmin(player, region);
        if (result.getStatus() == DeletionStatus.CONFIRM_REQUIRED) {
            messageService.send(player, "region.delete.confirm", Map.of(
                    "remaining", Integer.toString(result.getRemainingConfirmations())
            ));
            return;
        }

        messageService.send(player, switch (result.getStatus()) {
            case DELETED -> "region.delete.success";
            case NOT_ALLOWED -> "region.delete.not-allowed";
            case NOT_FOUND -> "region.delete.not-found";
            case FAILED -> "region.delete.failed";
            case CONFIRM_REQUIRED -> "region.delete.confirm";
        });
    }
}
