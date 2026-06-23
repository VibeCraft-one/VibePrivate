package com.vibeprivate.visualization;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

final class BoundaryEffect {
    private final Player player;
    private final long expiresAt;
    private final int centerY;
    private final int height;
    private final List<WallSegment> segments = new ArrayList<>(2);

    BoundaryEffect(Player player, long expiresAt, int centerY, int height) {
        this.player = player;
        this.expiresAt = expiresAt;
        this.centerY = centerY;
        this.height = height;
    }

    Player player() {
        return player;
    }

    long expiresAt() {
        return expiresAt;
    }

    int centerY() {
        return centerY;
    }

    int height() {
        return height;
    }

    List<WallSegment> segments() {
        return segments;
    }

    void addXWall(int z, int minX, int maxX) {
        segments.add(new WallSegment(WallAxis.X, z, minX, maxX));
    }

    void addZWall(int x, int minZ, int maxZ) {
        segments.add(new WallSegment(WallAxis.Z, x, minZ, maxZ));
    }
}
