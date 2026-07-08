package com.vibeprivate.service;

import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionShape;
import com.vibeprivate.storage.RegionDepositRepository;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public final class RegionUpgradeService {
    private final RegionManager regionManager;
    private final ConfigService configService;
    private final RegionDepositRepository depositRepository;
    private final BiFunction<Material, Integer, ItemStack> itemFactory;
    private final ToIntFunction<Material> maxStackSizeProvider;
    private Map<String, Map<Material, Integer>> depositsByRegion = new HashMap<>();

    public RegionUpgradeService(RegionManager regionManager, ConfigService configService,
                                RegionDepositRepository depositRepository) {
        this(regionManager, configService, depositRepository, ItemStack::new, Material::getMaxStackSize);
    }

    RegionUpgradeService(RegionManager regionManager, ConfigService configService,
                         RegionDepositRepository depositRepository,
                         BiFunction<Material, Integer, ItemStack> itemFactory,
                         ToIntFunction<Material> maxStackSizeProvider) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.depositRepository = Objects.requireNonNull(depositRepository, "depositRepository");
        this.itemFactory = Objects.requireNonNull(itemFactory, "itemFactory");
        this.maxStackSizeProvider = Objects.requireNonNull(maxStackSizeProvider, "maxStackSizeProvider");
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
        ItemStack originalItem = item.clone();
        boolean upgradedRegion = false;

        try {
            player.getInventory().setItemInMainHand(null);

            if (targetRadius > currentRadius) {
                Region upgraded = region.withRadius(targetRadius);
                upgraded.setUpgradeLevel(Math.max(0, targetRadius - configService.getStartRadius(region.getType())));
                regionManager.replaceRegion(upgraded);
                upgradedRegion = true;
            }

            int newAmount = existingAmount + amount;
            depositRepository.saveDeposit(region.getId(), material, newAmount);
            depositsByRegion.computeIfAbsent(region.getId(), ignored -> new EnumMap<>(Material.class)).put(material, newAmount);
            return UpgradeResult.success(amount, currentRadius, targetRadius);
        } catch (IllegalArgumentException exception) {
            restoreMainHand(player, originalItem);
            return UpgradeResult.fail(UpgradeStatus.OVERLAP, currentRadius);
        } catch (RuntimeException exception) {
            if (upgradedRegion && rollbackRegion(region, exception)) {
                restoreMainHand(player, originalItem);
            } else if (!upgradedRegion) {
                restoreMainHand(player, originalItem);
            }
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
        depositRepository.deleteAll(regionId);
        depositsByRegion.remove(regionId);
    }

    void forgetLoadedDeposits(String regionId) {
        Objects.requireNonNull(regionId, "regionId");
        depositsByRegion.remove(regionId);
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

            WithdrawalPlan plan = calculateWithdrawalPlan(deposits, excessPoints);
            if (plan.returnedAmount() <= 0) {
                return WithdrawDepositResult.fail(WithdrawDepositStatus.NOTHING_TO_RETURN);
            }

            persistDeposits(region.getId(), deposits, plan.remainingDeposits());
            if (plan.remainingDeposits().isEmpty()) {
                depositsByRegion.remove(region.getId());
            } else {
                depositsByRegion.put(region.getId(), copyDeposits(plan.remainingDeposits()));
            }

            for (ItemStack item : plan.itemsToReturn()) {
                giveOrDrop(player, item);
            }

            return WithdrawDepositResult.success(plan.returnedAmount());
        } catch (RuntimeException exception) {
            return WithdrawDepositResult.fail(WithdrawDepositStatus.FAILED);
        }
    }

    private WithdrawalPlan calculateWithdrawalPlan(Map<Material, Integer> deposits, int excessPoints) {
        Map<Material, Integer> remainingDeposits = copyDeposits(deposits);
        List<ItemStack> itemsToReturn = new ArrayList<>();
        int returnedAmount = 0;

        for (Material material : Material.values()) {
            int points = configService.getUpgradeDepositRadiusPoints(material);
            int amount = remainingDeposits.getOrDefault(material, 0);
            int materialReturned = 0;
            while (amount > 0 && points > 0 && excessPoints >= points) {
                amount--;
                excessPoints -= points;
                materialReturned++;
                returnedAmount++;
            }

            if (materialReturned <= 0) {
                continue;
            }

            if (amount <= 0) {
                remainingDeposits.remove(material);
            } else {
                remainingDeposits.put(material, amount);
            }

            int remainingItems = materialReturned;
            int maxStackSize = maxStackSizeProvider.applyAsInt(material);
            while (remainingItems > 0) {
                int stackAmount = Math.min(maxStackSize, remainingItems);
                itemsToReturn.add(itemFactory.apply(material, stackAmount));
                remainingItems -= stackAmount;
            }
        }

        return new WithdrawalPlan(remainingDeposits, itemsToReturn, returnedAmount);
    }

    private void persistDeposits(String regionId, Map<Material, Integer> oldDeposits,
                                 Map<Material, Integer> newDeposits) {
        List<DepositChange> appliedChanges = new ArrayList<>();
        try {
            for (Material material : oldDeposits.keySet()) {
                int oldAmount = oldDeposits.getOrDefault(material, 0);
                int newAmount = newDeposits.getOrDefault(material, 0);
                if (newAmount <= 0) {
                    depositRepository.deleteDeposit(regionId, material);
                    appliedChanges.add(new DepositChange(material, oldAmount));
                } else if (newAmount != oldAmount) {
                    depositRepository.saveDeposit(regionId, material, newAmount);
                    appliedChanges.add(new DepositChange(material, oldAmount));
                }
            }
        } catch (RuntimeException exception) {
            rollbackDepositChanges(regionId, appliedChanges, exception);
            throw exception;
        }
    }

    private void rollbackDepositChanges(String regionId, List<DepositChange> appliedChanges,
                                        RuntimeException primaryException) {
        for (int index = appliedChanges.size() - 1; index >= 0; index--) {
            DepositChange change = appliedChanges.get(index);
            try {
                depositRepository.saveDeposit(regionId, change.material(), change.oldAmount());
            } catch (RuntimeException rollbackException) {
                primaryException.addSuppressed(rollbackException);
            }
        }
    }

    private Map<Material, Integer> copyDeposits(Map<Material, Integer> deposits) {
        Map<Material, Integer> copy = new EnumMap<>(Material.class);
        copy.putAll(deposits);
        return copy;
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

    private boolean rollbackRegion(Region originalRegion, RuntimeException primaryException) {
        try {
            regionManager.replaceRegion(originalRegion);
            return true;
        } catch (RuntimeException rollbackException) {
            primaryException.addSuppressed(rollbackException);
            return false;
        }
    }

    private void restoreMainHand(Player player, ItemStack originalItem) {
        try {
            player.getInventory().setItemInMainHand(originalItem.clone());
        } catch (RuntimeException ignored) {
            // Best-effort rollback after a failed persistent deposit operation.
        }
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        overflow.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }

    private record WithdrawalPlan(Map<Material, Integer> remainingDeposits, List<ItemStack> itemsToReturn,
                                  int returnedAmount) {
    }

    private record DepositChange(Material material, int oldAmount) {
    }
}
