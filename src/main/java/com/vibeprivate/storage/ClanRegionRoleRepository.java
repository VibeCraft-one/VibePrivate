package com.vibeprivate.storage;

import com.vibeprivate.model.ClanRegionRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClanRegionRoleRepository {
    private final DatabaseService databaseService;

    public ClanRegionRoleRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    protected ClanRegionRoleRepository() {
        this.databaseService = null;
    }

    public Map<String, Map<UUID, ClanRegionRole>> loadAll() {
        Map<String, Map<UUID, ClanRegionRole>> roles = new HashMap<>();
        String sql = "SELECT region_id, player_id, role FROM clan_region_roles";

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String regionId = resultSet.getString("region_id");
                UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                ClanRegionRole role = ClanRegionRole.valueOf(resultSet.getString("role"));
                roles.computeIfAbsent(regionId, ignored -> new HashMap<>()).put(playerId, role);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load clan region roles.", exception);
        }

        return roles;
    }

    public void save(String regionId, UUID playerId, ClanRegionRole role) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(role, "role");

        try (PreparedStatement statement = connection().prepareStatement(databaseService.upsertClanRegionRoleSql())) {
            statement.setString(1, regionId);
            statement.setString(2, playerId.toString());
            statement.setString(3, role.name());
            statement.setLong(4, System.currentTimeMillis());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save clan region role.", exception);
        }
    }

    public void delete(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");

        try (PreparedStatement statement = connection().prepareStatement(
                "DELETE FROM clan_region_roles WHERE region_id = ? AND player_id = ?")) {
            statement.setString(1, regionId);
            statement.setString(2, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete clan region role.", exception);
        }
    }

    private Connection connection() {
        if (databaseService == null) {
            throw new IllegalStateException("Database service is not configured.");
        }
        return databaseService.getConnection();
    }
}
