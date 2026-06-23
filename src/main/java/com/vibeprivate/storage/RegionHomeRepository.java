package com.vibeprivate.storage;

import com.vibeprivate.model.RegionHome;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public final class RegionHomeRepository {
    private final DatabaseService databaseService;

    public RegionHomeRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    public Optional<RegionHome> getHome(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        String sql = """
                SELECT region_id, world, x, y, z, yaw, pitch
                FROM region_homes
                WHERE region_id = ?
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new RegionHome(
                        resultSet.getString("region_id"),
                        resultSet.getString("world"),
                        resultSet.getDouble("x"),
                        resultSet.getDouble("y"),
                        resultSet.getDouble("z"),
                        resultSet.getFloat("yaw"),
                        resultSet.getFloat("pitch")
                ));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load region home.", exception);
        }
    }

    public void saveHome(RegionHome home) {
        Objects.requireNonNull(home, "home");

        try (PreparedStatement statement = connection().prepareStatement(databaseService.upsertRegionHomeSql())) {
            statement.setString(1, home.regionId());
            statement.setString(2, home.worldName());
            statement.setDouble(3, home.x());
            statement.setDouble(4, home.y());
            statement.setDouble(5, home.z());
            statement.setFloat(6, home.yaw());
            statement.setFloat(7, home.pitch());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save region home.", exception);
        }
    }

    public void deleteHome(String regionId) {
        Objects.requireNonNull(regionId, "regionId");

        try (PreparedStatement statement = connection().prepareStatement("DELETE FROM region_homes WHERE region_id = ?")) {
            statement.setString(1, regionId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete region home.", exception);
        }
    }

    private Connection connection() {
        return databaseService.getConnection();
    }
}
