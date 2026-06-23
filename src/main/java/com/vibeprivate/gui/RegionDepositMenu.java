package com.vibeprivate.gui;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.service.RegionUpgradeService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RegionDepositMenu implements InventoryHolder {
    public static final int ADD_SLOT = 20;
    public static final int WITHDRAW_SLOT = 24;
    public static final int BACK_SLOT = 53;
    private static final int[] DEPOSIT_SLOTS = {30, 31, 32};
    private static final Material[] DISPLAYED_DEPOSITS = {
            Material.DIAMOND_BLOCK,
            Material.EMERALD_BLOCK,
            Material.NETHERITE_BLOCK
    };

    private final MessageService messageService;
    private final RegionUpgradeService upgradeService;
    private final ConfigService configService;
    private final Region region;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;

    public RegionDepositMenu(MessageService messageService, RegionUpgradeService upgradeService,
                             ConfigService configService, Region region) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.upgradeService = Objects.requireNonNull(upgradeService, "upgradeService");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.region = Objects.requireNonNull(region, "region");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.deposit.title"));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Region getRegion() {
        return region;
    }

    private void render() {
        inventory.setItem(4, infoItem());
        inventory.setItem(ADD_SLOT, itemFactory.item(GuiIcon.DEPOSIT, "gui.deposit.add.name",
                List.of("gui.deposit.add.lore.1", "gui.deposit.add.lore.2")));
        inventory.setItem(WITHDRAW_SLOT, itemFactory.item(GuiIcon.WITHDRAW_DEPOSIT, "gui.deposit.withdraw.name",
                List.of("gui.deposit.withdraw.lore.1", "gui.deposit.withdraw.lore.2")));

        for (int index = 0; index < DISPLAYED_DEPOSITS.length; index++) {
            inventory.setItem(DEPOSIT_SLOTS[index], depositItem(DISPLAYED_DEPOSITS[index]));
        }

        inventory.setItem(BACK_SLOT, itemFactory.item(GuiIcon.BACK, "gui.back.name", List.of("gui.back.lore")));
    }

    private ItemStack infoItem() {
        int currentRadius = region.getRadius() == null ? 0 : region.getRadius();
        return itemFactory.itemWithLore(GuiIcon.REGION_INFO, messageService.get("gui.deposit.info.name"),
                List.of(
                        messageService.get("gui.deposit.info.radius", Map.of(
                                "current", Integer.toString(currentRadius),
                                "allowed", Integer.toString(upgradeService.calculateAllowedRadius(region)),
                                "max", Integer.toString(configService.getMaxRadius(region.getType()))
                        )),
                        messageService.get("gui.deposit.info.points", Map.of(
                                "points", Integer.toString(upgradeService.getDepositPoints(region))
                        ))
                ));
    }

    private ItemStack depositItem(Material material) {
        int stored = upgradeService.getDeposits(region.getId()).getOrDefault(material, 0);
        return itemFactory.itemWithLore(material, messageService.get("gui.deposit.item.name", Map.of(
                        "material", material.name()
                )),
                List.of(
                        messageService.get("gui.deposit.item.lore.points", Map.of(
                                "points", Integer.toString(configService.getUpgradeDepositRadiusPoints(material))
                        )),
                        messageService.get("gui.deposit.item.lore.stored", Map.of(
                                "amount", Integer.toString(stored)
                        ))
                ));
    }
}
