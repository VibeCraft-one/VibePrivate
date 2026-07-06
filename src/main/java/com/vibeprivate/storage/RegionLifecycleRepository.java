package com.vibeprivate.storage;

import com.vibeprivate.model.RegionLifecycleState;
import com.vibeprivate.model.RegionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegionLifecycleRepository {
    private final DatabaseService databaseService;

    public RegionLifecycleRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    protected RegionLifecycleRepository() {
        this.databaseService = null;
    }

    public Map<String, RegionLifecycleState> loadAll() {
        Map<String, RegionLifecycleState> states = new HashMap<>();
        String sql = """
                SELECT region_id, status, status_reason, status_changed_at, upkeep_paused_until, upkeep_pause_reason
                FROM region_lifecycle
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                RegionLifecycleState state = mapState(resultSet);
                states.put(state.getRegionId(), state);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load region lifecycle states.", exception);
        }

        return states;
    }

    public void save(RegionLifecycleState state) {
        Objects.requireNonNull(state, "state");
        String sql = databaseService.upsertRegionLifecycleSql();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, state.getRegionId());
            statement.setString(2, state.getStatus().name());
            statement.setString(3, state.getStatusReason());
            statement.setLong(4, state.getStatusChangedAt());
            if (state.getUpkeepPausedUntil() == null) {
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(5, state.getUpkeepPausedUntil());
            }
            statement.setString(6, state.getUpkeepPauseReason());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save lifecycle state for " + state.getRegionId() + ".", exception);
        }
    }

    public void delete(String regionId) {
        Objects.requireNonNull(regionId, "regionId");

        try (PreparedStatement statement = connection().prepareStatement("DELETE FROM region_lifecycle WHERE region_id = ?")) {
            statement.setString(1, regionId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete lifecycle state for " + regionId + ".", exception);
        }
    }

    private RegionLifecycleState mapState(ResultSet resultSet) throws SQLException {
        long pausedUntil = resultSet.getLong("upkeep_paused_until");
        Long upkeepPausedUntil = resultSet.wasNull() ? null : pausedUntil;
        return new RegionLifecycleState(
                resultSet.getString("region_id"),
                RegionStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("status_reason"),
                resultSet.getLong("status_changed_at"),
                upkeepPausedUntil,
                resultSet.getString("upkeep_pause_reason")
        );
    }

    private Connection connection() {
        if (databaseService == null) {
            throw new IllegalStateException("Database service is not configured.");
        }
        return databaseService.getConnection();
    }
}
