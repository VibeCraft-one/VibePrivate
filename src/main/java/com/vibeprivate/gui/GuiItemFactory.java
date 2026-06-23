package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class GuiItemFactory {
    private final MessageService messageService;
    private final ConcurrentMap<String, ItemStack> headCache = new ConcurrentHashMap<>();

    public GuiItemFactory(MessageService messageService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
    }

    public ItemStack item(Material material, String nameKey, List<String> loreKeys) {
        return item(material, nameKey, Map.of(), loreKeys, Map.of());
    }

    public ItemStack item(GuiIcon icon, String nameKey, List<String> loreKeys) {
        return item(icon.spec(), nameKey, Map.of(), loreKeys, Map.of());
    }

    public ItemStack item(GuiIcon icon, String nameKey, Map<String, String> namePlaceholders,
                          List<String> loreKeys, Map<String, String> lorePlaceholders) {
        return item(icon.spec(), nameKey, namePlaceholders, loreKeys, lorePlaceholders);
    }

    public ItemStack item(GuiIconSpec icon, String nameKey, List<String> loreKeys) {
        return item(icon, nameKey, Map.of(), loreKeys, Map.of());
    }

    public ItemStack item(GuiIconSpec icon, String nameKey, Map<String, String> namePlaceholders,
                          List<String> loreKeys, Map<String, String> lorePlaceholders) {
        return applyText(createIconItem(icon), nameKey, namePlaceholders, loreKeys, lorePlaceholders);
    }

    public ItemStack item(Material material, String nameKey, Map<String, String> namePlaceholders,
                          List<String> loreKeys, Map<String, String> lorePlaceholders) {
        return applyText(new ItemStack(material), nameKey, namePlaceholders, loreKeys, lorePlaceholders);
    }

    public ItemStack itemWithLore(GuiIcon icon, String displayName, List<String> lore) {
        return itemWithLore(icon.spec(), displayName, lore);
    }

    public ItemStack itemWithLore(GuiIconSpec icon, String displayName, List<String> lore) {
        return applyText(createIconItem(icon), displayName, lore);
    }

    public ItemStack itemWithLore(Material material, String displayName, List<String> lore) {
        return applyText(new ItemStack(material), displayName, lore);
    }

    public ItemStack customHead(String textureUrl, String nameKey, List<String> loreKeys) {
        return applyText(createHeadItem(textureUrl), nameKey, Map.of(), loreKeys, Map.of());
    }

    private ItemStack createIconItem(GuiIconSpec icon) {
        Objects.requireNonNull(icon, "icon");
        if (icon.hasTexture()) {
            return createHeadItem(icon.textureUrl());
        }

        return new ItemStack(icon.fallbackMaterial());
    }

    private ItemStack applyText(ItemStack item, String nameKey, Map<String, String> namePlaceholders,
                                List<String> loreKeys, Map<String, String> lorePlaceholders) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(messageService.get(nameKey, namePlaceholders));
        meta.setLore(loreKeys.stream()
                .map(key -> messageService.get(key, lorePlaceholders))
                .toList());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack applyText(ItemStack item, String displayName, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createHeadItem(String textureUrl) {
        ItemStack cached = headCache.get(textureUrl);
        if (cached != null) {
            return cached.clone();
        }

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof SkullMeta skullMeta)) {
            return item;
        }

        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(textureUrl));
            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
            item.setItemMeta(skullMeta);
        } catch (MalformedURLException exception) {
            return new ItemStack(Material.PLAYER_HEAD);
        }

        headCache.put(textureUrl, item.clone());
        return item;
    }

    public ItemStack head(OfflinePlayer owner, String nameKey, Map<String, String> namePlaceholders,
                          List<String> loreKeys, Map<String, String> lorePlaceholders) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(owner);
        }

        meta.setDisplayName(messageService.get(nameKey, namePlaceholders));
        meta.setLore(loreKeys.stream()
                .map(key -> messageService.get(key, lorePlaceholders))
                .toList());
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack headWithLore(OfflinePlayer owner, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(owner);
        }

        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
