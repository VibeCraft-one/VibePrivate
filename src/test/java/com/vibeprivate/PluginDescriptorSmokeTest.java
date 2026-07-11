package com.vibeprivate;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDescriptorSmokeTest {
    private static final List<String> REQUIRED_COMMANDS = List.of(
            "vp",
            "privat",
            "privatadmin",
            "home",
            "sethome"
    );
    private static final List<String> REQUIRED_PERMISSIONS = List.of(
            "vibeprivate.bypass",
            "vibeprivate.admin"
    );

    @Test
    void pluginDescriptorDeclaresPublicIdentityAndCommands() throws IOException {
        String descriptor = readPluginDescriptor();

        assertTopLevelEntry(descriptor, "name", "VibeRegionGuard");
        assertTopLevelEntry(descriptor, "main", "com.vibeprivate.VibePrivatePlugin");
        assertTopLevelEntry(descriptor, "api-version", "'1.21'");
        assertTopLevelSection(descriptor, "commands");
        for (String command : REQUIRED_COMMANDS) {
            assertNestedEntry(descriptor, command);
        }
        assertTopLevelSection(descriptor, "permissions");
        for (String permission : REQUIRED_PERMISSIONS) {
            assertNestedEntry(descriptor, permission);
        }
    }

    private static String readPluginDescriptor() throws IOException {
        try (InputStream input = PluginDescriptorSmokeTest.class.getResourceAsStream("/plugin.yml")) {
            assertNotNull(input, "plugin.yml must be present on the test runtime classpath");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertTopLevelEntry(String descriptor, String key, String value) {
        assertTrue(descriptor.lines().anyMatch(line -> line.equals(key + ": " + value)),
                () -> "plugin.yml must declare " + key + ": " + value);
    }

    private static void assertTopLevelSection(String descriptor, String section) {
        assertTrue(descriptor.lines().anyMatch(line -> line.equals(section + ":")),
                () -> "plugin.yml must declare top-level section " + section);
    }

    private static void assertNestedEntry(String descriptor, String key) {
        assertTrue(descriptor.lines().anyMatch(line -> line.equals("  " + key + ":")),
                () -> "plugin.yml must declare nested entry " + key);
    }
}
