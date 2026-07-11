package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.service.RegionAccessService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AdminRegionListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 53;
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int PAGE_INFO_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 52;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final RegionAccessService accessService;
    private final Player player;
    private int page;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> regionIdsBySlot = new HashMap<>();
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public AdminRegionListMenu(MessageService messageService, RegionManager regionManager,
                               RegionAccessService accessService, Player player) {
        this(messageService, regionManager, accessService, player, 0);
    }

    public AdminRegionListMenu(MessageService messageService, RegionManager regionManager,
                               RegionAccessService accessService, Player player, int page) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.accessService = Objects.requireNonNull(accessService, "accessService");
        this.player = Objects.requireNonNull(player, "player");
        this.page = Math.max(0, page);
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.admin.list.title"));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public int getPage() {
        return page;
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
        List<Region> regions = regionManager.getAdminRegions();
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
            inventory.setItem(22, itemFactory.item(Material.BARRIER, "gui.admin.list.empty.name",
                    List.of("gui.admin.list.empty.lore")));
        }

        renderPagination(guiPage);
        inventory.setItem(BACK_SLOT, itemFactory.item(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
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
        boolean noClaim = accessService.getDefaultFlag(region.getId(), RegionFlag.NO_CLAIM);
        return itemFactory.itemWithLore(noClaim ? Material.BARRIER : Material.REDSTONE_BLOCK,
                messageService.get("gui.admin.list.region.name", Map.of("name", region.getName())),
                List.of(
                messageService.get("gui.admin.list.region.lore.world",
                        Map.of("world", region.getWorldName())),
                messageService.get("gui.admin.list.region.lore.bounds", Map.of(
                        "x1", Integer.toString(region.getBounds().getMinX()),
                        "y1", Integer.toString(region.getBounds().getMinY()),
                        "z1", Integer.toString(region.getBounds().getMinZ()),
                        "x2", Integer.toString(region.getBounds().getMaxX()),
                        "y2", Integer.toString(region.getBounds().getMaxY()),
                        "z2", Integer.toString(region.getBounds().getMaxZ())
                )),
                messageService.get(noClaim
                        ? "gui.admin.list.region.lore.noclaim"
                        : "gui.admin.list.region.lore.normal"),
                messageService.get("gui.admin.list.region.lore.open")
        ));
    }
}
