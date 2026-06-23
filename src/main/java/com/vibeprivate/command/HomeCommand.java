package com.vibeprivate.command;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.config.ConfigService;
import com.vibeprivate.service.CommandCooldownService;
import com.vibeprivate.service.PendingTeleportService;
import com.vibeprivate.service.RegionHomeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

public final class HomeCommand implements CommandExecutor {
    private final MessageService messageService;
    private final ConfigService configService;
    private final RegionHomeService homeService;
    private final PendingTeleportService pendingTeleportService;
    private final CommandCooldownService cooldownService;

    public HomeCommand(MessageService messageService, ConfigService configService, RegionHomeService homeService,
                       PendingTeleportService pendingTeleportService, CommandCooldownService cooldownService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.configService = Objects.requireNonNull(configService, "configService");
        this.homeService = Objects.requireNonNull(homeService, "homeService");
        this.pendingTeleportService = Objects.requireNonNull(pendingTeleportService, "pendingTeleportService");
        this.cooldownService = Objects.requireNonNull(cooldownService, "cooldownService");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messageService.send(sender, "command.player-only");
            return true;
        }

        String commandName = command.getName().toLowerCase(java.util.Locale.ROOT);
        long remainingMillis = cooldownService.getRemainingMillis(player.getUniqueId(), commandName);
        if (remainingMillis > 0L) {
            messageService.send(player, "home.cooldown", Map.of(
                    "seconds", Long.toString(Math.max(1L, (remainingMillis + 999L) / 1000L))
            ));
            return true;
        }

        cooldownService.markUsed(player.getUniqueId(), commandName, configService.getHomeCommandCooldownSeconds(player));
        if (commandName.equals("sethome")) {
            handleSetHome(player);
        } else {
            handleHome(player);
        }
        return true;
    }

    private void handleSetHome(Player player) {
        if (homeService.setHome(player)) {
            messageService.send(player, "home.set.success");
        } else {
            messageService.send(player, "home.set.failed");
        }
    }

    private void handleHome(Player player) {
        Region region = homeService.getPlayerHomeRegion(player).orElse(null);
        if (region == null) {
            messageService.send(player, "home.teleport.no-region");
            return;
        }

        pendingTeleportService.start(player, region, homeService.getHome(region).orElse(null),
                configService.getHomeTeleportDelaySeconds(player),
                "home.teleport.start", "home.teleport.success", "home.teleport.failed");
    }
}
