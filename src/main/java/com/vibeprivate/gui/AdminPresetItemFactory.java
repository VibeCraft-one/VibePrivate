package com.vibeprivate.gui;

import com.vibeprivate.service.AdminRegionPreset;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

final class AdminPresetItemFactory {
    private final GuiItemFactory itemFactory;

    AdminPresetItemFactory(GuiItemFactory itemFactory) {
        this.itemFactory = Objects.requireNonNull(itemFactory, "itemFactory");
    }

    ItemStack item(AdminRegionPreset preset) {
        String key = preset.name().toLowerCase();
        return itemFactory.item(material(preset), "gui.admin.preset." + key + ".name",
                List.of("gui.admin.preset." + key + ".lore.1", "gui.admin.preset." + key + ".lore.2"));
    }

    private Material material(AdminRegionPreset preset) {
        return switch (preset) {
            case SPAWN_SAFE -> Material.BEACON;
            case PVP_ARENA -> Material.DIAMOND_SWORD;
            case BOSS_ZONE -> Material.WITHER_SKELETON_SKULL;
            case SNOWBALL_EVENT -> Material.SNOWBALL;
            case INTERACT_ZONE -> Material.LEVER;
            case NO_CLAIM -> Material.BARRIER;
        };
    }
}
