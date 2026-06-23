package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.service.AdminRegionResult;
import com.vibeprivate.service.AdminRegionStatus;
import com.vibeprivate.service.DeletionResult;
import com.vibeprivate.service.DeletionStatus;
import com.vibeprivate.service.FuelAddResult;
import com.vibeprivate.service.FuelAddStatus;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.RegionCreationResult;
import com.vibeprivate.service.RegionCreationStatus;
import com.vibeprivate.service.RegionDeletionService;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionUpgradeService;
import com.vibeprivate.service.UpgradeResult;
import com.vibeprivate.service.UpgradeStatus;
import com.vibeprivate.service.WithdrawDepositResult;
import com.vibeprivate.service.WithdrawDepositStatus;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

final class RegionMenuActionHandler {
    private final MessageService messageService;
    private final FuelService fuelService;
    private final RegionUpgradeService regionUpgradeService;
    private final RegionDeletionService regionDeletionService;
    private final RegionHomeService regionHomeService;

    RegionMenuActionHandler(MessageService messageService, FuelService fuelService,
                            RegionUpgradeService regionUpgradeService, RegionDeletionService regionDeletionService,
                            RegionHomeService regionHomeService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.fuelService = Objects.requireNonNull(fuelService, "fuelService");
        this.regionUpgradeService = Objects.requireNonNull(regionUpgradeService, "regionUpgradeService");
        this.regionDeletionService = Objects.requireNonNull(regionDeletionService, "regionDeletionService");
        this.regionHomeService = Objects.requireNonNull(regionHomeService, "regionHomeService");
    }

    void handleFuel(Player player, Region region) {
        FuelAddResult result = fuelService.addFuelFromMainHand(player, region);
        if (result.getStatus() == FuelAddStatus.SUCCESS) {
            messageService.send(player, "fuel.add.success", Map.of(
                    "amount", Integer.toString(result.getConsumedAmount()),
                    "added", fuelService.formatMillis(result.getAddedMinutes() * 60_000L),
                    "remaining", fuelService.formatMillis(result.getRemainingMillis())
            ));
            return;
        }

        messageService.send(player, result.getStatus() == FuelAddStatus.MAX_REACHED
                ? "fuel.add.max"
                : "fuel.add.invalid");
    }

    void handleUpgrade(Player player, Region region) {
        UpgradeResult result = regionUpgradeService.depositFromMainHand(player, region);
        if (result.getStatus() == UpgradeStatus.SUCCESS) {
            messageService.send(player, "upgrade.deposit.success", Map.of(
                    "amount", Integer.toString(result.getConsumedAmount()),
                    "old", Integer.toString(result.getOldRadius()),
                    "new", Integer.toString(result.getNewRadius())
            ));
            return;
        }

        messageService.send(player, switch (result.getStatus()) {
            case INVALID_ITEM -> "upgrade.deposit.invalid";
            case MAX_REACHED -> "upgrade.deposit.max";
            case OVERLAP -> "upgrade.deposit.overlap";
            case UNSUPPORTED_REGION -> "upgrade.deposit.unsupported";
            case FAILED -> "upgrade.deposit.failed";
            case SUCCESS -> "upgrade.deposit.success";
        });
    }

    void handleWithdraw(Player player, Region region) {
        WithdrawDepositResult result = regionUpgradeService.withdrawExcessDeposit(player, region);
        if (result.getStatus() == WithdrawDepositStatus.SUCCESS) {
            messageService.send(player, "upgrade.withdraw.success", Map.of(
                    "amount", Integer.toString(result.getReturnedAmount())
            ));
            return;
        }

        messageService.send(player, result.getStatus() == WithdrawDepositStatus.NOTHING_TO_RETURN
                ? "upgrade.withdraw.none"
                : "upgrade.withdraw.failed");
    }

    void handleDelete(Player player, Region region, boolean adminAllowed) {
        DeletionResult result = region.isAdmin() && adminAllowed
                ? regionDeletionService.deleteAsAdmin(player, region)
                : regionDeletionService.deleteOwned(player, region);
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

    void handleCreation(Player player, RegionCreationResult result) {
        if (result.getStatus() == RegionCreationStatus.SUCCESS) {
            result.getRegion().ifPresent(region -> {
                if (region.getType() == RegionType.HOME) {
                    regionHomeService.setHome(player, region);
                }

                messageService.send(player, "region.create.success", Map.of(
                        "type", region.getType().name(),
                        "name", region.getName()
                ));
            });
            return;
        }

        messageService.send(player, switch (result.getStatus()) {
            case HOME_LIMIT_REACHED -> "region.create.limit-home";
            case FARM_LIMIT_REACHED -> "region.create.limit-farm";
            case CLAN_LIMIT_REACHED -> "region.create.limit-clan";
            case WORLD_NOT_ALLOWED -> "region.create.world-denied";
            case CLAIM_BLOCKED -> "region.create.claim-blocked";
            case REGION_OVERLAP -> "region.create.overlap";
            case FAILED -> "region.create.failed";
            case SUCCESS -> "region.create.success";
        });
    }

    void handleAdminCreate(Player player, AdminRegionResult result) {
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

    void handleAdminNoClaim(Player player, AdminRegionResult result, int radius) {
        if (result.getStatus() == AdminRegionStatus.SUCCESS) {
            messageService.send(player, "admin.noclaim.success", Map.of("radius", Integer.toString(radius)));
            return;
        }

        messageService.send(player, result.getStatus() == AdminRegionStatus.INVALID_RADIUS
                ? "admin.create.invalid-radius"
                : "admin.create.failed");
    }
}
