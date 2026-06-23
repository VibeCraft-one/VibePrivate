package com.vibeprivate.storage;

import org.bukkit.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RegionDepositRepository {
    private final DatabaseService databaseService;

    public RegionDepositRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    public Map<String, Map<Material, Integer>> loadAll() {
        Map<String, Map<Material, Integer>> deposits = new HashMap<>();
        String sql = "SELECT region_id, material, amount FROM region_deposits";

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String regionId = resultSet.getString("region_id");
                Material material = Material.matchMaterial(resultSet.getString("material"));
                int amount = resultSet.getInt("amount");
                if (material == null || amount <= 0) {
                    continue;
                }

                deposits.computeIfAbsent(regionId, ignored -> new EnumMap<>(Material.class)).put(material, amount);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load region deposits.", exception);
        }

        return deposits;
    }

    public void saveDeposit(String regionId, Material material, int amount) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(material, "material");

        String sql = databaseService.upsertDepositSql();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            statement.setString(2, material.name());
            statement.setInt(3, Math.max(0, amount));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save region deposit.", exception);
        }
    }

    public void deleteDeposit(String regionId, Material material) {
        Objects.requireNonNull(regionId, "regionId");
        Objects.requireNonNull(material, "material");

        String sql = "DELETE FROM region_deposits WHERE region_id = ? AND material = ?";

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            statement.setString(2, material.name());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete region deposit.", exception);
        }
    }

    public void deleteAll(String regionId) {
        Objects.requireNonNull(regionId, "regionId");

        try (PreparedStatement statement = connection().prepareStatement("DELETE FROM region_deposits WHERE region_id = ?")) {
            statement.setString(1, regionId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete region deposits.", exception);
        }
    }

    private Connection connection() {
        return databaseService.getConnection();
    }
}
