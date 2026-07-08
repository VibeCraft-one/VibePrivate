package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AdminPlayerListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 53;
    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int PAGE_INFO_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 52;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final Player player;
    private int page;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> ownerIdsBySlot = new HashMap<>();
    private boolean hasPreviousPage;
    private boolean hasNextPage;

    public AdminPlayerListMenu(MessageService messageService, RegionManager regionManager, Player player) {
        this(messageService, regionManager, player, 0);
    }

    public AdminPlayerListMenu(MessageService messageService, RegionManager regionManager, Player player, int page) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.player = Objects.requireNonNull(player, "player");
        this.page = Math.max(0, page);
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.admin.players.title"));
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

    public String getOwnerId(int slot) {
        return ownerIdsBySlot.get(slot);
    }

    private void render() {
        List<String> ownerIds = regionManager.getPlayerOwnerIds().stream()
                .sorted(Comparator.comparing(this::displayOwner))
                .toList();
        AdminGuiPage guiPage = new AdminGuiPage(page, ownerIds.size());
        page = guiPage.page();
        hasPreviousPage = guiPage.hasPrevious();
        hasNextPage = guiPage.hasNext();

        int slot = 0;
        for (String ownerId : guiPage.slice(ownerIds)) {
            inventory.setItem(slot, ownerItem(ownerId, regionManager.getPlayerRegionsByOwner(ownerId)));
            ownerIdsBySlot.put(slot, ownerId);
            slot++;
        }

        if (ownerIds.isEmpty()) {
            inventory.setItem(22, itemFactory.item(Material.BARRIER, "gui.admin.players.empty.name",
                    List.of("gui.admin.players.empty.lore")));
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

    private ItemStack ownerItem(String ownerId, List<Region> regions) {
        return itemFactory.itemWithLore(GuiIcon.MEMBERS, messageService.get("gui.admin.players.owner.name",
                        Map.of("owner", displayOwner(ownerId))),
                List.of(
                        messageService.get("gui.admin.players.owner.lore.count",
                                Map.of("count", Integer.toString(regions.size()))),
                        messageService.get("gui.admin.players.owner.lore.open")
                ));
    }

    private String displayOwner(String ownerId) {
        try {
            UUID uuid = UUID.fromString(ownerId);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            return offlinePlayer.getName() == null ? uuid.toString().substring(0, 8) : offlinePlayer.getName();
        } catch (IllegalArgumentException exception) {
            return ownerId;
        }
    }
}
