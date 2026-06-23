package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AdminMainMenu implements InventoryHolder {
    public static final int POS1_SLOT = 10;
    public static final int POS2_SLOT = 11;
    public static final int CREATE_SLOT = 12;
    public static final int NO_CLAIM_SLOT = 13;
    public static final int REGIONS_SLOT = 14;
    public static final int PLAYER_REGIONS_SLOT = 15;
    public static final int PLAYER_MENU_SLOT = 16;
    public static final int HELP_SLOT = 22;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;

    public AdminMainMenu(MessageService messageService, RegionManager regionManager, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.player = Objects.requireNonNull(player, "player");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 27, messageService.get("gui.admin.main.title"));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    private void render() {
        inventory.setItem(POS1_SLOT, itemFactory.item(Material.LIME_DYE, "gui.admin.main.pos1.name",
                List.of("gui.admin.main.pos1.lore.1", "gui.admin.main.pos1.lore.2")));
        inventory.setItem(POS2_SLOT, itemFactory.item(Material.RED_DYE, "gui.admin.main.pos2.name",
                List.of("gui.admin.main.pos2.lore.1", "gui.admin.main.pos2.lore.2")));
        inventory.setItem(CREATE_SLOT, itemFactory.item(Material.REDSTONE_BLOCK, "gui.admin.main.create.name",
                List.of("gui.admin.main.create.lore.1", "gui.admin.main.create.lore.2")));
        inventory.setItem(NO_CLAIM_SLOT, itemFactory.item(Material.BARRIER, "gui.admin.main.noclaim.name",
                List.of("gui.admin.main.noclaim.lore.1", "gui.admin.main.noclaim.lore.2")));
        inventory.setItem(REGIONS_SLOT, regionsItem());
        inventory.setItem(PLAYER_REGIONS_SLOT, playerRegionsItem());
        inventory.setItem(PLAYER_MENU_SLOT, itemFactory.item(Material.GRASS_BLOCK, "gui.admin.main.player-menu.name",
                List.of("gui.admin.main.player-menu.lore.1", "gui.admin.main.player-menu.lore.2")));
        inventory.setItem(HELP_SLOT, itemFactory.item(Material.BOOK, "gui.admin.main.help.name",
                List.of("gui.admin.main.help.lore.1", "gui.admin.main.help.lore.2")));
    }

    private ItemStack regionsItem() {
        long count = regionManager.getRegions().stream()
                .filter(Region::isAdmin)
                .count();
        return itemFactory.item(Material.MAP, "gui.admin.main.regions.name",
                Map.of("count", Long.toString(count)),
                List.of("gui.admin.main.regions.lore.1", "gui.admin.main.regions.lore.2"),
                Map.of());
    }

    private ItemStack playerRegionsItem() {
        long count = regionManager.getRegions().stream()
                .filter(region -> !region.isAdmin())
                .count();
        return itemFactory.item(Material.PLAYER_HEAD, "gui.admin.main.player-regions.name",
                Map.of("count", Long.toString(count)),
                List.of("gui.admin.main.player-regions.lore.1", "gui.admin.main.player-regions.lore.2"),
                Map.of());
    }
}
