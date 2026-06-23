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
import java.util.stream.Collectors;

public final class AdminPlayerListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 53;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> ownerIdsBySlot = new HashMap<>();

    public AdminPlayerListMenu(MessageService messageService, RegionManager regionManager, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.player = Objects.requireNonNull(player, "player");
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

    public String getOwnerId(int slot) {
        return ownerIdsBySlot.get(slot);
    }

    private void render() {
        Map<String, List<Region>> regionsByOwner = regionManager.getRegions().stream()
                .filter(region -> !region.isAdmin())
                .collect(Collectors.groupingBy(Region::getOwnerId));

        List<String> ownerIds = regionsByOwner.keySet().stream()
                .sorted(Comparator.comparing(this::displayOwner))
                .toList();

        int slot = 0;
        for (String ownerId : ownerIds) {
            if (slot >= 45) {
                break;
            }

            inventory.setItem(slot, ownerItem(ownerId, regionsByOwner.get(ownerId)));
            ownerIdsBySlot.put(slot, ownerId);
            slot++;
        }

        if (ownerIds.isEmpty()) {
            inventory.setItem(22, itemFactory.item(Material.BARRIER, "gui.admin.players.empty.name",
                    List.of("gui.admin.players.empty.lore")));
        }

        inventory.setItem(BACK_SLOT, itemFactory.item(GuiIcon.BACK, "gui.back.name", List.of("gui.back.lore")));
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
