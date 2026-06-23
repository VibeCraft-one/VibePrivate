package com.vibeprivate.gui;

import org.bukkit.Material;

public enum GuiIcon {
    BACK(Material.ARROW),
    CLOSE(Material.BARRIER),
    HOME_REGION(Material.OAK_DOOR),
    FARM_REGION(Material.HAY_BLOCK),
    CLAN_REGION(Material.SHIELD),
    ADMIN_REGION(Material.BEACON),
    REGION_INFO(Material.MAP),
    TELEPORT(Material.COMPASS),
    MEMBERS(Material.PLAYER_HEAD),
    FLAGS(Material.REPEATER),
    FUEL(Material.COAL_BLOCK),
    FUEL_INACTIVE(Material.CHARCOAL),
    DEPOSIT(Material.DIAMOND_BLOCK),
    WITHDRAW_DEPOSIT(Material.EMERALD_BLOCK),
    DELETE(Material.BARRIER),
    HELP(Material.BOOK),
    ADD_MEMBER(Material.LIME_DYE),
    REMOVE_MEMBER(Material.RED_DYE),
    EDIT_MEMBER(Material.NAME_TAG),
    SPAWN_POINT(Material.ENDER_PEARL),
    PRESET_SPAWN(Material.BEACON),
    PRESET_PVP(Material.DIAMOND_SWORD),
    PRESET_BOSS(Material.WITHER_SKELETON_SKULL),
    PRESET_SNOWBALL(Material.SNOWBALL),
    PRESET_INTERACT(Material.LEVER),
    PRESET_NO_CLAIM(Material.BARRIER);

    private final Material material;
    private final String textureUrl;

    GuiIcon(Material material) {
        this(material, null);
    }

    GuiIcon(Material material, String textureUrl) {
        this.material = material;
        this.textureUrl = textureUrl;
    }

    public Material material() {
        return material;
    }

    public String textureUrl() {
        return textureUrl;
    }

    public boolean hasTexture() {
        return textureUrl != null && !textureUrl.isBlank();
    }

    public GuiIconSpec spec() {
        return new GuiIconSpec(material, textureUrl);
    }
}
