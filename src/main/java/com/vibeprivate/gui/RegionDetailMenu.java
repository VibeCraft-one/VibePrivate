package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.FlagCatalog;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.service.AdminRegionPreset;
import com.vibeprivate.service.FuelService;
import com.vibeprivate.service.RegionAccessService;
import com.vibeprivate.service.RegionUpgradeService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RegionDetailMenu implements InventoryHolder {
    public static final int HOME_SLOT = 45;
    public static final int MEMBERS_SLOT = 46;
    public static final int FUEL_SLOT = 47;
    public static final int UPGRADE_SLOT = 48;
    public static final int SET_HOME_SLOT = 49;
    public static final int WITHDRAW_SLOT = 50;
    public static final int DELETE_SLOT = 51;
    public static final int BACK_SLOT = 53;

    private final MessageService messageService;
    private final RegionAccessService accessService;
    private final FuelService fuelService;
    private final RegionUpgradeService upgradeService;
    private final Region region;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final FlagItemFactory flagItemFactory;
    private final AdminPresetItemFactory presetItemFactory;
    private final Map<Integer, RegionFlag> flagsBySlot = new HashMap<>();
    private final Map<Integer, AdminRegionPreset> presetsBySlot = new HashMap<>();

    public RegionDetailMenu(MessageService messageService, RegionAccessService accessService,
                            FuelService fuelService, RegionUpgradeService upgradeService, Region region) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.accessService = Objects.requireNonNull(accessService, "accessService");
        this.fuelService = Objects.requireNonNull(fuelService, "fuelService");
        this.upgradeService = Objects.requireNonNull(upgradeService, "upgradeService");
        this.region = Objects.requireNonNull(region, "region");
        this.itemFactory = new GuiItemFactory(messageService);
        this.flagItemFactory = new FlagItemFactory(messageService, accessService, itemFactory);
        this.presetItemFactory = new AdminPresetItemFactory(itemFactory);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.region-detail.title"));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Region getRegion() {
        return region;
    }

    public RegionFlag getFlag(int slot) {
        return flagsBySlot.get(slot);
    }

    public AdminRegionPreset getPreset(int slot) {
        return presetsBySlot.get(slot);
    }

    private void render() {
        inventory.setItem(4, infoItem());
        int slotIndex = 0;
        for (RegionFlag flag : visibleFlags()) {
            if (slotIndex >= GuiSlots.REGION_FLAG_SLOTS.length) {
                break;
            }
            int slot = GuiSlots.REGION_FLAG_SLOTS[slotIndex++];
            flagsBySlot.put(slot, flag);
            inventory.setItem(slot, flagItemFactory.regionFlagItem(region, flag));
        }

        if (region.isAdmin()) {
            renderPresets();
        }

        inventory.setItem(HOME_SLOT, teleportItem());
        if (region.isAdmin()) {
            inventory.setItem(MEMBERS_SLOT, namedItem(Material.PLAYER_HEAD, "gui.region-detail.guests.name",
                    List.of("gui.region-detail.guests.lore.1", "gui.region-detail.guests.lore.2")));
        } else if (isPlayerManagedRegion()) {
            inventory.setItem(MEMBERS_SLOT, namedItem(Material.PLAYER_HEAD, "gui.region-detail.members.name",
                    List.of("gui.region-detail.members.lore.1", "gui.region-detail.members.lore.2")));
        }
        if (!region.isAdmin()) {
            inventory.setItem(FUEL_SLOT, fuelItem());
            inventory.setItem(UPGRADE_SLOT, upgradeItem());
            if (region.getType() == RegionType.HOME) {
                inventory.setItem(SET_HOME_SLOT, namedItem(GuiIcon.SPAWN_POINT, "gui.region-detail.set-home.name",
                        List.of("gui.region-detail.set-home.lore.1", "gui.region-detail.set-home.lore.2")));
            }
            inventory.setItem(WITHDRAW_SLOT, namedItem(Material.EMERALD_BLOCK, "gui.region-detail.withdraw.name",
                    List.of("gui.region-detail.withdraw.lore.1", "gui.region-detail.withdraw.lore.2")));
        }
        inventory.setItem(DELETE_SLOT, namedItem(Material.BARRIER, "gui.region-detail.delete.name",
                List.of("gui.region-detail.delete.lore.1", "gui.region-detail.delete.lore.2")));
        inventory.setItem(BACK_SLOT, namedItem(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
    }

    private ItemStack infoItem() {
        return itemFactory.itemWithLore(Material.MAP, messageService.get("gui.region-detail.info.name", Map.of(
                        "type", region.getType().name(),
                        "name", region.getName()
                )),
                List.of(
                messageService.get("gui.region-detail.info.lore.owner", Map.of("owner", displayOwner())),
                messageService.get("gui.region-detail.info.lore.world", Map.of("world", region.getWorldName())),
                messageService.get("gui.region-detail.info.lore.center", Map.of(
                        "x", String.valueOf(region.getCenterX()),
                        "z", String.valueOf(region.getCenterZ())
                )),
                messageService.get("gui.region-detail.info.lore.radius", Map.of(
                        "radius", String.valueOf(region.getRadius())
                ))
        ));
    }

    private ItemStack fuelItem() {
        return itemFactory.itemWithLore(region.isEnabled() ? Material.COAL_BLOCK : Material.CHARCOAL,
                messageService.get("gui.region-detail.fuel.name"),
                List.of(
                messageService.get(region.isEnabled() ? "gui.region-detail.fuel.active" : "gui.region-detail.fuel.inactive"),
                messageService.get("gui.region-detail.fuel.remaining", Map.of(
                        "time", fuelService.formatRemaining(region)
                )),
                messageService.get("gui.region-detail.fuel.list"),
                messageService.get("gui.region-detail.fuel.lore.1"),
                messageService.get("gui.region-detail.fuel.lore.2")
        ));
    }

    private ItemStack upgradeItem() {
        return itemFactory.itemWithLore(Material.DIAMOND_BLOCK,
                messageService.get("gui.region-detail.upgrade.name"),
                List.of(
                messageService.get("gui.region-detail.upgrade.radius", Map.of(
                        "current", String.valueOf(region.getRadius()),
                        "allowed", String.valueOf(upgradeService.calculateAllowedRadius(region))
                )),
                messageService.get("gui.region-detail.upgrade.deposit", Map.of(
                        "points", String.valueOf(upgradeService.getDepositPoints(region))
                )),
                messageService.get("gui.region-detail.upgrade.list"),
                messageService.get("gui.region-detail.upgrade.lore.1"),
                messageService.get("gui.region-detail.upgrade.lore.2")
        ));
    }

    private ItemStack teleportItem() {
        if (region.getType() == RegionType.FARM) {
            return namedItem(Material.HAY_BLOCK, "gui.region-detail.farm-teleport.name",
                    List.of("gui.region-detail.farm-teleport.lore.1", "gui.region-detail.farm-teleport.lore.2"));
        }

        if (region.isAdmin()) {
            return namedItem(Material.COMPASS, "gui.region-detail.admin-teleport.name",
                    List.of("gui.region-detail.admin-teleport.lore.1", "gui.region-detail.admin-teleport.lore.2"));
        }

        return namedItem(Material.COMPASS, "gui.region-detail.home.name",
                List.of("gui.region-detail.home.lore.1", "gui.region-detail.home.lore.2"));
    }

    private ItemStack namedItem(Material material, String nameKey, List<String> loreKeys) {
        return itemFactory.item(material, nameKey, loreKeys);
    }

    private ItemStack namedItem(GuiIcon icon, String nameKey, List<String> loreKeys) {
        return itemFactory.item(icon, nameKey, loreKeys);
    }

    private void renderPresets() {
        AdminRegionPreset[] presets = AdminRegionPreset.values();
        for (int index = 0; index < presets.length && index < GuiSlots.ADMIN_PRESET_SLOTS.length; index++) {
            AdminRegionPreset preset = presets[index];
            int slot = GuiSlots.ADMIN_PRESET_SLOTS[index];
            presetsBySlot.put(slot, preset);
            inventory.setItem(slot, presetItemFactory.item(preset));
        }
    }

    private String displayOwner() {
        try {
            UUID ownerId = UUID.fromString(region.getOwnerId());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerId);
            if (offlinePlayer.getName() != null) {
                return offlinePlayer.getName();
            }

            return ownerId.toString().substring(0, 8);
        } catch (IllegalArgumentException exception) {
            return region.getOwnerId();
        }
    }

    private RegionFlag[] visibleFlags() {
        return region.isAdmin() ? FlagCatalog.adminRegionFlags() : FlagCatalog.playerRegionFlags();
    }

    private boolean isPlayerManagedRegion() {
        return region.getType() == RegionType.HOME || region.getType() == RegionType.FARM;
    }
}
