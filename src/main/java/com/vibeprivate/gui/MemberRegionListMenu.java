package com.vibeprivate.gui;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.service.RegionAccessService;
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

public final class MemberRegionListMenu implements InventoryHolder {
    public static final int BACK_SLOT = 26;

    private final MessageService messageService;
    private final RegionManager regionManager;
    private final RegionAccessService regionAccessService;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> regionIdsBySlot = new HashMap<>();

    public MemberRegionListMenu(MessageService messageService, RegionManager regionManager,
                                RegionAccessService regionAccessService, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.regionAccessService = Objects.requireNonNull(regionAccessService, "regionAccessService");
        this.player = Objects.requireNonNull(player, "player");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 27, messageService.get("gui.member-regions.title"));
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
        List<Region> regions = regionAccessService.getRegionIdsByMember(player.getUniqueId()).stream()
                .map(regionManager::getRegion)
                .flatMap(java.util.Optional::stream)
                .filter(RegionMenuPermissions::isPlayerManagedRegion)
                .sorted(Comparator.comparing(Region::getType).thenComparing(Region::getName))
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
            inventory.setItem(13, itemFactory.item(Material.GRAY_DYE, "gui.member-regions.empty.name",
                    List.of("gui.member-regions.empty.lore")));
        }

        inventory.setItem(BACK_SLOT, itemFactory.item(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
    }

    private ItemStack regionItem(Region region) {
        Material material = switch (region.getType()) {
            case HOME -> Material.OAK_DOOR;
            case FARM -> Material.WHEAT;
            case CLAN -> Material.BLUE_BANNER;
            case ADMIN -> Material.BARRIER;
        };

        return itemFactory.itemWithLore(material, messageService.get("gui.member-regions.region.name", Map.of(
                        "type", region.getType().name(),
                        "name", region.getName()
                )),
                List.of(
                        messageService.get("gui.member-regions.region.lore.owner", Map.of("owner", displayOwner(region))),
                        messageService.get("gui.member-regions.region.lore.radius", Map.of(
                                "radius", Integer.toString(region.getRadius())
                        )),
                        messageService.get("gui.member-regions.region.lore.info")
                ));
    }

    private String displayOwner(Region region) {
        try {
            UUID ownerId = UUID.fromString(region.getOwnerId());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerId);
            String name = offlinePlayer.getName();
            return name == null ? ownerId.toString().substring(0, 8) : name;
        } catch (IllegalArgumentException exception) {
            return region.getOwnerId();
        }
    }
}
