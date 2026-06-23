package com.vibeprivate.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.vibeprivate.model.RegionType;

import java.util.List;
import java.util.Objects;

public final class ConfigService {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigService(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getLanguage() {
        return config.getString("language", "ru").toLowerCase();
    }

    public String getDatabaseFile() {
        return config.getString("database.file", "vibeprivate.db");
    }

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite").toLowerCase();
    }

    public String getMySqlHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMySqlPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMySqlDatabase() {
        return config.getString("database.mysql.database", "vibeprivate");
    }

    public String getMySqlUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMySqlPassword() {
        return config.getString("database.mysql.password", "");
    }

    public String getMySqlParameters() {
        return config.getString("database.mysql.parameters",
                "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8");
    }

    public List<String> getAllowedWorlds() {
        return config.getStringList("regions.allowed-worlds");
    }

    public int getRegionMinY() {
        return config.getInt("regions.min-y", -64);
    }

    public int getRegionMaxY() {
        return config.getInt("regions.max-y", 500);
    }

    public int getHomeStartRadius() {
        return config.getInt("limits.home.start-radius", 32);
    }

    public int getHomeMaxPerPlayer() {
        return config.getInt("limits.home.max-per-player", 1);
    }

    public int getHomeMaxRadius() {
        return config.getInt("limits.home.max-radius", 512);
    }

    public int getFarmStartRadius() {
        return config.getInt("limits.farm.start-radius", 16);
    }

    public int getFarmMaxPerPlayer() {
        return config.getInt("limits.farm.max-per-player", 3);
    }

    public int getHomeMaxPerPlayer(Player player) {
        Objects.requireNonNull(player, "player");
        return getHomeMaxPerPlayer() + getExtraRegions(player, "home");
    }

    public int getFarmMaxPerPlayer(Player player) {
        Objects.requireNonNull(player, "player");
        return getFarmMaxPerPlayer() + getExtraRegions(player, "farm");
    }

    public int getFarmMaxRadius() {
        return config.getInt("limits.farm.max-radius", 126);
    }

    public int getClanStartRadius() {
        return config.getInt("limits.clan.start-radius", 128);
    }

    public int getClanMaxPerClan() {
        return config.getInt("limits.clan.max-per-clan", 1);
    }

    public int getClanMaxRadius() {
        return config.getInt("limits.clan.max-radius", 768);
    }

    public int getHomeTeleportDelaySeconds(Player player) {
        Objects.requireNonNull(player, "player");
        if (hasBonus(player, "fast-home")) {
            return Math.max(1, config.getInt("player-bonuses.fast-home.home-teleport-delay-seconds", 3));
        }

        return Math.max(1, config.getInt("teleport.home-delay-seconds", 5));
    }

    public int getHomeCommandCooldownSeconds(Player player) {
        Objects.requireNonNull(player, "player");
        if (hasBonus(player, "fast-home")) {
            return Math.max(0, config.getInt("player-bonuses.fast-home.home-command-cooldown-seconds", 3));
        }

        return Math.max(0, config.getInt("teleport.home-command-cooldown-seconds", 7));
    }

    public int getFuelDrainIntervalHours() {
        return Math.max(1, config.getInt("fuel.drain-interval-hours", 1));
    }

    public int getFuelMaxDays() {
        return Math.max(1, config.getInt("fuel.max-days", 60));
    }

    public int getExpiredDeleteAfterHours() {
        return Math.max(1, config.getInt("fuel.expired-delete-after-hours", 24));
    }

    public double getFuelRadiusCostMaxMultiplier() {
        return Math.max(1.0D, config.getDouble("fuel.radius-cost-max-multiplier", 10.0D));
    }

    public int getFuelMinutes(Material material) {
        Objects.requireNonNull(material, "material");
        String name = material.name();
        int exactValue = config.getInt("fuel.material-minutes." + name, -1);
        if (exactValue >= 0) {
            return exactValue;
        }

        if (name.endsWith("_LOG") || name.endsWith("_WOOD")) {
            return config.getInt("fuel.material-minutes.LOG", 0);
        }

        if (name.endsWith("_STEM") || name.endsWith("_HYPHAE")) {
            return config.getInt("fuel.material-minutes.STEM", 0);
        }

        return 0;
    }

    public int getVisualizationBorderDistanceBlocks() {
        return Math.max(1, config.getInt("visualization.border-distance-blocks", 2));
    }

    public int getVisualizationDurationSeconds() {
        return Math.max(1, config.getInt("visualization.duration-seconds", 4));
    }

    public int getVisualizationCooldownSeconds() {
        return Math.max(1, config.getInt("visualization.cooldown-seconds", 3));
    }

    public int getVisualizationWallRadiusBlocks() {
        return Math.max(4, config.getInt("visualization.wall-radius-blocks", 12));
    }

    public int getVisualizationWallHeightBlocks() {
        return Math.max(2, config.getInt("visualization.wall-height-blocks", 5));
    }

    public double getUpgradeCostMultiplier() {
        return Math.max(0.1D, config.getDouble("upgrades.cost-multiplier", 1.0D));
    }

    public int getUpgradeDepositRadiusPoints(Material material) {
        Objects.requireNonNull(material, "material");
        return Math.max(0, config.getInt("upgrades.deposit-radius-points." + material.name(), 0));
    }

    public boolean isChunkKeeperEnabled() {
        return config.getBoolean("chunk-keeper.enabled", true);
    }

    public int getChunkKeeperBufferChunks() {
        return Math.max(0, config.getInt("chunk-keeper.buffer-chunks", 4));
    }

    public int getChunkKeeperMaxChunksPerRegion() {
        return Math.max(1, config.getInt("chunk-keeper.max-chunks-per-region", 50000));
    }

    public int getStartRadius(RegionType type) {
        Objects.requireNonNull(type, "type");
        return switch (type) {
            case HOME -> getHomeStartRadius();
            case FARM -> getFarmStartRadius();
            case CLAN -> getClanStartRadius();
            case ADMIN -> 0;
        };
    }

    public int getMaxRadius(RegionType type) {
        Objects.requireNonNull(type, "type");
        return switch (type) {
            case HOME -> getHomeMaxRadius();
            case FARM -> getFarmMaxRadius();
            case CLAN -> getClanMaxRadius();
            case ADMIN -> 0;
        };
    }

    private int getExtraRegions(Player player, String type) {
        if (!hasBonus(player, "extra-regions")) {
            return 0;
        }

        return Math.max(0, config.getInt("player-bonuses.extra-regions.extra-" + type + "-regions", 0));
    }

    private boolean hasBonus(Player player, String key) {
        String permission = config.getString("player-bonuses." + key + ".permission", "");
        return permission != null && !permission.isBlank() && player.hasPermission(permission);
    }
}
