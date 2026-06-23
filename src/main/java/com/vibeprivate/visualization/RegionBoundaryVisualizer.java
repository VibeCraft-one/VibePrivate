package com.vibeprivate.visualization;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionBounds;
import com.vibeprivate.model.VisualizationMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RegionBoundaryVisualizer {
    private static final long DRAW_PERIOD_TICKS = 8L;

    private final Plugin plugin;
    private final ConfigService configService;
    private final Map<UUID, Long> cooldownsByPlayer = new HashMap<>();
    private final Map<UUID, BoundaryEffect> activeEffectsByPlayer = new HashMap<>();
    private BukkitTask drawTask;

    public RegionBoundaryVisualizer(Plugin plugin, ConfigService configService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configService = Objects.requireNonNull(configService, "configService");
    }

    public void showIfNearBoundary(Player player, Region region, Location location) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(location, "location");

        if (!region.isEnabled() || region.getVisualizationMode() == VisualizationMode.OFF) {
            return;
        }

        World world = location.getWorld();
        if (world == null || !world.getName().equals(region.getWorldName())) {
            return;
        }

        RegionBounds bounds = region.getBounds();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int distance = configService.getVisualizationBorderDistanceBlocks();
        boolean nearX = x - bounds.getMinX() <= distance || bounds.getMaxX() - x <= distance;
        boolean nearZ = z - bounds.getMinZ() <= distance || bounds.getMaxZ() - z <= distance;
        if (!nearX && !nearZ) {
            return;
        }

        long now = System.currentTimeMillis();
        long nextAllowedAt = cooldownsByPlayer.getOrDefault(player.getUniqueId(), 0L);
        if (nextAllowedAt > now) {
            return;
        }

        BoundaryEffect effect = createEffect(player, bounds, x, location.getBlockY(), z, nearX, nearZ, now);
        cooldownsByPlayer.put(player.getUniqueId(), now + effectiveCooldownMillis());
        activeEffectsByPlayer.put(player.getUniqueId(), effect);
        draw(effect);
        ensureDrawTask();
    }

    public void clear(Player player) {
        Objects.requireNonNull(player, "player");
        cooldownsByPlayer.remove(player.getUniqueId());
        activeEffectsByPlayer.remove(player.getUniqueId());
        stopDrawTaskIfIdle();
    }

    private BoundaryEffect createEffect(Player player, RegionBounds bounds, int playerX, int playerY, int playerZ,
                                        boolean nearX, boolean nearZ, long now) {
        int wallRadius = configService.getVisualizationWallRadiusBlocks();
        int wallHeight = configService.getVisualizationWallHeightBlocks();
        long expiresAt = now + configService.getVisualizationDurationSeconds() * 1000L;
        BoundaryEffect effect = new BoundaryEffect(player, expiresAt, playerY, wallHeight);

        if (nearX) {
            int boundaryX = nearest(playerX, bounds.getMinX(), bounds.getMaxX());
            effect.addZWall(boundaryX, Math.max(bounds.getMinZ(), playerZ - wallRadius),
                    Math.min(bounds.getMaxZ(), playerZ + wallRadius));
        }

        if (nearZ) {
            int boundaryZ = nearest(playerZ, bounds.getMinZ(), bounds.getMaxZ());
            effect.addXWall(boundaryZ, Math.max(bounds.getMinX(), playerX - wallRadius),
                    Math.min(bounds.getMaxX(), playerX + wallRadius));
        }

        return effect;
    }

    private long effectiveCooldownMillis() {
        int configuredSeconds = configService.getVisualizationCooldownSeconds();
        int durationSeconds = configService.getVisualizationDurationSeconds();
        int seconds = Math.min(configuredSeconds, Math.max(1, durationSeconds));
        return seconds * 1000L;
    }

    private void ensureDrawTask() {
        if (drawTask != null) {
            return;
        }

        drawTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, DRAW_PERIOD_TICKS, DRAW_PERIOD_TICKS);
    }

    private void tick() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, BoundaryEffect>> iterator = activeEffectsByPlayer.entrySet().iterator();
        while (iterator.hasNext()) {
            BoundaryEffect effect = iterator.next().getValue();
            if (!effect.player().isOnline() || effect.expiresAt() <= now) {
                iterator.remove();
                continue;
            }

            draw(effect);
        }

        stopDrawTaskIfIdle();
    }

    private void stopDrawTaskIfIdle() {
        if (!activeEffectsByPlayer.isEmpty() || drawTask == null) {
            return;
        }

        drawTask.cancel();
        drawTask = null;
    }

    private void draw(BoundaryEffect effect) {
        for (WallSegment segment : effect.segments()) {
            drawWall(effect.player(), segment, effect.centerY(), effect.height());
        }
    }

    private void drawWall(Player player, WallSegment segment, int centerY, int height) {
        int minY = centerY - 1;
        int maxY = centerY + height;
        for (int y = minY; y <= maxY; y++) {
            if (segment.axis() == WallAxis.X) {
                drawXRow(player, segment.fixed(), segment.min(), segment.max(), y);
            } else {
                drawZRow(player, segment.fixed(), segment.min(), segment.max(), y);
            }
        }
    }

    private void drawXRow(Player player, int z, int minX, int maxX, int y) {
        for (int x = minX; x <= maxX; x += 2) {
            spawn(player, x, y, z);
        }
    }

    private void drawZRow(Player player, int x, int minZ, int maxZ, int y) {
        for (int z = minZ; z <= maxZ; z += 2) {
            spawn(player, x, y, z);
        }
    }

    private void spawn(Player player, int x, int y, int z) {
        player.spawnParticle(Particle.END_ROD, x + 0.5, y + 0.2, z + 0.5, 1, 0.03, 0.03, 0.03, 0.0);
    }

    private int nearest(int value, int min, int max) {
        return Math.abs(value - min) <= Math.abs(value - max) ? min : max;
    }
}
