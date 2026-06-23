package com.vibeprivate.gui;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.service.FuelService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RegionFuelMenu implements InventoryHolder {
    public static final int ADD_SLOT = 22;
    public static final int BACK_SLOT = 53;
    private static final int[] FUEL_SLOTS = {29, 30, 31, 32};
    private static final Material[] DISPLAYED_FUEL = {
            Material.COAL_BLOCK,
            Material.COAL,
            Material.CHARCOAL,
            Material.OAK_LOG
    };

    private final MessageService messageService;
    private final FuelService fuelService;
    private final ConfigService configService;
    private final Region region;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;

    public RegionFuelMenu(MessageService messageService, FuelService fuelService,
                          ConfigService configService, Region region) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.fuelService = Objects.requireNonNull(fuelService, "fuelService");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.region = Objects.requireNonNull(region, "region");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.fuel.title"));
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
        inventory.setItem(ADD_SLOT, itemFactory.item(GuiIcon.FUEL, "gui.fuel.add.name",
                List.of("gui.fuel.add.lore.1", "gui.fuel.add.lore.2", "gui.fuel.add.lore.3")));
        for (int index = 0; index < DISPLAYED_FUEL.length; index++) {
            inventory.setItem(FUEL_SLOTS[index], fuelItem(DISPLAYED_FUEL[index]));
        }
        inventory.setItem(BACK_SLOT, itemFactory.item(GuiIcon.BACK, "gui.back.name", List.of("gui.back.lore")));
    }

    private ItemStack infoItem() {
        return itemFactory.itemWithLore(region.isEnabled() ? GuiIcon.FUEL : GuiIcon.FUEL_INACTIVE,
                messageService.get("gui.fuel.info.name"),
                List.of(
                        messageService.get(region.isEnabled() ? "gui.fuel.info.active" : "gui.fuel.info.inactive"),
                        messageService.get("gui.fuel.info.remaining", Map.of(
                                "time", fuelService.formatRemaining(region)
                        )),
                        messageService.get("gui.fuel.info.max", Map.of(
                                "days", Integer.toString(configService.getFuelMaxDays())
                        )),
                        messageService.get("gui.fuel.info.multiplier", Map.of(
                                "multiplier", String.format(java.util.Locale.US, "%.2f", fuelService.getFuelCostMultiplier(region))
                        ))
                ));
    }

    private ItemStack fuelItem(Material material) {
        return itemFactory.itemWithLore(material, messageService.get("gui.fuel.item.name", Map.of(
                        "material", material.name()
                )),
                List.of(messageService.get("gui.fuel.item.lore.minutes", Map.of(
                        "minutes", Integer.toString(configService.getFuelMinutes(material))
                ))));
    }
}
