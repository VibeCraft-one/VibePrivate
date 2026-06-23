package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionInviteService;
import com.vibeprivate.service.RegionUpgradeService;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

final class RegionMenuNavigator {
    private final MessageService messageService;
    private final RegionManager regionManager;
    private final RegionAccessService regionAccessService;
    private final RegionInviteService regionInviteService;
    private final FuelService fuelService;
    private final RegionUpgradeService regionUpgradeService;
    private final ConfigService configService;

    RegionMenuNavigator(MessageService messageService, RegionManager regionManager,
                        RegionAccessService regionAccessService, RegionInviteService regionInviteService,
                        FuelService fuelService,
                        RegionUpgradeService regionUpgradeService, ConfigService configService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
        this.regionInviteService = Objects.requireNonNull(regionInviteService, "regionInviteService");
        this.fuelService = Objects.requireNonNull(fuelService, "fuelService");
        this.regionUpgradeService = Objects.requireNonNull(regionUpgradeService, "regionUpgradeService");
        this.configService = Objects.requireNonNull(configService, "configService");
    }

    void openMain(Player player) {
        player.openInventory(new PrivateMainMenu(messageService, regionManager, player).getInventory());
    }

    void openAdminMain(Player player) {
        player.openInventory(new AdminMainMenu(messageService, regionManager, player).getInventory());
    }

    void openHelp(Player player) {
        player.openInventory(new HelpMenu(messageService, player).getInventory());
    }

    void openRegionList(Player player) {
        player.openInventory(new RegionListMenu(messageService, regionManager, player).getInventory());
    }

    void openMemberRegionList(Player player) {
        player.openInventory(new MemberRegionListMenu(messageService, regionManager, regionAccessService, player).getInventory());
    }

    void openInvites(Player player) {
        player.openInventory(new RegionInvitesMenu(messageService, regionInviteService, player).getInventory());
    }

    void openAdminRegionList(Player player) {
        player.openInventory(new AdminRegionListMenu(messageService, regionManager, regionAccessService, player).getInventory());
    }

    void openAdminPlayerList(Player player) {
        player.openInventory(new AdminPlayerListMenu(messageService, regionManager, player).getInventory());
    }

    void openAdminPlayerRegionList(Player player, String ownerId) {
        player.openInventory(new AdminPlayerRegionListMenu(messageService, regionManager, player, ownerId).getInventory());
    }

    void openRegionDetail(Player player, Region region) {
        player.openInventory(new RegionDetailMenu(messageService, regionAccessService,
                fuelService, regionUpgradeService, region).getInventory());
    }

    void openMembers(Player owner, Region region) {
        playerInventory(owner, new RegionMembersMenu(messageService, regionAccessService, owner, region));
    }

    void openMemberFlags(Player owner, Region region, UUID memberId) {
        playerInventory(owner, new RegionMemberFlagsMenu(messageService, regionAccessService, owner, region, memberId));
    }

    void openFuel(Player player, Region region) {
        player.openInventory(new RegionFuelMenu(messageService, fuelService, configService, region).getInventory());
    }

    void openDeposit(Player player, Region region) {
        player.openInventory(new RegionDepositMenu(messageService, regionUpgradeService, configService, region).getInventory());
    }

    private void playerInventory(Player player, org.bukkit.inventory.InventoryHolder holder) {
        player.openInventory(holder.getInventory());
    }
}
