package com.vibeprivate.service;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class FuelService {
    private static final long MINUTE_MILLIS = 60_000L;
    private static final long HOUR_MILLIS = 60L * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24L * HOUR_MILLIS;

    private final JavaPlugin plugin;
    private final RegionManager regionManager;
    private final ConfigService configService;
    private BukkitTask task;

    public FuelService(JavaPlugin plugin, RegionManager regionManager, ConfigService configService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.configService = Objects.requireNonNull(configService, "configService");
    }

    public void start() {
        stop();
        if (configService.getUpkeepMode() == UpkeepMode.MONEY) {
            return;
        }

        long periodTicks = Math.max(1L, configService.getFuelDrainIntervalHours()) * 60L * 60L * 20L;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::runMaintenance, 20L * 60L, periodTicks);
        runMaintenance();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public FuelAddResult addFuelFromMainHand(Player player, Region region) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR || item.getAmount() <= 0) {
            return FuelAddResult.fail(FuelAddStatus.INVALID_ITEM, getRemainingMillis(region));
        }

        int minutesPerItem = configService.getFuelMinutes(item.getType());
        if (minutesPerItem <= 0) {
            return FuelAddResult.fail(FuelAddStatus.INVALID_ITEM, getRemainingMillis(region));
        }

        long now = System.currentTimeMillis();
        long base = Math.max(now, region.getFuelExpiresAt());
        long maxExpiresAt = now + getMaxFuelMillis();
        if (base >= maxExpiresAt) {
            return FuelAddResult.fail(FuelAddStatus.MAX_REACHED, getRemainingMillis(region));
        }

        long effectiveMillisPerItem = Math.max(MINUTE_MILLIS,
                Math.round(minutesPerItem * MINUTE_MILLIS / getFuelCostMultiplier(region)));
        long millisToCap = Math.max(MINUTE_MILLIS, maxExpiresAt - base);
        int consumed = (int) Math.min(item.getAmount(),
                Math.max(1L, (millisToCap + effectiveMillisPerItem - 1L) / effectiveMillisPerItem));
        long addedMillis = consumed * effectiveMillisPerItem;
        long newExpiresAt = Math.min(maxExpiresAt, base + addedMillis);

        ItemStack originalItem = item.clone();
        boolean wasEnabled = region.isEnabled();
        long oldFuelExpiresAt = region.getFuelExpiresAt();
        long oldFuelEmptySince = region.getFuelEmptySince();
        long oldLastFuelDrainAt = region.getLastFuelDrainAt();

        try {
            setMainHandAfterConsuming(player, originalItem, consumed);
        } catch (RuntimeException exception) {
            return FuelAddResult.fail(FuelAddStatus.FAILED, getRemainingMillis(region));
        }

        try {
            region.setFuelExpiresAt(newExpiresAt);
            region.setFuelEmptySince(0);
            region.setLastFuelDrainAt(now);
            region.setEnabled(true);
            regionManager.saveRegion(region);
        } catch (RuntimeException exception) {
            region.setFuelExpiresAt(oldFuelExpiresAt);
            region.setFuelEmptySince(oldFuelEmptySince);
            region.setLastFuelDrainAt(oldLastFuelDrainAt);
            region.setEnabled(wasEnabled);
            restoreMainHand(player, originalItem);
            return FuelAddResult.fail(FuelAddStatus.FAILED, getRemainingMillis(region));
        }

        return FuelAddResult.success(consumed, Math.max(1L, (newExpiresAt - base) / MINUTE_MILLIS),
                getRemainingMillis(region));
    }

    public long getRemainingMillis(Region region) {
        Objects.requireNonNull(region, "region");
        return Math.max(0L, region.getFuelExpiresAt() - System.currentTimeMillis());
    }

    public String formatRemaining(Region region) {
        return formatMillis(getRemainingMillis(region));
    }

    public String formatMillis(long millis) {
        if (millis <= 0) {
            return "0h";
        }

        long days = millis / DAY_MILLIS;
        long hours = (millis % DAY_MILLIS) / HOUR_MILLIS;
        if (days > 0) {
            return days + "d " + hours + "h";
        }

        long minutes = (millis % HOUR_MILLIS) / MINUTE_MILLIS;
        return hours + "h " + minutes + "m";
    }

    public double getFuelCostMultiplier(Region region) {
        Objects.requireNonNull(region, "region");
        if (region.getRadius() == null || region.isAdmin()) {
            return 1.0D;
        }

        int startRadius = configService.getStartRadius(region.getType());
        int maxRadius = configService.getMaxRadius(region.getType());
        if (maxRadius <= startRadius) {
            return 1.0D;
        }

        double progress = (region.getRadius() - startRadius) / (double) (maxRadius - startRadius);
        double clampedProgress = Math.max(0.0D, Math.min(1.0D, progress));
        double maxMultiplier = configService.getFuelRadiusCostMaxMultiplier();
        return 1.0D + clampedProgress * (maxMultiplier - 1.0D);
    }

    private void runMaintenance() {
        if (configService.getUpkeepMode() == UpkeepMode.MONEY) {
            return;
        }

        long now = System.currentTimeMillis();
        for (Region region : regionManager.getPlayerRegions()) {
            if (region.isEnabled() && region.getFuelExpiresAt() <= now) {
                region.setEnabled(false);
                region.setFuelEmptySince(now);
                region.setLastFuelDrainAt(now);
                regionManager.saveRegion(region);
            }
        }
    }

    private long getMaxFuelMillis() {
        return configService.getFuelMaxDays() * DAY_MILLIS;
    }

    private void setMainHandAfterConsuming(Player player, ItemStack originalItem, int consumed) {
        int remainingAmount = originalItem.getAmount() - consumed;
        if (remainingAmount <= 0) {
            player.getInventory().setItemInMainHand(null);
            return;
        }

        ItemStack remaining = originalItem.clone();
        remaining.setAmount(remainingAmount);
        player.getInventory().setItemInMainHand(remaining);
    }

    private void restoreMainHand(Player player, ItemStack originalItem) {
        try {
            player.getInventory().setItemInMainHand(originalItem.clone());
        } catch (RuntimeException ignored) {
            // Best-effort rollback after a failed persistent save.
        }
    }
}
