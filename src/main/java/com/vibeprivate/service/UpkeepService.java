package com.vibeprivate.service;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.storage.UpkeepRepository;
import com.vibeprivate.storage.UpkeepState;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class UpkeepService {
    private static final long DAY_MILLIS = 86_400_000L;

    private final JavaPlugin plugin;
    private final RegionManager regionManager;
    private final ConfigService configService;
    private final MessageService messageService;
    private final EconomyService economyService;
    private final UpkeepRepository upkeepRepository;
    private final Map<String, UpkeepState> states = new HashMap<>();
    private BukkitTask task;

    public UpkeepService(JavaPlugin plugin, RegionManager regionManager, ConfigService configService,
                         MessageService messageService, EconomyService economyService,
                         UpkeepRepository upkeepRepository) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.economyService = Objects.requireNonNull(economyService, "economyService");
        this.upkeepRepository = Objects.requireNonNull(upkeepRepository, "upkeepRepository");
    }

    public void load() {
        states.clear();
        states.putAll(upkeepRepository.loadAll());
    }

    public void start() {
        stop();
        if (!configService.isUpkeepEnabled()) {
            return;
        }

        long periodTicks = Math.max(20L * 60L, configService.getUpkeepCheckIntervalMinutes() * 60L * 20L);
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::runMaintenance, 20L * 60L, periodTicks);
        runMaintenance();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public UpkeepMode getMode() {
        return configService.getUpkeepMode();
    }

    public double getDailyCost(String ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        List<Region> regions = playerRegions(ownerId);
        int chunks = regions.stream().mapToInt(this::countChunks).sum();
        if (chunks <= 0) {
            return 0.0D;
        }

        double base = configService.getUpkeepBaseCostPerChunk();
        double growth = configService.getUpkeepSizeGrowth();
        double power = configService.getUpkeepSizePower();
        double cost = base * chunks * (1.0D + growth * Math.pow(Math.max(0, chunks - 1), power));
        if (isOfflineReduced(ownerId)) {
            cost *= configService.getUpkeepOfflineRateMultiplier();
        }

        return Math.max(0.0D, cost);
    }

    public int getChunkCount(String ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        return playerRegions(ownerId).stream().mapToInt(this::countChunks).sum();
    }

    public int getDebtDays(String ownerId) {
        return state(ownerId).debtDays();
    }

    public boolean isProtectionActive(String ownerId) {
        return getDebtDays(ownerId) < configService.getUpkeepGraceDays();
    }

    public String formatCost(double cost) {
        return getMode() == UpkeepMode.MONEY ? economyService.format(cost) : String.format("%.2f", cost);
    }

    private void runMaintenance() {
        if (!configService.isUpkeepEnabled() || getMode() == UpkeepMode.FUEL) {
            cleanupStaleOwners();
            return;
        }

        economyService.hook();
        long now = System.currentTimeMillis();
        for (String ownerId : owners()) {
            UpkeepState current = state(ownerId);
            long dueEvery = configService.getUpkeepIntervalHours() * 60L * 60L * 1000L;
            if (current.lastChargedAt() > 0L && now - current.lastChargedAt() < dueEvery) {
                continue;
            }
            if (isNewbie(ownerId) || isFrozen(ownerId)) {
                saveState(new UpkeepState(ownerId, 0, now));
                enableOwnerRegions(ownerId);
                continue;
            }

            double cost = getDailyCost(ownerId);
            switch (pay(ownerId, cost)) {
                case PAID -> {
                    saveState(new UpkeepState(ownerId, 0, now));
                    enableOwnerRegions(ownerId);
                    notifyOwner(ownerId, "upkeep.paid", Map.of("cost", formatCost(cost)));
                    log(ownerId + " burned " + cost + " for private upkeep at " + Instant.now());
                }
                case INSUFFICIENT_FUNDS -> {
                    int debtDays = current.debtDays() + 1;
                    saveState(new UpkeepState(ownerId, debtDays, now));
                    if (debtDays >= configService.getUpkeepGraceDays()) {
                        disableOwnerRegions(ownerId);
                    }
                    notifyOwner(ownerId, "upkeep.failed", Map.of("days", Integer.toString(debtDays)));
                    if (debtDays >= configService.getUpkeepRemoveAfterDays()) {
                        removeOwnerRegions(ownerId);
                    }
                }
                case ECONOMY_UNAVAILABLE -> {
                    log("Skipped private upkeep for " + ownerId + ": economy provider is unavailable.");
                    notifyOwner(ownerId, "upkeep.economy-unavailable", Map.of());
                }
            }
        }

        cleanupStaleOwners();
    }

    private UpkeepPaymentStatus pay(String ownerId, double cost) {
        if (cost <= 0.0D) {
            return UpkeepPaymentStatus.PAID;
        }
        if (!economyService.isAvailable()) {
            return UpkeepPaymentStatus.ECONOMY_UNAVAILABLE;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(java.util.UUID.fromString(ownerId));
        if (economyService.getBalance(owner) + 0.0001D < cost) {
            return UpkeepPaymentStatus.INSUFFICIENT_FUNDS;
        }

        return economyService.withdraw(owner, cost)
                ? UpkeepPaymentStatus.PAID
                : UpkeepPaymentStatus.ECONOMY_UNAVAILABLE;
    }

    private void enableOwnerRegions(String ownerId) {
        for (Region region : playerRegions(ownerId)) {
            if (!region.isEnabled()) {
                region.setEnabled(true);
                region.setFuelEmptySince(0L);
                regionManager.saveRegion(region);
            }
        }
    }

    private void disableOwnerRegions(String ownerId) {
        for (Region region : playerRegions(ownerId)) {
            if (region.isEnabled()) {
                region.setEnabled(false);
                region.setFuelEmptySince(System.currentTimeMillis());
                regionManager.saveRegion(region);
            }
        }
    }

    private void removeOwnerRegions(String ownerId) {
        for (Region region : List.copyOf(playerRegions(ownerId))) {
            regionManager.removeRegion(region.getId());
        }
        states.remove(ownerId);
        upkeepRepository.delete(ownerId);
        log("Removed overdue private regions for " + ownerId + " at " + Instant.now());
    }

    private void cleanupStaleOwners() {
        Set<String> liveOwners = owners();
        for (String ownerId : List.copyOf(states.keySet())) {
            if (!liveOwners.contains(ownerId)) {
                states.remove(ownerId);
                upkeepRepository.delete(ownerId);
            }
        }
    }

    private boolean isNewbie(String ownerId) {
        Player player = Bukkit.getPlayer(java.util.UUID.fromString(ownerId));
        int minutes = configService.getUpkeepNewbieFreePlaytimeMinutes();
        return player != null && minutes > 0 && player.getStatistic(Statistic.PLAY_ONE_MINUTE) < minutes * 60 * 20;
    }

    private boolean isFrozen(String ownerId) {
        int days = configService.getUpkeepFreezeAfterOfflineDays();
        if (days <= 0) {
            return false;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(java.util.UUID.fromString(ownerId));
        long lastPlayed = player.getLastPlayed();
        return lastPlayed > 0L && System.currentTimeMillis() - lastPlayed >= days * DAY_MILLIS;
    }

    private boolean isOfflineReduced(String ownerId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(java.util.UUID.fromString(ownerId));
        long lastPlayed = player.getLastPlayed();
        return Bukkit.getPlayer(java.util.UUID.fromString(ownerId)) == null
                && lastPlayed > 0L
                && System.currentTimeMillis() - lastPlayed >= DAY_MILLIS;
    }

    private int countChunks(Region region) {
        int width = region.getBounds().getMaxChunkX() - region.getBounds().getMinChunkX() + 1;
        int depth = region.getBounds().getMaxChunkZ() - region.getBounds().getMinChunkZ() + 1;
        return Math.max(1, width * depth);
    }

    private List<Region> playerRegions(String ownerId) {
        return regionManager.getRegionsByOwner(ownerId).stream()
                .filter(region -> !region.isAdmin())
                .toList();
    }

    private Set<String> owners() {
        return regionManager.getRegions().stream()
                .filter(region -> !region.isAdmin())
                .map(Region::getOwnerId)
                .collect(Collectors.toSet());
    }

    private UpkeepState state(String ownerId) {
        return states.getOrDefault(ownerId, UpkeepState.empty(ownerId));
    }

    private void saveState(UpkeepState state) {
        states.put(state.ownerId(), state);
        upkeepRepository.save(state);
    }

    private void notifyOwner(String ownerId, String key, Map<String, String> placeholders) {
        Player player = Bukkit.getPlayer(java.util.UUID.fromString(ownerId));
        if (player != null) {
            messageService.send(player, key, placeholders);
        }
    }

    private void log(String message) {
        if (configService.isUpkeepLoggingEnabled()) {
            plugin.getLogger().info(message);
        }
    }
}
