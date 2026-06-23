package com.vibeprivate.storage;

import com.vibeprivate.model.RegionFlag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class RegionAccessRepository {
    private final DatabaseService databaseService;

    public RegionAccessRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    public Map<String, Set<UUID>> loadMembers() {
        Map<String, Set<UUID>> members = new HashMap<>();
        String sql = "SELECT region_id, player_id FROM region_members";

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String regionId = resultSet.getString("region_id");
                UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                members.computeIfAbsent(regionId, ignored -> new HashSet<>()).add(playerId);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load region members.", exception);
        }

        return members;
    }

    public Map<String, Map<UUID, Map<RegionFlag, Boolean>>> loadMemberFlags() {
        Map<String, Map<UUID, Map<RegionFlag, Boolean>>> flags = new HashMap<>();
        String sql = "SELECT region_id, player_id, flag, enabled FROM region_member_flags";

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String regionId = resultSet.getString("region_id");
                UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                RegionFlag flag = RegionFlag.valueOf(resultSet.getString("flag"));
                boolean enabled = resultSet.getInt("enabled") == 1;

                flags.computeIfAbsent(regionId, ignored -> new HashMap<>())
                        .computeIfAbsent(playerId, ignored -> new EnumMap<>(RegionFlag.class))
                        .put(flag, enabled);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load region member flags.", exception);
        }

        return flags;
    }

    public Map<String, Map<RegionFlag, Boolean>> loadDefaultFlags() {
        Map<String, Map<RegionFlag, Boolean>> flags = new HashMap<>();
        String sql = "SELECT region_id, flag, enabled FROM region_default_flags";

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String regionId = resultSet.getString("region_id");
                RegionFlag flag = RegionFlag.valueOf(resultSet.getString("flag"));
                boolean enabled = resultSet.getInt("enabled") == 1;

                flags.computeIfAbsent(regionId, ignored -> new EnumMap<>(RegionFlag.class)).put(flag, enabled);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load region default flags.", exception);
        }

        return flags;
    }

    public void saveDefaultFlag(String regionId, RegionFlag flag, boolean enabled) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(flag, "flag");

        String sql = databaseService.upsertDefaultFlagSql();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            statement.setString(2, flag.name());
            statement.setInt(3, enabled ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save default region flag.", exception);
        }
    }

    public void addMember(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");

        String sql = databaseService.upsertMemberSql();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            statement.setString(2, playerId.toString());
            statement.setLong(3, System.currentTimeMillis());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to add region member.", exception);
        }
    }

    public void removeMember(String regionId, UUID playerId) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");

        deleteMemberFlags(regionId, playerId);

        String sql = "DELETE FROM region_members WHERE region_id = ? AND player_id = ?";

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            statement.setString(2, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to remove region member.", exception);
        }
    }

    public void saveMemberFlag(String regionId, UUID playerId, RegionFlag flag, boolean enabled) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(flag, "flag");

        try (PreparedStatement statement = connection().prepareStatement(databaseService.upsertMemberFlagSql())) {
            statement.setString(1, regionId);
            statement.setString(2, playerId.toString());
            statement.setString(3, flag.name());
            statement.setInt(4, enabled ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save region member flag.", exception);
        }
    }

    private void deleteMemberFlags(String regionId, UUID playerId) {
        String sql = "DELETE FROM region_member_flags WHERE region_id = ? AND player_id = ?";

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            statement.setString(2, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete region member flags.", exception);
        }
    }

    private Connection connection() {
        return databaseService.getConnection();
    }
}
