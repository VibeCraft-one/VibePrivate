package com.vibeprivate.gui;

import org.bukkit.Material;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiIconRegistry {
    private final Map<String, GuiIconSpec> iconsByKey = new ConcurrentHashMap<>();

    public static GuiIconRegistry defaults() {
        GuiIconRegistry registry = new GuiIconRegistry();
        for (GuiIcon icon : GuiIcon.values()) {
            registry.register(icon.name().toLowerCase(Locale.ROOT), icon.spec());
        }

        registry.register("region.home", GuiIcon.HOME_REGION.spec());
        registry.register("region.farm", GuiIcon.FARM_REGION.spec());
        registry.register("region.clan", GuiIcon.CLAN_REGION.spec());
        registry.register("region.admin", GuiIcon.ADMIN_REGION.spec());
        registry.register("button.back", GuiIcon.BACK.spec());
        registry.register("button.close", GuiIcon.CLOSE.spec());
        registry.register("button.help", GuiIcon.HELP.spec());
        registry.register("button.teleport", GuiIcon.TELEPORT.spec());
        registry.register("button.spawn-point", GuiIcon.SPAWN_POINT.spec());
        registry.register("section.fuel", GuiIcon.FUEL.spec());
        registry.register("section.deposit", GuiIcon.DEPOSIT.spec());
        registry.register("section.members", GuiIcon.MEMBERS.spec());
        registry.register("section.flags", GuiIcon.FLAGS.spec());
        return registry;
    }

    public void register(String key, GuiIcon icon) {
        register(key, icon.spec());
    }

    public void registerMaterial(String key, Material material) {
        register(key, GuiIconSpec.material(material));
    }

    public void registerHead(String key, String textureUrl, Material fallbackMaterial) {
        register(key, GuiIconSpec.head(textureUrl, fallbackMaterial));
    }

    public void register(String key, GuiIconSpec icon) {
        iconsByKey.put(normalize(key), Objects.requireNonNull(icon, "icon"));
    }

    public Optional<GuiIconSpec> find(String key) {
        return Optional.ofNullable(iconsByKey.get(normalize(key)));
    }

    public GuiIconSpec getOrDefault(String key, GuiIcon fallback) {
        return find(key).orElse(fallback.spec());
    }

    private String normalize(String key) {
        Objects.requireNonNull(key, "key");
        return key.trim().toLowerCase(Locale.ROOT);
    }
}
