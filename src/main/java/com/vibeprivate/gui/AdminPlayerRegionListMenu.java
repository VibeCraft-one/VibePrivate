package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AdminPlayerRegionListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 53;
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int PAGE_INFO_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 52;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final Player player;
    private final String ownerId;
    private int page;
    private final int parentPage;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> regionIdsBySlot = new HashMap<>();
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public AdminPlayerRegionListMenu(MessageService messageService, RegionManager regionManager,
                                     Player player, String ownerId) {
        this(messageService, regionManager, player, ownerId, 0, 0);
    }

    public AdminPlayerRegionListMenu(MessageService messageService, RegionManager regionManager,
                                     Player player, String ownerId, int page, int parentPage) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.player = Objects.requireNonNull(player, "player");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId");
        this.page = Math.max(0, page);
        this.parentPage = Math.max(0, parentPage);
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.admin.player-regions.title",
                Map.of("owner", displayOwner())));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public int getPage() {
        return page;
    }

    public int getParentPage() {
        return parentPage;
    }

    public boolean hasPreviousPage() {
        return hasPreviousPage;
    }

    public boolean hasNextPage() {
        return hasNextPage;
    }

    public String getRegionId(int slot) {
        return regionIdsBySlot.get(slot);
    }

    private void render() {
        List<Region> regions = regionManager.getPlayerRegionsByOwner(ownerId);
        AdminGuiPage guiPage = new AdminGuiPage(page, regions.size());
        page = guiPage.page();
        hasPreviousPage = guiPage.hasPrevious();
        hasNextPage = guiPage.hasNext();

        int slot = 0;
        for (Region region : guiPage.slice(regions)) {
            inventory.setItem(slot, regionItem(region));
            regionIdsBySlot.put(slot, region.getId());
            slot++;
        }

        if (regions.isEmpty()) {
            inventory.setItem(22, itemFactory.item(Material.BARRIER, "gui.admin.player-regions.empty.name",
                    List.of("gui.admin.player-regions.empty.lore")));
        }

        renderPagination(guiPage);
        inventory.setItem(BACK_SLOT, itemFactory.item(GuiIcon.BACK, "gui.back.name", List.of("gui.back.lore")));
    }

    private void renderPagination(AdminGuiPage guiPage) {
        Map<String, String> placeholders = Map.of(
                "page", Integer.toString(guiPage.displayPage()),
                "pages", Integer.toString(guiPage.totalPages())
        );
        if (guiPage.hasPrevious()) {
            inventory.setItem(PREVIOUS_PAGE_SLOT, itemFactory.item(Material.ARROW, "gui.page.previous.name",
                    placeholders, List.of("gui.page.previous.lore"), placeholders));
        }
        inventory.setItem(PAGE_INFO_SLOT, itemFactory.item(Material.PAPER, "gui.page.info.name",
                placeholders, List.of("gui.page.info.lore"), placeholders));
        if (guiPage.hasNext()) {
            inventory.setItem(NEXT_PAGE_SLOT, itemFactory.item(Material.ARROW, "gui.page.next.name",
                    placeholders, List.of("gui.page.next.lore"), placeholders));
        }
    }

    private ItemStack regionItem(Region region) {
        return itemFactory.itemWithLore(icon(region.getType()), messageService.get("gui.admin.player-regions.region.name",
                        Map.of("type", region.getType().name(), "name", region.getName())),
                List.of(
                        messageService.get("gui.admin.player-regions.region.lore.world",
                                Map.of("world", region.getWorldName())),
                        messageService.get("gui.admin.player-regions.region.lore.radius",
                                Map.of("radius", String.valueOf(region.getRadius()))),
                        messageService.get("gui.admin.player-regions.region.lore.center", Map.of(
                                "x", String.valueOf(region.getCenterX()),
                                "z", String.valueOf(region.getCenterZ())
                        )),
                        messageService.get(region.isEnabled()
                                ? "gui.admin.player-regions.region.lore.enabled"
                                : "gui.admin.player-regions.region.lore.disabled")
                ));
    }

    private GuiIcon icon(RegionType type) {
        return switch (type) {
            case HOME -> GuiIcon.HOME_REGION;
            case FARM -> GuiIcon.FARM_REGION;
            case CLAN -> GuiIcon.CLAN_REGION;
            case ADMIN -> GuiIcon.ADMIN_REGION;
        };
    }

    private String displayOwner() {
        try {
            UUID uuid = UUID.fromString(ownerId);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            return offlinePlayer.getName() == null ? uuid.toString().substring(0, 8) : offlinePlayer.getName();
        } catch (IllegalArgumentException exception) {
            return ownerId;
        }
    }
}
