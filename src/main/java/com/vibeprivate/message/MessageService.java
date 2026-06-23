package com.vibeprivate.message;

import com.vibeprivate.config.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public final class MessageService {
    private final JavaPlugin plugin;
    private final ConfigService configService;
    private YamlConfiguration messages;
    private YamlConfiguration defaults;

    public MessageService(JavaPlugin plugin, ConfigService configService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configService = Objects.requireNonNull(configService, "configService");
    }

    public void load() {
        saveMessageResource("messages/ru.yml");
        saveMessageResource("messages/en.yml");

        String language = configService.getLanguage();
        if (!language.equals("ru") && !language.equals("en")) {
            language = "ru";
        }

        String path = "messages/" + language + ".yml";
        File file = new File(plugin.getDataFolder(), path);
        messages = YamlConfiguration.loadConfiguration(file);
        defaults = loadBundledMessages(path);
        if (defaults != null) {
            messages.setDefaults(defaults);
            if (mergeMissingMessages(messages, defaults)) {
                try {
                    messages.save(file);
                    messages = YamlConfiguration.loadConfiguration(file);
                    messages.setDefaults(defaults);
                } catch (IOException exception) {
                    plugin.getLogger().warning("Could not update missing message keys in " + path + ": " + exception.getMessage());
                }
            }
        }
    }

    public void send(CommandSender sender, String key) {
        Objects.requireNonNull(sender, "sender");
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        Objects.requireNonNull(sender, "sender");
        sender.sendMessage(get(key, placeholders));
    }

    public String get(String key) {
        return get(key, Map.of());
    }

    public String get(String key, Map<String, String> placeholders) {
        Objects.requireNonNull(placeholders, "placeholders");
        String prefix = messages.getString("prefix", "");
        String value = messages.getString(key);
        if (value == null && defaults != null) {
            value = defaults.getString(key);
        }
        if (value == null) {
            value = key;
        }
        value = value.replace("{prefix}", prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return color(value);
    }

    public String plain(String key) {
        return ChatColor.stripColor(get(key));
    }

    private void saveMessageResource(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
    }

    private YamlConfiguration loadBundledMessages(String path) {
        InputStream stream = plugin.getResource(path);
        if (stream == null) {
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (java.io.IOException exception) {
            return null;
        }
    }

    private boolean mergeMissingMessages(YamlConfiguration target, YamlConfiguration source) {
        boolean changed = false;
        for (String key : source.getKeys(true)) {
            if (source.isConfigurationSection(key) || target.contains(key, true)) {
                continue;
            }

            target.set(key, source.get(key));
            changed = true;
        }

        return changed;
    }

    private String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}
