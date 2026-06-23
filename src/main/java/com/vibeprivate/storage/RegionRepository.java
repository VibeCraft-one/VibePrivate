package com.vibeprivate.storage;

import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionShape;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RegionRepository {
    private final DatabaseService databaseService;

    public RegionRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    public List<Region> loadAll() {
        String sql = """
                SELECT id, name, type, owner_id, world, shape, center_x, center_z, radius,
                       min_y, max_y, pos1_x, pos1_y, pos1_z, pos2_x, pos2_y, pos2_z,
                       enabled, created_at, fuel_expires_at, fuel_empty_since,
                       last_fuel_drain_at, upgrade_level, visualization_mode
                FROM regions
                """;
        List<Region> regions = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                regions.add(mapRegion(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load regions.", exception);
        }

        return regions;
    }

    public void save(Region region) {
        Objects.requireNonNull(region, "region");
        String sql = databaseService.upsertRegionSql();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            bindRegion(statement, region);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save region " + region.getId() + ".", exception);
        }
    }

    public void delete(String regionId) {
        Objects.requireNonNull(regionId, "regionId");

        try (PreparedStatement statement = connection().prepareStatement("DELETE FROM regions WHERE id = ?")) {
            statement.setString(1, regionId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete region " + regionId + ".", exception);
        }
    }

    private Region mapRegion(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        String name = resultSet.getString("name");
        RegionType type = RegionType.valueOf(resultSet.getString("type"));
        String ownerId = resultSet.getString("owner_id");
        String worldName = resultSet.getString("world");
        RegionShape shape = RegionShape.valueOf(resultSet.getString("shape"));

        Region.Builder builder = shape == RegionShape.RADIUS
                ? Region.radiusRegion(id, name, type, ownerId, worldName).radius(
                resultSet.getInt("center_x"),
                resultSet.getInt("center_z"),
                resultSet.getInt("radius"),
                resultSet.getInt("min_y"),
                resultSet.getInt("max_y")
        )
                : Region.adminRegion(id, name, ownerId, worldName).cuboid(
                resultSet.getInt("pos1_x"),
                resultSet.getInt("pos1_y"),
                resultSet.getInt("pos1_z"),
                resultSet.getInt("pos2_x"),
                resultSet.getInt("pos2_y"),
                resultSet.getInt("pos2_z")
        );

        return builder.state(
                resultSet.getInt("enabled") == 1,
                resultSet.getLong("fuel_expires_at"),
                resultSet.getLong("fuel_empty_since"),
                resultSet.getLong("last_fuel_drain_at"),
                resultSet.getInt("upgrade_level"),
                VisualizationMode.valueOf(resultSet.getString("visualization_mode")),
                resultSet.getLong("created_at")
        ).build();
    }

    private void bindRegion(PreparedStatement statement, Region region) throws SQLException {
        statement.setString(1, region.getId());
        statement.setString(2, region.getName());
        statement.setString(3, region.getType().name());
        statement.setString(4, region.getOwnerId());
        statement.setString(5, region.getWorldName());
        statement.setString(6, region.getShape().name());
        setNullableInt(statement, 7, region.getCenterX());
        setNullableInt(statement, 8, region.getCenterZ());
        setNullableInt(statement, 9, region.getRadius());
        statement.setInt(10, region.getMinY());
        statement.setInt(11, region.getMaxY());
        setNullableInt(statement, 12, region.getPos1X());
        setNullableInt(statement, 13, region.getPos1Y());
        setNullableInt(statement, 14, region.getPos1Z());
        setNullableInt(statement, 15, region.getPos2X());
        setNullableInt(statement, 16, region.getPos2Y());
        setNullableInt(statement, 17, region.getPos2Z());
        statement.setInt(18, region.isEnabled() ? 1 : 0);
        statement.setLong(19, region.getCreatedAt());
        statement.setLong(20, region.getFuelExpiresAt());
        statement.setLong(21, region.getFuelEmptySince());
        statement.setLong(22, region.getLastFuelDrainAt());
        statement.setInt(23, region.getUpgradeLevel());
        statement.setString(24, region.getVisualizationMode().name());
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private Connection connection() {
        return databaseService.getConnection();
    }
}
