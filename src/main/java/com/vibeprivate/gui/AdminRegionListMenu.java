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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AdminRegionListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 53;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final RegionAccessService accessService;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> regionIdsBySlot = new HashMap<>();

    public AdminRegionListMenu(MessageService messageService, RegionManager regionManager,
                               RegionAccessService accessService, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.accessService = Objects.requireNonNull(accessService, "accessService");
        this.player = Objects.requireNonNull(player, "player");
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

    public String getRegionId(int slot) {
        return regionIdsBySlot.get(slot);
    }

    private void render() {
        List<Region> regions = regionManager.getRegions().stream()
                .filter(Region::isAdmin)
                .sorted(Comparator.comparing(Region::getName))
                .toList();

        int slot = 0;
        for (Region region : regions) {
            if (slot >= 45) {
                break;
            }

            inventory.setItem(slot, regionItem(region));
            regionIdsBySlot.put(slot, region.getId());
            slot++;
        }

        if (regions.isEmpty()) {
            inventory.setItem(22, itemFactory.item(Material.BARRIER, "gui.admin.list.empty.name",
                    List.of("gui.admin.list.empty.lore")));
        }

        inventory.setItem(BACK_SLOT, itemFactory.item(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
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
