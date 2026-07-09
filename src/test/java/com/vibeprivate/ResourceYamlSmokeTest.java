package com.vibeprivate;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourceYamlSmokeTest {
    private static final Set<String> REQUIRED_CONFIG_KEYS = Set.of(
            "language",
            "database.type",
            "database.file",
            "database.mysql.host",
            "database.mysql.port",
            "database.mysql.database",
            "database.mysql.username",
            "database.mysql.password",
            "database.mysql.parameters",
            "regions.allowed-worlds",
            "regions.min-y",
            "regions.max-y",
            "limits.home.max-per-player",
            "limits.home.start-radius",
            "limits.home.max-radius",
            "limits.farm.max-per-player",
            "limits.farm.start-radius",
            "limits.farm.max-radius",
            "limits.clan.max-per-clan",
            "limits.clan.start-radius",
            "limits.clan.max-radius",
            "fuel.drain-interval-hours",
            "fuel.max-days",
            "fuel.expired-delete-after-hours",
            "fuel.radius-cost-max-multiplier",
            "upkeep.enabled",
            "upkeep.mode",
            "upkeep.check-interval-minutes",
            "upkeep.interval-hours",
            "upkeep.base-cost-per-chunk",
            "upkeep.size-growth",
            "upkeep.size-power",
            "upkeep.grace-days",
            "upkeep.remove-after-days",
            "upkeep.freeze-after-offline-days",
            "upkeep.offline-rate-multiplier",
            "upkeep.newbie-free-playtime-minutes",
            "upkeep.log-withdrawals",
            "chunk-keeper.enabled",
            "chunk-keeper.buffer-chunks",
            "chunk-keeper.max-chunks-per-region",
            "visualization.border-distance-blocks",
            "visualization.duration-seconds",
            "visualization.cooldown-seconds",
            "visualization.wall-radius-blocks",
            "visualization.wall-height-blocks"
    );
    private static final Set<String> REQUIRED_MESSAGE_KEYS = Set.of(
            "prefix",
            "command.help.header",
            "command.help.open",
            "command.help.help",
            "command.help.admin-open",
            "gui.main.title",
            "gui.admin.main.title",
            "region.create.success",
            "region.create.limit-home",
            "region.create.limit-farm",
            "region.create.limit-clan",
            "protection.denied",
            "home.set.success",
            "home.teleport.success",
            "fuel.add.success"
    );

    @Test
    void bundledConfigContainsRuntimeDefaultKeys() throws IOException {
        YamlConfiguration config = loadYaml("config.yml");

        for (String key : REQUIRED_CONFIG_KEYS) {
            assertPresent(config, key);
        }
    }

    @Test
    void bundledMessagesShareTheSameLeafKeys() throws IOException {
        YamlConfiguration english = loadYaml("messages/en.yml");
        YamlConfiguration russian = loadYaml("messages/ru.yml");

        assertEquals(leafKeys(english), leafKeys(russian), "English and Russian message bundles must stay aligned");
        for (String key : REQUIRED_MESSAGE_KEYS) {
            assertPresent(english, key);
            assertPresent(russian, key);
        }
    }

    private static YamlConfiguration loadYaml(String resourcePath) throws IOException {
        try (InputStream input = ResourceYamlSmokeTest.class.getResourceAsStream("/" + resourcePath)) {
            assertNotNull(input, resourcePath + " must be present on the test runtime classpath");
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(reader);
                assertFalse(yaml.getKeys(false).isEmpty(), resourcePath + " must not load as an empty YAML file");
                return yaml;
            }
        }
    }

    private static Set<String> leafKeys(YamlConfiguration yaml) {
        Set<String> keys = new TreeSet<>();
        for (String key : yaml.getKeys(true)) {
            if (!yaml.isConfigurationSection(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static void assertPresent(YamlConfiguration yaml, String key) {
        assertNotNull(yaml.get(key), () -> "Missing YAML key: " + key);
    }
}
