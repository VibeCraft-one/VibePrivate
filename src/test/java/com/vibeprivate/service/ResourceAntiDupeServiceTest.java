package com.vibeprivate.service;

import com.vibeprivate.cache.PlayerRegionCache;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.model.VisualizationMode;
import com.vibeprivate.storage.RegionDepositRepository;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceAntiDupeServiceTest {

    @Test
    void fuelSaveFailureRestoresItemAndRegionAfterConsumingFirst() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        RegionManager regionManager = mock(RegionManager.class);
        ConfigService configService = mock(ConfigService.class);
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Region region = testRegion("r-fuel", UUID.randomUUID().toString(), 8);
        region.setEnabled(false);
        region.setFuelEmptySince(50L);
        region.setLastFuelDrainAt(60L);
        ItemStack fuel = mockItemStack(Material.COAL, 3);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(fuel);
        when(configService.getFuelMinutes(Material.COAL)).thenReturn(60);
        when(configService.getFuelMaxDays()).thenReturn(1);
        when(configService.getFuelRadiusCostMaxMultiplier()).thenReturn(1.0D);
        when(configService.getStartRadius(RegionType.HOME)).thenReturn(8);
        when(configService.getMaxRadius(RegionType.HOME)).thenReturn(64);
        doThrow(new IllegalStateException("save failed")).when(regionManager).saveRegion(region);

        FuelService service = new FuelService(plugin, regionManager, configService);
        FuelAddResult result = service.addFuelFromMainHand(player, region);

        assertEquals(FuelAddStatus.FAILED, result.getStatus());
        assertFalse(region.isEnabled());
        assertEquals(0L, region.getFuelExpiresAt());
        assertEquals(50L, region.getFuelEmptySince());
        assertEquals(60L, region.getLastFuelDrainAt());

        InOrder order = inOrder(inventory, regionManager);
        order.verify(inventory).setItemInMainHand(null);
        order.verify(regionManager).saveRegion(region);

        ArgumentCaptor<ItemStack> restored = ArgumentCaptor.forClass(ItemStack.class);
        verify(inventory, times(2)).setItemInMainHand(restored.capture());
        ItemStack restoredItem = restored.getAllValues().get(restored.getAllValues().size() - 1);
        assertEquals(Material.COAL, restoredItem.getType());
        assertEquals(3, restoredItem.getAmount());
    }

    @Test
    void depositSaveFailureRollsBackRadiusAndRestoresHand() {
        RegionManager regionManager = mock(RegionManager.class);
        ConfigService configService = mock(ConfigService.class);
        RegionDepositRepository depositRepository = mock(RegionDepositRepository.class);
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Region region = testRegion("r-upgrade", UUID.randomUUID().toString(), 8);
        ItemStack deposit = mockItemStack(Material.DIAMOND, 2);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(deposit);
        when(configService.getMaxRadius(RegionType.HOME)).thenReturn(64);
        when(configService.getStartRadius(RegionType.HOME)).thenReturn(8);
        when(configService.getUpgradeCostMultiplier()).thenReturn(1.0D);
        when(configService.getUpgradeDepositRadiusPoints(Material.DIAMOND)).thenReturn(10);
        doThrow(new IllegalStateException("deposit save failed"))
                .when(depositRepository).saveDeposit("r-upgrade", Material.DIAMOND, 2);

        RegionUpgradeService service = new RegionUpgradeService(regionManager, configService, depositRepository,
                ResourceAntiDupeServiceTest::mockItemStack, ignored -> 64);
        UpgradeResult result = service.depositFromMainHand(player, region);

        assertEquals(UpgradeStatus.FAILED, result.getStatus());
        InOrder order = inOrder(inventory, regionManager, depositRepository);
        order.verify(inventory).setItemInMainHand(null);
        order.verify(regionManager).replaceRegion(any(Region.class));
        order.verify(depositRepository).saveDeposit("r-upgrade", Material.DIAMOND, 2);
        order.verify(regionManager).replaceRegion(region);

        ArgumentCaptor<ItemStack> restored = ArgumentCaptor.forClass(ItemStack.class);
        verify(inventory, times(2)).setItemInMainHand(restored.capture());
        ItemStack restoredItem = restored.getAllValues().get(restored.getAllValues().size() - 1);
        assertEquals(Material.DIAMOND, restoredItem.getType());
        assertEquals(2, restoredItem.getAmount());
    }

    @Test
    void withdrawPersistsReducedDepositBeforeGivingItems() {
        RegionManager regionManager = mock(RegionManager.class);
        ConfigService configService = mock(ConfigService.class);
        RegionDepositRepository depositRepository = mock(RegionDepositRepository.class);
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Region region = testRegion("r-withdraw", UUID.randomUUID().toString(), 10);

        Map<Material, Integer> regionDeposits = new EnumMap<>(Material.class);
        regionDeposits.put(Material.DIAMOND, 5);
        Map<String, Map<Material, Integer>> loadedDeposits = new HashMap<>();
        loadedDeposits.put(region.getId(), regionDeposits);

        when(depositRepository.loadAll()).thenReturn(loadedDeposits);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.addItem(any(ItemStack.class))).thenReturn(new HashMap<>());
        when(configService.getStartRadius(RegionType.HOME)).thenReturn(8);
        when(configService.getUpgradeCostMultiplier()).thenReturn(1.0D);
        when(configService.getUpgradeDepositRadiusPoints(any(Material.class))).thenReturn(0);
        when(configService.getUpgradeDepositRadiusPoints(Material.DIAMOND)).thenReturn(1);

        RegionUpgradeService service = new RegionUpgradeService(regionManager, configService, depositRepository,
                ResourceAntiDupeServiceTest::mockItemStack, ignored -> 64);
        service.load();
        WithdrawDepositResult result = service.withdrawExcessDeposit(player, region);

        assertEquals(WithdrawDepositStatus.SUCCESS, result.getStatus());
        assertEquals(3, result.getReturnedAmount());

        InOrder order = inOrder(depositRepository, inventory);
        order.verify(depositRepository).saveDeposit("r-withdraw", Material.DIAMOND, 2);
        order.verify(inventory).addItem(any(ItemStack.class));
        assertEquals(2, service.getDeposits(region.getId()).get(Material.DIAMOND));
    }

    @Test
    void withdrawRollsBackEarlierDepositChangeWhenLaterPersistenceFails() {
        RegionManager regionManager = mock(RegionManager.class);
        ConfigService configService = mock(ConfigService.class);
        RegionDepositRepository depositRepository = mock(RegionDepositRepository.class);
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Region region = testRegion("r-withdraw-rollback", UUID.randomUUID().toString(), 10);

        Map<Material, Integer> regionDeposits = new EnumMap<>(Material.class);
        regionDeposits.put(Material.STONE, 5);
        regionDeposits.put(Material.DIAMOND, 5);
        Map<String, Map<Material, Integer>> loadedDeposits = new HashMap<>();
        loadedDeposits.put(region.getId(), regionDeposits);

        when(depositRepository.loadAll()).thenReturn(loadedDeposits);
        when(player.getInventory()).thenReturn(inventory);
        when(configService.getStartRadius(RegionType.HOME)).thenReturn(8);
        when(configService.getUpgradeCostMultiplier()).thenReturn(1.0D);
        when(configService.getUpgradeDepositRadiusPoints(any(Material.class))).thenReturn(0);
        when(configService.getUpgradeDepositRadiusPoints(Material.STONE)).thenReturn(1);
        when(configService.getUpgradeDepositRadiusPoints(Material.DIAMOND)).thenReturn(1);
        doThrow(new IllegalStateException("save failed"))
                .when(depositRepository).saveDeposit(eq("r-withdraw-rollback"), any(Material.class), eq(2));

        RegionUpgradeService service = new RegionUpgradeService(regionManager, configService, depositRepository,
                ResourceAntiDupeServiceTest::mockItemStack, ignored -> 64);
        service.load();
        WithdrawDepositResult result = service.withdrawExcessDeposit(player, region);

        assertEquals(WithdrawDepositStatus.FAILED, result.getStatus());
        InOrder order = inOrder(depositRepository);
        order.verify(depositRepository).deleteDeposit(eq("r-withdraw-rollback"), any(Material.class));
        order.verify(depositRepository).saveDeposit(eq("r-withdraw-rollback"), any(Material.class), eq(2));
        order.verify(depositRepository).saveDeposit(eq("r-withdraw-rollback"), any(Material.class), eq(5));
        verify(inventory, never()).addItem(any(ItemStack.class));
    }

    @Test
    void deleteWithDepositsFailsClosedWhenDropLocationIsUnavailable() {
        RegionManager regionManager = mock(RegionManager.class);
        RegionUpgradeService upgradeService = mock(RegionUpgradeService.class);
        PlayerRegionCache playerRegionCache = mock(PlayerRegionCache.class);
        Player player = mock(Player.class);
        UUID ownerId = UUID.randomUUID();
        Region region = testRegion("r-delete", ownerId.toString(), 8);

        when(player.getUniqueId()).thenReturn(ownerId);
        when(regionManager.getRegion(region.getId())).thenReturn(Optional.of(region));
        when(upgradeService.getDeposits(region.getId())).thenReturn(Map.of(Material.DIAMOND, 1));

        RegionDeletionService service = new RegionDeletionService(regionManager, upgradeService, playerRegionCache,
                new ConfirmationService(), ignored -> null);

        service.deleteOwned(player, region);
        service.deleteOwned(player, region);
        DeletionResult result = service.deleteOwned(player, region);

        assertEquals(DeletionStatus.FAILED, result.getStatus());
        verify(regionManager, never()).removeRegion(region.getId());
        verify(upgradeService, never()).clearDeposits(region.getId());
        verify(upgradeService, never()).forgetLoadedDeposits(region.getId());
        verify(playerRegionCache, never()).clear();
    }

    @Test
    void deleteDropsDepositsBeforeRegionRemovalAndThenForgetsLoadedState() {
        RegionManager regionManager = mock(RegionManager.class);
        RegionUpgradeService upgradeService = mock(RegionUpgradeService.class);
        PlayerRegionCache playerRegionCache = mock(PlayerRegionCache.class);
        Player player = mock(Player.class);
        World world = mock(World.class);
        UUID ownerId = UUID.randomUUID();
        Region region = testRegion("r-delete-drop", ownerId.toString(), 8);
        Location location = new Location(world, 0.5D, 65.0D, 0.5D);

        when(player.getUniqueId()).thenReturn(ownerId);
        when(regionManager.getRegion(region.getId())).thenReturn(Optional.of(region));
        when(regionManager.removeRegion(region.getId())).thenReturn(Optional.of(region));
        when(upgradeService.getDeposits(region.getId())).thenReturn(Map.of(Material.DIAMOND, 1));

        RegionDeletionService service = new RegionDeletionService(regionManager, upgradeService, playerRegionCache,
                new ConfirmationService(), ignored -> location, ResourceAntiDupeServiceTest::mockItemStack,
                ignored -> 64);

        service.deleteOwned(player, region);
        service.deleteOwned(player, region);
        DeletionResult result = service.deleteOwned(player, region);

        assertEquals(DeletionStatus.DELETED, result.getStatus());
        InOrder order = inOrder(regionManager, upgradeService, playerRegionCache, world);
        order.verify(world).dropItemNaturally(any(Location.class), any(ItemStack.class));
        order.verify(regionManager).removeRegion(region.getId());
        order.verify(upgradeService).forgetLoadedDeposits(region.getId());
        order.verify(playerRegionCache).clear();
    }

    @Test
    void deleteRollsBackSpawnedDepositsWhenLaterDropFails() {
        RegionManager regionManager = mock(RegionManager.class);
        RegionUpgradeService upgradeService = mock(RegionUpgradeService.class);
        PlayerRegionCache playerRegionCache = mock(PlayerRegionCache.class);
        Player player = mock(Player.class);
        World world = mock(World.class);
        Item firstDroppedItem = mock(Item.class);
        UUID ownerId = UUID.randomUUID();
        Region region = testRegion("r-delete-drop-failure", ownerId.toString(), 8);
        Location location = new Location(world, 0.5D, 65.0D, 0.5D);

        when(player.getUniqueId()).thenReturn(ownerId);
        when(regionManager.getRegion(region.getId())).thenReturn(Optional.of(region));
        when(upgradeService.getDeposits(region.getId())).thenReturn(Map.of(Material.DIAMOND, 65));
        when(world.dropItemNaturally(any(Location.class), any(ItemStack.class)))
                .thenReturn(firstDroppedItem)
                .thenThrow(new IllegalStateException("drop failed"));

        RegionDeletionService service = new RegionDeletionService(regionManager, upgradeService, playerRegionCache,
                new ConfirmationService(), ignored -> location, ResourceAntiDupeServiceTest::mockItemStack,
                ignored -> 64);

        service.deleteOwned(player, region);
        service.deleteOwned(player, region);
        DeletionResult result = service.deleteOwned(player, region);

        assertEquals(DeletionStatus.FAILED, result.getStatus());
        verify(firstDroppedItem).remove();
        verify(regionManager, never()).removeRegion(region.getId());
        verify(upgradeService, never()).forgetLoadedDeposits(region.getId());
        verify(playerRegionCache, never()).clear();
    }

    @Test
    void deleteRollsBackSpawnedDepositsWhenRegionRemovalFails() {
        RegionManager regionManager = mock(RegionManager.class);
        RegionUpgradeService upgradeService = mock(RegionUpgradeService.class);
        PlayerRegionCache playerRegionCache = mock(PlayerRegionCache.class);
        Player player = mock(Player.class);
        World world = mock(World.class);
        Item droppedItem = mock(Item.class);
        UUID ownerId = UUID.randomUUID();
        Region region = testRegion("r-delete-remove-failure", ownerId.toString(), 8);
        Location location = new Location(world, 0.5D, 65.0D, 0.5D);

        when(player.getUniqueId()).thenReturn(ownerId);
        when(regionManager.getRegion(region.getId())).thenReturn(Optional.of(region));
        when(regionManager.removeRegion(region.getId())).thenReturn(Optional.empty());
        when(upgradeService.getDeposits(region.getId())).thenReturn(Map.of(Material.DIAMOND, 1));
        when(world.dropItemNaturally(any(Location.class), any(ItemStack.class))).thenReturn(droppedItem);

        RegionDeletionService service = new RegionDeletionService(regionManager, upgradeService, playerRegionCache,
                new ConfirmationService(), ignored -> location, ResourceAntiDupeServiceTest::mockItemStack,
                ignored -> 64);

        service.deleteOwned(player, region);
        service.deleteOwned(player, region);
        DeletionResult result = service.deleteOwned(player, region);

        assertEquals(DeletionStatus.NOT_FOUND, result.getStatus());
        verify(droppedItem).remove();
        verify(upgradeService, never()).forgetLoadedDeposits(region.getId());
        verify(playerRegionCache, never()).clear();
    }

    private static Region testRegion(String id, String ownerId, int radius) {
        return Region.radiusRegion(id, id, RegionType.HOME, ownerId, "world")
                .radius(0, 0, radius, 64, 100)
                .state(true, 0L, 0L, 0L, 0, VisualizationMode.ALL, 100L)
                .build();
    }

    private static ItemStack mockItemStack(Material material, int amount) {
        ItemStack item = mock(ItemStack.class);
        ItemStack cloned = mock(ItemStack.class);
        ItemStack restored = mock(ItemStack.class);
        when(item.getType()).thenReturn(material);
        when(item.getAmount()).thenReturn(amount);
        when(item.clone()).thenReturn(cloned);
        when(cloned.getType()).thenReturn(material);
        when(cloned.getAmount()).thenReturn(amount);
        when(cloned.clone()).thenReturn(restored);
        when(restored.getType()).thenReturn(material);
        when(restored.getAmount()).thenReturn(amount);
        return item;
    }
}
