package com.vibeprivate.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

final class DatabaseSchema {
    private final DatabaseService databaseService;

    DatabaseSchema(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    void migrate() {
        execute(createRegionsSql());
        execute(createRegionMembersSql());
        execute(createRegionMemberFlagsSql());
        execute(createRegionDefaultFlagsSql());
        execute(createRegionDepositsSql());
        execute(createRegionHomesSql());
        execute(createRegionFuelSlotsSql());
        execute(createPendingConfirmationsSql());
        execute(createProtectedChunksSql());
        createIndex("idx_protected_chunks_region", "protected_chunks", "region_id");
        createIndex("idx_protected_chunks_owner", "protected_chunks", "owner_id");
    }

    private Connection connection() {
        return databaseService.getConnection();
    }

    private boolean isMySql() {
        return databaseService.isMySql();
    }

    private void execute(String sql) {
        try (Statement statement = connection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to migrate database.", exception);
        }
    }

    private void createIndex(String name, String table, String column) {
        String sql = isMySql()
                ? "CREATE INDEX " + name + " ON " + table + "(" + column + ")"
                : "CREATE INDEX IF NOT EXISTS " + name + " ON " + table + "(" + column + ")";

        try (Statement statement = connection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            if (isMySql() && isDuplicateIndex(exception)) {
                return;
            }

            throw new IllegalStateException("Failed to create database index " + name + ".", exception);
        }
    }

    private boolean isDuplicateIndex(SQLException exception) {
        return exception.getErrorCode() == 1061;
    }

    private String createRegionsSql() {
        if (isMySql()) {
            return """
                    CREATE TABLE IF NOT EXISTS regions (
                        id VARCHAR(64) PRIMARY KEY,
                        name VARCHAR(64) NOT NULL,
                        type VARCHAR(32) NOT NULL,
                        owner_id VARCHAR(128) NOT NULL,
                        world VARCHAR(128) NOT NULL,
                        shape VARCHAR(32) NOT NULL,
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
                        created_at BIGINT NOT NULL,
                        fuel_expires_at BIGINT NOT NULL DEFAULT 0,
                        fuel_empty_since BIGINT NOT NULL DEFAULT 0,
                        last_fuel_drain_at BIGINT NOT NULL DEFAULT 0,
                        upgrade_level INTEGER NOT NULL DEFAULT 0,
                        visualization_mode VARCHAR(32) NOT NULL DEFAULT 'ALL'
                    )
                    """;
        }

        return """
                CREATE TABLE IF NOT EXISTS regions (
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
                    created_at INTEGER NOT NULL,
                    fuel_expires_at INTEGER NOT NULL DEFAULT 0,
                    fuel_empty_since INTEGER NOT NULL DEFAULT 0,
                    last_fuel_drain_at INTEGER NOT NULL DEFAULT 0,
                    upgrade_level INTEGER NOT NULL DEFAULT 0,
                    visualization_mode TEXT NOT NULL DEFAULT 'ALL'
                )
                """;
    }

    private String createRegionMembersSql() {
        String regionIdType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String playerIdType = isMySql() ? "VARCHAR(36)" : "TEXT";
        String longType = isMySql() ? "BIGINT" : "INTEGER";
        return """
                CREATE TABLE IF NOT EXISTS region_members (
                    region_id %s NOT NULL,
                    player_id %s NOT NULL,
                    added_at %s NOT NULL,
                    PRIMARY KEY (region_id, player_id),
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )
                """.formatted(regionIdType, playerIdType, longType);
    }

    private String createRegionMemberFlagsSql() {
        String regionIdType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String playerIdType = isMySql() ? "VARCHAR(36)" : "TEXT";
        String flagType = isMySql() ? "VARCHAR(64)" : "TEXT";
        return """
                CREATE TABLE IF NOT EXISTS region_member_flags (
                    region_id %s NOT NULL,
                    player_id %s NOT NULL,
                    flag %s NOT NULL,
                    enabled INTEGER NOT NULL,
                    PRIMARY KEY (region_id, player_id, flag),
                    FOREIGN KEY (region_id, player_id) REFERENCES region_members(region_id, player_id) ON DELETE CASCADE
                )
                """.formatted(regionIdType, playerIdType, flagType);
    }

    private String createRegionDefaultFlagsSql() {
        String regionIdType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String flagType = isMySql() ? "VARCHAR(64)" : "TEXT";
        return """
                CREATE TABLE IF NOT EXISTS region_default_flags (
                    region_id %s NOT NULL,
                    flag %s NOT NULL,
                    enabled INTEGER NOT NULL,
                    PRIMARY KEY (region_id, flag),
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )
                """.formatted(regionIdType, flagType);
    }

    private String createRegionDepositsSql() {
        String regionIdType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String materialType = isMySql() ? "VARCHAR(64)" : "TEXT";
        return """
                CREATE TABLE IF NOT EXISTS region_deposits (
                    region_id %s NOT NULL,
                    material %s NOT NULL,
                    amount INTEGER NOT NULL,
                    PRIMARY KEY (region_id, material),
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )
                """.formatted(regionIdType, materialType);
    }

    private String createRegionFuelSlotsSql() {
        String regionIdType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String materialType = isMySql() ? "VARCHAR(64)" : "TEXT";
        return """
                CREATE TABLE IF NOT EXISTS region_fuel_slots (
                    region_id %s NOT NULL,
                    slot INTEGER NOT NULL,
                    material %s,
                    amount INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (region_id, slot),
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )
                """.formatted(regionIdType, materialType);
    }

    private String createRegionHomesSql() {
        String regionIdType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String worldType = isMySql() ? "VARCHAR(128)" : "TEXT";
        String doubleType = isMySql() ? "DOUBLE" : "REAL";
        return """
                CREATE TABLE IF NOT EXISTS region_homes (
                    region_id %s PRIMARY KEY,
                    world %s NOT NULL,
                    x %s NOT NULL,
                    y %s NOT NULL,
                    z %s NOT NULL,
                    yaw %s NOT NULL,
                    pitch %s NOT NULL,
                    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
                )
                """.formatted(regionIdType, worldType, doubleType, doubleType, doubleType, doubleType, doubleType);
    }

    private String createPendingConfirmationsSql() {
        String idType = isMySql() ? "VARCHAR(191)" : "TEXT";
        String actorType = isMySql() ? "VARCHAR(36)" : "TEXT";
        String actionType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String targetType = isMySql() ? "VARCHAR(191)" : "TEXT";
        String longType = isMySql() ? "BIGINT" : "INTEGER";
        return """
                CREATE TABLE IF NOT EXISTS pending_confirmations (
                    id %s PRIMARY KEY,
                    actor_id %s NOT NULL,
                    action %s NOT NULL,
                    target_id %s NOT NULL,
                    attempts INTEGER NOT NULL,
                    expires_at %s NOT NULL
                )
                """.formatted(idType, actorType, actionType, targetType, longType);
    }

    private String createProtectedChunksSql() {
        String worldType = isMySql() ? "VARCHAR(128)" : "TEXT";
        String ownerType = isMySql() ? "VARCHAR(128)" : "TEXT";
        String regionIdType = isMySql() ? "VARCHAR(64)" : "TEXT";
        String regionType = isMySql() ? "VARCHAR(32)" : "TEXT";
        String sourceType = isMySql() ? "VARCHAR(32)" : "TEXT";
        String longType = isMySql() ? "BIGINT" : "INTEGER";
        return """
                CREATE TABLE IF NOT EXISTS protected_chunks (
                    world %s NOT NULL,
                    chunk_x INTEGER NOT NULL,
                    chunk_z INTEGER NOT NULL,
                    owner_id %s NOT NULL,
                    region_id %s NOT NULL,
                    region_type %s NOT NULL,
                    source %s NOT NULL,
                    first_seen_at %s NOT NULL,
                    last_protected_at %s NOT NULL,
                    PRIMARY KEY (world, chunk_x, chunk_z)
                )
                """.formatted(worldType, ownerType, regionIdType, regionType, sourceType, longType, longType);
    }
}
