package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.service.AdminRegionPresetService;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.ConfirmationService;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.PendingTeleportService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionCreationService;
import com.vibeprivate.service.RegionDeletionService;
import com.vibeprivate.service.RegionHomeService;
import com.vibeprivate.service.RegionInviteService;
import com.vibeprivate.service.RegionTeleportService;
import com.vibeprivate.service.RegionUpgradeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public final class PrivateMenuListener implements Listener {
    private final MainMenuHandler mainMenuHandler;
    private final AdminMenuHandler adminMenuHandler;
    private final RegionListMenuHandler regionListMenuHandler;
    private final RegionDetailClickHandler detailClickHandler;
    private final RegionInviteMenuHandler inviteMenuHandler;
    private final RegionMembersMenuHandler membersMenuHandler;
    private final RegionResourceMenuHandler resourceMenuHandler;
    private final HelpMenuHandler helpMenuHandler;

    public PrivateMenuListener(MessageService messageService, ConfigService configService, RegionManager regionManager,
                               RegionAccessService regionAccessService, RegionCreationService regionCreationService,
                               FuelService fuelService, RegionInviteService regionInviteService,
                               RegionUpgradeService regionUpgradeService,
                               RegionDeletionService regionDeletionService,
                               RegionTeleportService regionTeleportService,
                               RegionHomeService regionHomeService,
                               PendingTeleportService pendingTeleportService,
                               AdminRegionService adminRegionService,
                               AdminRegionPresetService adminRegionPresetService,
                               ConfirmationService confirmationService) {
        Objects.requireNonNull(messageService, "messageService");
        Objects.requireNonNull(configService, "configService");
        Objects.requireNonNull(regionManager, "regionManager");
        Objects.requireNonNull(regionAccessService, "regionAccessService");
        Objects.requireNonNull(regionCreationService, "regionCreationService");
        Objects.requireNonNull(regionInviteService, "regionInviteService");
        Objects.requireNonNull(fuelService, "fuelService");
        Objects.requireNonNull(regionUpgradeService, "regionUpgradeService");
        Objects.requireNonNull(adminRegionService, "adminRegionService");
        Objects.requireNonNull(adminRegionPresetService, "adminRegionPresetService");
        Objects.requireNonNull(confirmationService, "confirmationService");
        RegionMenuActionHandler actionHandler = new RegionMenuActionHandler(messageService, fuelService, regionUpgradeService,
                regionDeletionService, regionHomeService);
        RegionMemberActionHandler memberActionHandler = new RegionMemberActionHandler(messageService, regionAccessService,
                regionInviteService);
        RegionMenuTeleportHandler teleportHandler = new RegionMenuTeleportHandler(messageService, configService, regionTeleportService,
                regionHomeService, pendingTeleportService);
        RegionMenuNavigator navigator = new RegionMenuNavigator(messageService, regionManager, regionAccessService, regionInviteService,
                fuelService, regionUpgradeService, configService);
        this.mainMenuHandler = new MainMenuHandler(regionCreationService, actionHandler, navigator);
        this.adminMenuHandler = new AdminMenuHandler(messageService, regionManager, adminRegionService, actionHandler, navigator);
        this.regionListMenuHandler = new RegionListMenuHandler(messageService, regionManager, navigator);
        this.detailClickHandler = new RegionDetailClickHandler(messageService, regionManager, regionAccessService,
                adminRegionPresetService, confirmationService, actionHandler, teleportHandler, navigator);
        this.inviteMenuHandler = new RegionInviteMenuHandler(memberActionHandler, navigator);
        this.membersMenuHandler = new RegionMembersMenuHandler(messageService, regionAccessService,
                memberActionHandler, navigator);
        this.resourceMenuHandler = new RegionResourceMenuHandler(regionManager, actionHandler, navigator);
        this.helpMenuHandler = new HelpMenuHandler(navigator);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof AdminMainMenu menu) {
            adminMenuHandler.handleMain(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof AdminRegionListMenu menu) {
            adminMenuHandler.handleRegionList(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof AdminPlayerListMenu menu) {
            adminMenuHandler.handlePlayerList(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof AdminPlayerRegionListMenu menu) {
            adminMenuHandler.handlePlayerRegionList(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof PrivateMainMenu menu) {
            mainMenuHandler.handle(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof RegionListMenu menu) {
            regionListMenuHandler.handle(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof MemberRegionListMenu menu) {
            inviteMenuHandler.handleMemberRegionListMenu(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof RegionInvitesMenu menu) {
            inviteMenuHandler.handleInvitesMenu(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof RegionDetailMenu menu) {
            detailClickHandler.handle(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof RegionFuelMenu menu) {
            resourceMenuHandler.handleFuel(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof RegionDepositMenu menu) {
            resourceMenuHandler.handleDeposit(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof RegionMembersMenu menu) {
            membersMenuHandler.handleMembers(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof RegionMemberFlagsMenu menu) {
            membersMenuHandler.handleMemberFlags(event, menu);
            return;
        }

        if (event.getInventory().getHolder() instanceof HelpMenu menu) {
            helpMenuHandler.handle(event, menu);
        }
    }
}
