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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class AdminPlayerRegionListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 53;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final Player player;
    private final String ownerId;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> regionIdsBySlot = new HashMap<>();

    public AdminPlayerRegionListMenu(MessageService messageService, RegionManager regionManager,
                                     Player player, String ownerId) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.player = Objects.requireNonNull(player, "player");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId");
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

    public String getRegionId(int slot) {
        return regionIdsBySlot.get(slot);
    }

    private void render() {
        List<Region> regions = regionManager.getRegionsByOwner(ownerId).stream()
                .filter(region -> !region.isAdmin())
                .sorted(Comparator.comparing(Region::getType).thenComparing(Region::getName))
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
            inventory.setItem(22, itemFactory.item(Material.BARRIER, "gui.admin.player-regions.empty.name",
                    List.of("gui.admin.player-regions.empty.lore")));
        }

        inventory.setItem(BACK_SLOT, itemFactory.item(GuiIcon.BACK, "gui.back.name", List.of("gui.back.lore")));
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
