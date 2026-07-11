package com.vibeprivate.model;

import java.util.Objects;

public final class SelectionBounds {
    private final String worldName;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;

    public SelectionBounds(String worldName, int pos1X, int pos2X, int pos1Y, int pos2Y, int pos1Z, int pos2Z) {
        this.worldName = requireText(worldName, "worldName");
        this.minX = Math.min(pos1X, pos2X);
        this.maxX = Math.max(pos1X, pos2X);
        this.minY = Math.min(pos1Y, pos2Y);
        this.maxY = Math.max(pos1Y, pos2Y);
        this.minZ = Math.min(pos1Z, pos2Z);
        this.maxZ = Math.max(pos1Z, pos2Z);
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

    public long getBlockVolume() {
        long width = (long) maxX - minX + 1L;
        long height = (long) maxY - minY + 1L;
        long depth = (long) maxZ - minZ + 1L;
        return width * height * depth;
    }

    public boolean isEmpty() {
        return getBlockVolume() <= 0L;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }

        return value;
    }
}
