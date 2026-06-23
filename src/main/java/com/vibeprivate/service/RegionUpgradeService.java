package com.vibeprivate.service;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionShape;
import com.vibeprivate.storage.RegionDepositRepository;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RegionUpgradeService {
    private final RegionManager regionManager;
    private final ConfigService configService;
    private final RegionDepositRepository depositRepository;
    private Map<String, Map<Material, Integer>> depositsByRegion = new HashMap<>();

    public RegionUpgradeService(RegionManager regionManager, ConfigService configService,
                                RegionDepositRepository depositRepository) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.depositRepository = Objects.requireNonNull(depositRepository, "depositRepository");
    }

    public void load() {
        depositsByRegion = depositRepository.loadAll();
    }

    public UpgradeResult depositFromMainHand(Player player, Region region) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");

        int currentRadius = region.getRadius() == null ? 0 : region.getRadius();
        if (region.getShape() != RegionShape.RADIUS || region.isAdmin()) {
            return UpgradeResult.fail(UpgradeStatus.UNSUPPORTED_REGION, currentRadius);
        }

        int maxRadius = configService.getMaxRadius(region.getType());
        if (currentRadius >= maxRadius) {
            return UpgradeResult.fail(UpgradeStatus.MAX_REACHED, currentRadius);
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR || item.getAmount() <= 0
                || configService.getUpgradeDepositRadiusPoints(item.getType()) <= 0) {
            return UpgradeResult.fail(UpgradeStatus.INVALID_ITEM, currentRadius);
        }

        Material material = item.getType();
        int amount = item.getAmount();
        int existingAmount = getDepositAmount(region.getId(), material);
        int targetRadius = calculateRadius(region, material, existingAmount + amount);

        try {
            if (targetRadius > currentRadius) {
                Region upgraded = region.withRadius(targetRadius);
                upgraded.setUpgradeLevel(Math.max(0, targetRadius - configService.getStartRadius(region.getType())));
                regionManager.replaceRegion(upgraded);
            }

            int newAmount = existingAmount + amount;
            depositsByRegion.computeIfAbsent(region.getId(), ignored -> new EnumMap<>(Material.class)).put(material, newAmount);
            depositRepository.saveDeposit(region.getId(), material, newAmount);
            player.getInventory().setItemInMainHand(null);
            return UpgradeResult.success(amount, currentRadius, targetRadius);
        } catch (IllegalArgumentException exception) {
            return UpgradeResult.fail(UpgradeStatus.OVERLAP, currentRadius);
        } catch (RuntimeException exception) {
            return UpgradeResult.fail(UpgradeStatus.FAILED, currentRadius);
        }
    }

    public int getDepositPoints(Region region) {
        Objects.requireNonNull(region, "region");
        return depositsByRegion.getOrDefault(region.getId(), Map.of()).entrySet().stream()
                .mapToInt(entry -> configService.getUpgradeDepositRadiusPoints(entry.getKey()) * entry.getValue())
                .sum();
    }

    public int calculateAllowedRadius(Region region) {
        Objects.requireNonNull(region, "region");
        int startRadius = configService.getStartRadius(region.getType());
        int maxRadius = configService.getMaxRadius(region.getType());
        int bonus = (int) Math.floor(getDepositPoints(region) / configService.getUpgradeCostMultiplier());
        return Math.min(maxRadius, startRadius + bonus);
    }

    public Map<Material, Integer> getDeposits(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        return Map.copyOf(depositsByRegion.getOrDefault(regionId, Map.of()));
    }

    public void clearDeposits(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        depositsByRegion.remove(regionId);
        depositRepository.deleteAll(regionId);
    }

    public WithdrawDepositResult withdrawExcessDeposit(Player player, Region region) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");

        try {
            int totalPoints = getDepositPoints(region);
            int currentRadius = region.getRadius() == null ? 0 : region.getRadius();
            int startRadius = configService.getStartRadius(region.getType());
            int requiredPoints = (int) Math.ceil(Math.max(0, currentRadius - startRadius) * configService.getUpgradeCostMultiplier());
            int excessPoints = totalPoints - requiredPoints;
            if (excessPoints <= 0) {
                return WithdrawDepositResult.fail(WithdrawDepositStatus.NOTHING_TO_RETURN);
            }

            Map<Material, Integer> deposits = depositsByRegion.get(region.getId());
            if (deposits == null || deposits.isEmpty()) {
                return WithdrawDepositResult.fail(WithdrawDepositStatus.NOTHING_TO_RETURN);
            }

            int returnedAmount = 0;
            for (Material material : Material.values()) {
                int points = configService.getUpgradeDepositRadiusPoints(material);
                int amount = deposits.getOrDefault(material, 0);
                while (amount > 0 && points > 0 && excessPoints >= points) {
                    amount--;
                    excessPoints -= points;
                    returnedAmount++;
                    giveOrDrop(player, new ItemStack(material, 1));
                }

                if (amount <= 0) {
                    deposits.remove(material);
                    depositRepository.deleteDeposit(region.getId(), material);
                } else {
                    deposits.put(material, amount);
                    depositRepository.saveDeposit(region.getId(), material, amount);
                }
            }

            return returnedAmount > 0
                    ? WithdrawDepositResult.success(returnedAmount)
                    : WithdrawDepositResult.fail(WithdrawDepositStatus.NOTHING_TO_RETURN);
        } catch (RuntimeException exception) {
            return WithdrawDepositResult.fail(WithdrawDepositStatus.FAILED);
        }
    }

    private int calculateRadius(Region region, Material addedMaterial, int addedMaterialAmount) {
        int existingPoints = getDepositPoints(region);
        int existingAmount = getDepositAmount(region.getId(), addedMaterial);
        int addedPoints = configService.getUpgradeDepositRadiusPoints(addedMaterial) * (addedMaterialAmount - existingAmount);
        int startRadius = configService.getStartRadius(region.getType());
        int maxRadius = configService.getMaxRadius(region.getType());
        int bonus = (int) Math.floor((existingPoints + addedPoints) / configService.getUpgradeCostMultiplier());
        return Math.min(maxRadius, startRadius + bonus);
    }

    private int getDepositAmount(String regionId, Material material) {
        return depositsByRegion.getOrDefault(regionId, Map.of()).getOrDefault(material, 0);
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        overflow.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }
}
