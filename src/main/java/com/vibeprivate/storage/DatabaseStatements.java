package com.vibeprivate.storage;

import java.util.Objects;

final class DatabaseStatements {
    private final DatabaseService databaseService;

    DatabaseStatements(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    String upsertRegionSql() {
        if (databaseService.isMySql()) {
            return """
                    INSERT INTO regions (
                        id, name, type, owner_id, world, shape, center_x, center_z, radius,
                        min_y, max_y, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z,
                        enabled, created_at, fuel_expires_at, fuel_empty_since,
                        last_fuel_drain_at, upgrade_level, visualization_mode
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name),
                        type = VALUES(type),
                        owner_id = VALUES(owner_id),
                        world = VALUES(world),
                        shape = VALUES(shape),
                        center_x = VALUES(center_x),
                        center_z = VALUES(center_z),
                        radius = VALUES(radius),
                        min_y = VALUES(min_y),
                        max_y = VALUES(max_y),
                        pos1_x = VALUES(pos1_x),
                        pos1_y = VALUES(pos1_y),
                        pos1_z = VALUES(pos1_z),
                        pos2_x = VALUES(pos2_x),
                        pos2_y = VALUES(pos2_y),
                        pos2_z = VALUES(pos2_z),
                        enabled = VALUES(enabled),
                        fuel_expires_at = VALUES(fuel_expires_at),
                        fuel_empty_since = VALUES(fuel_empty_since),
                        last_fuel_drain_at = VALUES(last_fuel_drain_at),
                        upgrade_level = VALUES(upgrade_level),
                        visualization_mode = VALUES(visualization_mode)
                    """;
        }

        return """
                INSERT INTO regions (
                    id, name, type, owner_id, world, shape, center_x, center_z, radius,
                    min_y, max_y, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z,
                    enabled, created_at, fuel_expires_at, fuel_empty_since,
                    last_fuel_drain_at, upgrade_level, visualization_mode
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    type = excluded.type,
                    owner_id = excluded.owner_id,
                    world = excluded.world,
                    shape = excluded.shape,
                    center_x = excluded.center_x,
                    center_z = excluded.center_z,
                    radius = excluded.radius,
                    min_y = excluded.min_y,
                    max_y = excluded.max_y,
                    pos1_x = excluded.pos1_x,
                    pos1_y = excluded.pos1_y,
                    pos1_z = excluded.pos1_z,
                    pos2_x = excluded.pos2_x,
                    pos2_y = excluded.pos2_y,
                    pos2_z = excluded.pos2_z,
                    enabled = excluded.enabled,
                    fuel_expires_at = excluded.fuel_expires_at,
                    fuel_empty_since = excluded.fuel_empty_since,
                    last_fuel_drain_at = excluded.last_fuel_drain_at,
                    upgrade_level = excluded.upgrade_level,
                    visualization_mode = excluded.visualization_mode
                """;
    }

    String upsertDefaultFlagSql() {
        return databaseService.isMySql()
                ? """
                INSERT INTO region_default_flags(region_id, flag, enabled)
                VALUES(?, ?, ?)
                ON DUPLICATE KEY UPDATE enabled = VALUES(enabled)
                """
                : """
                INSERT INTO region_default_flags(region_id, flag, enabled)
                VALUES(?, ?, ?)
                ON CONFLICT(region_id, flag) DO UPDATE SET enabled = excluded.enabled
                """;
    }

    String upsertMemberSql() {
        return databaseService.isMySql()
                ? """
                INSERT INTO region_members(region_id, player_id, added_at)
                VALUES(?, ?, ?)
                ON DUPLICATE KEY UPDATE added_at = added_at
                """
                : """
                INSERT INTO region_members(region_id, player_id, added_at)
                VALUES(?, ?, ?)
                ON CONFLICT(region_id, player_id) DO UPDATE SET added_at = added_at
                """;
    }

    String upsertMemberFlagSql() {
        return databaseService.isMySql()
                ? """
                INSERT INTO region_member_flags(region_id, player_id, flag, enabled)
                VALUES(?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE enabled = VALUES(enabled)
                """
                : """
                INSERT INTO region_member_flags(region_id, player_id, flag, enabled)
                VALUES(?, ?, ?, ?)
                ON CONFLICT(region_id, player_id, flag) DO UPDATE SET enabled = excluded.enabled
                """;
    }

    String upsertDepositSql() {
        return databaseService.isMySql()
                ? """
                INSERT INTO region_deposits(region_id, material, amount)
                VALUES(?, ?, ?)
                ON DUPLICATE KEY UPDATE amount = VALUES(amount)
                """
                : """
                INSERT INTO region_deposits(region_id, material, amount)
                VALUES(?, ?, ?)
                ON CONFLICT(region_id, material) DO UPDATE SET amount = excluded.amount
                """;
    }

    String upsertProtectedChunkSql() {
        return databaseService.isMySql()
                ? """
                INSERT INTO protected_chunks (
                    world, chunk_x, chunk_z, owner_id, region_id, region_type,
                    source, first_seen_at, last_protected_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    last_protected_at = VALUES(last_protected_at),
                    source = VALUES(source)
                """
                : """
                INSERT INTO protected_chunks (
                    world, chunk_x, chunk_z, owner_id, region_id, region_type,
                    source, first_seen_at, last_protected_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(world, chunk_x, chunk_z) DO UPDATE SET
                    last_protected_at = excluded.last_protected_at,
                    source = excluded.source
                """;
    }

    String upsertRegionHomeSql() {
        return databaseService.isMySql()
                ? """
                INSERT INTO region_homes(region_id, world, x, y, z, yaw, pitch)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    world = VALUES(world),
                    x = VALUES(x),
                    y = VALUES(y),
                    z = VALUES(z),
                    yaw = VALUES(yaw),
                    pitch = VALUES(pitch)
                """
                : """
                INSERT INTO region_homes(region_id, world, x, y, z, yaw, pitch)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(region_id) DO UPDATE SET
                    world = excluded.world,
                    x = excluded.x,
                    y = excluded.y,
                    z = excluded.z,
                    yaw = excluded.yaw,
                    pitch = excluded.pitch
                """;
    }
}
