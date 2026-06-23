package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
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

public final class RegionListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 26;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> regionIdsBySlot = new HashMap<>();

    public RegionListMenu(MessageService messageService, RegionManager regionManager, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.player = Objects.requireNonNull(player, "player");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 27, messageService.get("gui.region-list.title"));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public String getRegionId(int slot) {
        return regionIdsBySlot.get(slot);
    }

    private void render() {
        List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId().toString()).stream()
                .filter(region -> region.getType() == RegionType.HOME || region.getType() == RegionType.FARM)
                .toList();
        int slot = 0;
        for (Region region : regions) {
            if (slot >= 18) {
                break;
            }

            inventory.setItem(slot, regionItem(region));
            regionIdsBySlot.put(slot, region.getId());
            slot++;
        }

        if (regions.isEmpty()) {
            inventory.setItem(13, itemFactory.item(Material.BARRIER, "gui.region-list.empty.name",
                    List.of("gui.region-list.empty.lore")));
        }

        inventory.setItem(BACK_SLOT, itemFactory.item(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
    }

    private ItemStack regionItem(Region region) {
        Material material = switch (region.getType()) {
            case HOME -> Material.GRASS_BLOCK;
            case FARM -> Material.WHEAT;
            case CLAN -> Material.BLUE_BANNER;
            case ADMIN -> Material.BARRIER;
        };

        return itemFactory.itemWithLore(material, messageService.get("gui.region-list.region.name", Map.of(
                        "type", region.getType().name(),
                        "name", region.getName()
                )),
                List.of(
                messageService.get("gui.region-list.region.lore.center", Map.of(
                        "world", region.getWorldName(),
                        "x", String.valueOf(region.getCenterX()),
                        "z", String.valueOf(region.getCenterZ())
                )),
                messageService.get("gui.region-list.region.lore.radius", Map.of(
                        "radius", String.valueOf(region.getRadius())
                )),
                messageService.get("gui.region-list.region.lore.open")
        ));
    }
}
