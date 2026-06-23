package com.vibeprivate.model;

import java.util.Objects;

public final class RegionBounds {
    private final String worldName;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    public RegionBounds(String worldName, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.worldName = requireText(worldName, "worldName");
        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public static RegionBounds radius(String worldName, int centerX, int centerZ, int radius, int minY, int maxY) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius must not be negative.");
        }

        return new RegionBounds(
                worldName,
                centerX - radius,
                centerX + radius,
                minY,
                maxY,
                centerZ - radius,
                centerZ + radius
        );
    }

    public boolean contains(String worldName, int x, int y, int z) {
        return this.worldName.equals(worldName)
                && x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public boolean intersects(RegionBounds other) {
        Objects.requireNonNull(other, "other");
        return worldName.equals(other.worldName)
                && minX <= other.maxX && maxX >= other.minX
                && minY <= other.maxY && maxY >= other.minY
                && minZ <= other.maxZ && maxZ >= other.minZ;
    }

    public long getBlockVolume() {
        long width = (long) maxX - minX + 1L;
        long height = (long) maxY - minY + 1L;
        long depth = (long) maxZ - minZ + 1L;
        return width * height * depth;
    }

    public int getMinChunkX() {
        return Math.floorDiv(minX, 16);
    }

    public int getMaxChunkX() {
        return Math.floorDiv(maxX, 16);
    }

    public int getMinChunkZ() {
        return Math.floorDiv(minZ, 16);
    }

    public int getMaxChunkZ() {
        return Math.floorDiv(maxZ, 16);
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }

        return value;
    }
}
