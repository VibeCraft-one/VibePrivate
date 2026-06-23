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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PrivateMainMenu implements InventoryHolder {
    public static final int HOME_SLOT = 11;
    public static final int REGIONS_SLOT = 13;
    public static final int FARM_SLOT = 15;
    public static final int MEMBER_REGIONS_SLOT = 20;
    public static final int HELP_SLOT = 22;
    public static final int INVITES_SLOT = 24;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;

    public PrivateMainMenu(MessageService messageService, RegionManager regionManager, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.player = Objects.requireNonNull(player, "player");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 27, messageService.get("gui.main.title"));
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
        inventory.setItem(HOME_SLOT, itemFactory.item(Material.GRASS_BLOCK, "gui.main.create-home.name",
                List.of("gui.main.create-home.lore.1", "gui.main.create-home.lore.2")));
        inventory.setItem(FARM_SLOT, itemFactory.item(Material.WHEAT, "gui.main.create-farm.name",
                List.of("gui.main.create-farm.lore.1", "gui.main.create-farm.lore.2")));
        inventory.setItem(REGIONS_SLOT, regionsItem());
        inventory.setItem(MEMBER_REGIONS_SLOT, itemFactory.item(Material.PLAYER_HEAD,
                "gui.main.member-regions.name",
                List.of("gui.main.member-regions.lore.1", "gui.main.member-regions.lore.2")));
        inventory.setItem(HELP_SLOT, itemFactory.item(Material.BOOK, "gui.main.help.name",
                List.of("gui.main.help.lore.1", "gui.main.help.lore.2")));
        inventory.setItem(INVITES_SLOT, itemFactory.item(Material.PAPER, "gui.main.invites.name",
                List.of("gui.main.invites.lore.1", "gui.main.invites.lore.2")));
    }

    private ItemStack regionsItem() {
        List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId().toString());
        List<String> lore = new ArrayList<>();
        if (regions.isEmpty()) {
            lore.add(messageService.get("gui.main.my-regions.empty"));
        } else {
            for (Region region : regions) {
                lore.add(messageService.get("gui.main.my-regions.entry", Map.of(
                        "type", region.getType().name(),
                        "name", region.getName()
                )));
            }
        }

        return itemFactory.itemWithLore(Material.CHEST, messageService.get("gui.main.my-regions.name",
                Map.of("count", Integer.toString(regions.size()))), lore);
    }
}
