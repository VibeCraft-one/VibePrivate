package com.vibeprivate.gui;

import org.bukkit.Material;

import java.util.Objects;

public record GuiIconSpec(Material fallbackMaterial, String textureUrl) {
    public GuiIconSpec {
        Objects.requireNonNull(fallbackMaterial, "fallbackMaterial");
    }

    public static GuiIconSpec material(Material material) {
        return new GuiIconSpec(material, null);
    }

    public static GuiIconSpec head(String textureUrl, Material fallbackMaterial) {
        return new GuiIconSpec(fallbackMaterial, Objects.requireNonNull(textureUrl, "textureUrl"));
    }

    public boolean hasTexture() {
        return textureUrl != null && !textureUrl.isBlank();
    }
}
