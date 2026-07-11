package com.vibeprivate.storage;

import com.vibeprivate.config.ConfigService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.sql.PreparedStatement;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseSchemaSmokeTest {
    private static final List<String> REQUIRED_TABLES = List.of(
            "regions",
            "region_members",
            "region_member_flags",
            "region_default_flags",
            "region_deposits",
            "region_homes",
            "region_fuel_slots",
            "clan_region_roles",
            "owner_upkeep",
            "region_lifecycle",
            "pending_confirmations",
            "protected_chunks"
    );
    private static final List<String> REQUIRED_INDEXES = List.of(
            "idx_clan_region_roles_player",
            "idx_region_lifecycle_status",
            "idx_protected_chunks_region",
            "idx_protected_chunks_owner"
    );

    @TempDir
    Path tempDir;

    @Test
    void sqliteMigrationCreatesCurrentSchemaAndEnforcesForeignKeys() throws SQLException {
        DatabaseService databaseService = openSqliteDatabase("schema-smoke.db");
        try {
            databaseService.migrate();

            Connection connection = databaseService.getConnection();
            for (String table : REQUIRED_TABLES) {
                assertObjectExists(connection, "table", table);
            }
            for (String index : REQUIRED_INDEXES) {
                assertObjectExists(connection, "index", index);
            }
            assertForeignKeysAreEnabled(connection);
            assertOrphanRegionMemberIsRejected(connection);
        } finally {
            databaseService.close();
        }
    }

    @Test
    void sqliteMigrationAddsCurrentRegionColumnsToLegacyRegionsTable() throws SQLException {
        DatabaseService databaseService = openSqliteDatabase("legacy-regions.db");
        try {
            createLegacyRegionsTable(databaseService.getConnection());

            databaseService.migrate();

            Connection connection = databaseService.getConnection();
            assertRegionColumn(connection, "fuel_expires_at");
            assertRegionColumn(connection, "fuel_empty_since");
            assertRegionColumn(connection, "last_fuel_drain_at");
            assertRegionColumn(connection, "upgrade_level");
            assertRegionColumn(connection, "visualization_mode");
            assertLegacyRegionLoadsWithCurrentDefaults(databaseService);
        } finally {
            databaseService.close();
        }
    }

    private DatabaseService openSqliteDatabase(String databaseFile) {
        JavaPlugin plugin = mock(JavaPlugin.class);
        FileConfiguration config = mock(FileConfiguration.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("DatabaseSchemaSmokeTest"));
        when(plugin.getConfig()).thenReturn(config);
        when(config.getString("database.type", "sqlite")).thenReturn("sqlite");
        when(config.getString("database.file", "vibeprivate.db")).thenReturn(databaseFile);

        ConfigService configService = new ConfigService(plugin);
        configService.load();

        DatabaseService databaseService = new DatabaseService(plugin, configService);
        databaseService.open();
        return databaseService;
    }

    private static void assertObjectExists(Connection connection, String type, String name) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = '" + type + "' AND name = '" + name + "'")) {
            assertTrue(result.next(), () -> "Expected SQLite " + type + " to exist: " + name);
        }
    }

    private static void assertForeignKeysAreEnabled(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA foreign_keys")) {
            assertTrue(result.next(), "SQLite foreign_keys pragma must return a value");
            assertTrue(result.getInt(1) == 1, "SQLite foreign keys must be enabled");
        }
    }

    private static void assertOrphanRegionMemberIsRejected(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO region_members (region_id, player_id, added_at)
                    VALUES ('missing-region', '00000000-0000-0000-0000-000000000000', 1)
                    """);
            fail("SQLite foreign keys must reject orphan region members");
        } catch (SQLException exception) {
            assertTrue(exception.getMessage().toLowerCase().contains("foreign key"),
                    () -> "Expected foreign key failure, got: " + exception.getMessage());
        }
    }

    private static void createLegacyRegionsTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE regions (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        owner_id TEXT NOT NULL,
                        world TEXT NOT NULL,
                        shape TEXT NOT NULL,
                        center_x INTEGER,
                        center_z INTEGER,
                        radius INTEGER,
                        min_y INTEGER NOT NULL,
                        max_y INTEGER NOT NULL,
                        pos1_x INTEGER,
                        pos1_y INTEGER,
                        pos1_z INTEGER,
                        pos2_x INTEGER,
                        pos2_y INTEGER,
                        pos2_z INTEGER,
                        enabled INTEGER NOT NULL DEFAULT 1,
                        created_at INTEGER NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO regions (
                        id, name, type, owner_id, world, shape,
                        center_x, center_z, radius, min_y, max_y, enabled, created_at
                    ) VALUES (
                        'legacy-home', 'legacy-home', 'HOME', 'owner-id', 'world', 'RADIUS',
                        0, 0, 8, 64, 100, 1, 123
                    )
                    """);
        }
    }

    private static void assertRegionColumn(Connection connection, String column) throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), null, "regions", null)) {
            while (columns.next()) {
                if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) {
                    return;
                }
            }
        }

        fail("Expected regions column after migration: " + column);
    }

    private static void assertLegacyRegionLoadsWithCurrentDefaults(DatabaseService databaseService) throws SQLException {
        assertEquals(1, new RegionRepository(databaseService).loadAll().size());
        try (PreparedStatement statement = databaseService.getConnection()
                .prepareStatement("SELECT fuel_expires_at, fuel_empty_since, last_fuel_drain_at, "
                        + "upgrade_level, visualization_mode FROM regions WHERE id = ?")) {
            statement.setString(1, "legacy-home");
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next(), "Legacy region must still exist after migration");
                assertEquals(0L, result.getLong("fuel_expires_at"));
                assertEquals(0L, result.getLong("fuel_empty_since"));
                assertEquals(0L, result.getLong("last_fuel_drain_at"));
                assertEquals(0, result.getInt("upgrade_level"));
                assertEquals("ALL", result.getString("visualization_mode"));
            }
        }
    }
}
