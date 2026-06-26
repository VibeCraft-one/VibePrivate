package com.vibeprivate.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class UpkeepRepository {
    private final DatabaseService databaseService;

    public UpkeepRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    public Map<String, UpkeepState> loadAll() {
        Map<String, UpkeepState> states = new HashMap<>();
        String sql = "SELECT owner_id, debt_days, last_charged_at FROM owner_upkeep";

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String ownerId = resultSet.getString("owner_id");
                states.put(ownerId, new UpkeepState(
                        ownerId,
                        resultSet.getInt("debt_days"),
                        resultSet.getLong("last_charged_at")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load owner upkeep states.", exception);
        }

        return states;
    }

    public void save(UpkeepState state) {
        Objects.requireNonNull(state, "state");
        String sql = databaseService.upsertUpkeepSql();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, state.ownerId());
            statement.setInt(2, Math.max(0, state.debtDays()));
            statement.setLong(3, Math.max(0L, state.lastChargedAt()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save upkeep state for " + state.ownerId() + ".", exception);
        }
    }

    public void delete(String ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");

        try (PreparedStatement statement = connection().prepareStatement("DELETE FROM owner_upkeep WHERE owner_id = ?")) {
            statement.setString(1, ownerId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete upkeep state for " + ownerId + ".", exception);
        }
    }

    private Connection connection() {
        return databaseService.getConnection();
    }
}
