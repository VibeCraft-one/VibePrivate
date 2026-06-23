package com.vibeprivate.service;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PendingTeleportService {
    private final JavaPlugin plugin;
    private final MessageService messageService;
    private final RegionTeleportService teleportService;
    private final Map<UUID, PendingTeleport> pendingTeleports = new HashMap<>();
    private BukkitTask task;

    public PendingTeleportService(JavaPlugin plugin, MessageService messageService,
                                  RegionTeleportService teleportService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.teleportService = Objects.requireNonNull(teleportService, "teleportService");
    }

    public void start(Player player, Region region, RegionHome home, int delaySeconds,
                      String startKey, String successKey, String failedKey) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(region, "region");
        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            messageService.send(player, failedKey);
            return;
        }

        pendingTeleports.put(player.getUniqueId(), new PendingTeleport(
                region,
                home,
                world.getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                System.currentTimeMillis() + delaySeconds * 1000L,
                successKey,
                failedKey
        ));
        messageService.send(player, startKey, Map.of("seconds", Integer.toString(delaySeconds)));
        ensureTask();
    }

    public void cancelIfMoved(Player player, Location to) {
        Objects.requireNonNull(player, "player");
        PendingTeleport pending = pendingTeleports.get(player.getUniqueId());
        if (pending == null || to == null || to.getWorld() == null) {
            return;
        }

        if (!pending.worldName.equals(to.getWorld().getName())
                || pending.blockX != to.getBlockX()
                || pending.blockY != to.getBlockY()
                || pending.blockZ != to.getBlockZ()) {
            cancel(player);
        }
    }

    public void cancel(Player player) {
        Objects.requireNonNull(player, "player");
        if (pendingTeleports.remove(player.getUniqueId()) != null) {
            messageService.send(player, "teleport.cancelled");
        }
        stopTaskIfIdle();
    }

    public void stop() {
        pendingTeleports.clear();
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void ensureTask() {
        if (task != null) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    private void tick() {
        if (pendingTeleports.isEmpty()) {
            stopTaskIfIdle();
            return;
        }

        long now = System.currentTimeMillis();
        java.util.Iterator<Map.Entry<UUID, PendingTeleport>> iterator = pendingTeleports.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingTeleport> entry = iterator.next();
            PendingTeleport pending = entry.getValue();
            if (pending.readyAtMillis > now) {
                continue;
            }

            Player player = Bukkit.getPlayer(entry.getKey());
            iterator.remove();
            if (player == null || !player.isOnline()) {
                continue;
            }

            boolean teleported = teleportService.teleportHome(player, pending.region, pending.home);
            messageService.send(player, teleported ? pending.successKey : pending.failedKey);
        }

        stopTaskIfIdle();
    }

    private void stopTaskIfIdle() {
        if (!pendingTeleports.isEmpty() || task == null) {
            return;
        }

        task.cancel();
        task = null;
    }

    private record PendingTeleport(Region region, RegionHome home, String worldName,
                                   int blockX, int blockY, int blockZ, long readyAtMillis,
                                   String successKey, String failedKey) {
    }
}
