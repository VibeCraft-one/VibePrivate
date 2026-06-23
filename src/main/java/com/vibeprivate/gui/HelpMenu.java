package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.Objects;

public final class HelpMenu implements InventoryHolder {
    public static final int BACK_SLOT = 26;

    private final MessageService messageService;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;

    public HelpMenu(MessageService messageService, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.player = Objects.requireNonNull(player, "player");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 27, messageService.get("gui.help.title"));
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
        inventory.setItem(10, itemFactory.item(Material.GRASS_BLOCK, "gui.help.region.name",
                List.of("gui.help.region.lore.1", "gui.help.region.lore.2", "gui.help.region.lore.3")));
        inventory.setItem(12, itemFactory.item(Material.COAL_BLOCK, "gui.help.fuel.name",
                List.of("gui.help.fuel.lore.1", "gui.help.fuel.lore.2", "gui.help.fuel.lore.3", "gui.help.fuel.lore.4")));
        inventory.setItem(14, itemFactory.item(Material.DIAMOND_BLOCK, "gui.help.deposit.name",
                List.of("gui.help.deposit.lore.1", "gui.help.deposit.lore.2", "gui.help.deposit.lore.3", "gui.help.deposit.lore.4")));
        inventory.setItem(16, itemFactory.item(Material.PLAYER_HEAD, "gui.help.members.name",
                List.of("gui.help.members.lore.1", "gui.help.members.lore.2", "gui.help.members.lore.3")));
        inventory.setItem(BACK_SLOT, itemFactory.item(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
    }
}
