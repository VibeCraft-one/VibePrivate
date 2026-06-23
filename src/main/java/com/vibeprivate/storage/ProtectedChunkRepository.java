package com.vibeprivate.storage;

import com.vibeprivate.model.Region;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class ProtectedChunkRepository {
    private static final String SOURCE = "vibeprivate";

    private final DatabaseService databaseService;

    public ProtectedChunkRepository(DatabaseService databaseService) {
        this.databaseService = Objects.requireNonNull(databaseService, "databaseService");
    }

    public int protectRegionChunks(Region region, int bufferChunks, int maxChunksPerRegion) {
        Objects.requireNonNull(region, "region");
        if (region.isAdmin()) {
            return 0;
        }

        int minChunkX = region.getBounds().getMinChunkX() - bufferChunks;
        int maxChunkX = region.getBounds().getMaxChunkX() + bufferChunks;
        int minChunkZ = region.getBounds().getMinChunkZ() - bufferChunks;
        int maxChunkZ = region.getBounds().getMaxChunkZ() + bufferChunks;
        long chunkCount = (long) (maxChunkX - minChunkX + 1) * (long) (maxChunkZ - minChunkZ + 1);
        if (chunkCount > maxChunksPerRegion) {
            throw new IllegalArgumentException("Region " + region.getId() + " covers too many protected chunks: " + chunkCount);
        }

        long now = System.currentTimeMillis();
        String sql = databaseService.upsertProtectedChunkSql();

        Connection connection = databaseService.getConnection();
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            int protectedChunks = 0;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        statement.setString(1, region.getWorldName());
                        statement.setInt(2, chunkX);
                        statement.setInt(3, chunkZ);
                        statement.setString(4, region.getOwnerId());
                        statement.setString(5, region.getId());
                        statement.setString(6, region.getType().name());
                        statement.setString(7, SOURCE);
                        statement.setLong(8, now);
                        statement.setLong(9, now);
                        statement.addBatch();
                        protectedChunks++;
                    }
                }

                statement.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(oldAutoCommit);
            return protectedChunks;
        } catch (SQLException exception) {
            rollbackQuietly(connection);
            throw new IllegalStateException("Failed to save protected chunks for region " + region.getId() + ".", exception);
        } finally {
            restoreAutoCommitQuietly(connection, oldAutoCommit);
        }
    }

    public boolean hasProtectedChunks(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        String sql = "SELECT 1 FROM protected_chunks WHERE region_id = ? LIMIT 1";

        try (PreparedStatement statement = databaseService.getConnection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to check protected chunks for region " + regionId + ".", exception);
        }
    }

    public int deleteRegionChunks(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        String sql = "DELETE FROM protected_chunks WHERE region_id = ?";

        try (PreparedStatement statement = databaseService.getConnection().prepareStatement(sql)) {
            statement.setString(1, regionId);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to delete protected chunks for region " + regionId + ".", exception);
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // The original SQL exception is more useful to callers.
        }
    }

    private void restoreAutoCommitQuietly(Connection connection, boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException ignored) {
            // The connection will be closed by the plugin if it becomes unhealthy.
        }
    }
}
