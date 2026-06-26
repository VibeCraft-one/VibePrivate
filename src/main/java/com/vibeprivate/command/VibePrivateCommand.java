package com.vibeprivate.command;

import com.vibeprivate.gui.PrivateMainMenu;
import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.service.UpkeepService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class VibePrivateCommand implements CommandExecutor, TabCompleter {
    private final MessageService messageService;
    private final RegionManager regionManager;
    private final UpkeepService upkeepService;

    public VibePrivateCommand(MessageService messageService, RegionManager regionManager, UpkeepService upkeepService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.upkeepService = Objects.requireNonNull(upkeepService, "upkeepService");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            messageService.send(sender, "command.player-only");
            return true;
        }

        Player player = (Player) sender;
        if (args.length > 0 && args[0].equalsIgnoreCase("upkeep")) {
            sendUpkeep(player);
            return true;
        }

        player.openInventory(new PrivateMainMenu(messageService, regionManager, player).getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            if ("help".startsWith(input)) {
                completions.add("help");
            }
            if ("upkeep".startsWith(input)) {
                completions.add("upkeep");
            }

            return completions;
        }

        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        messageService.send(sender, "command.help.header");
        messageService.send(sender, "command.help.open");
        messageService.send(sender, "command.help.help");
        messageService.send(sender, "command.help.upkeep");
        if (sender.hasPermission(AdminCommandHandler.PERMISSION)) {
            messageService.send(sender, "command.help.admin-open");
        }
    }

    private void sendUpkeep(Player player) {
        String ownerId = player.getUniqueId().toString();
        double cost = upkeepService.getDailyCost(ownerId);
        messageService.send(player, "upkeep.status", Map.of(
                "mode", upkeepService.getMode().name().toLowerCase(Locale.ROOT),
                "chunks", Integer.toString(upkeepService.getChunkCount(ownerId)),
                "cost", upkeepService.formatCost(cost),
                "debt", Integer.toString(upkeepService.getDebtDays(ownerId))
        ));
    }
}
