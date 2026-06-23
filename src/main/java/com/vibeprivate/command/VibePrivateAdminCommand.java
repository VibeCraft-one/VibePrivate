package com.vibeprivate.command;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.service.AdminRegionService;
import com.vibeprivate.service.RegionDeletionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class VibePrivateAdminCommand implements CommandExecutor, TabCompleter {
    private static final List<String> ADMIN_SUBCOMMANDS = List.of("pos1", "pos2", "create", "deletehere", "noclaim");
    private static final List<String> ADMIN_CREATE_SUGGESTIONS = List.of("spawn", "event");

    private final MessageService messageService;
    private final AdminCommandHandler adminCommandHandler;

    public VibePrivateAdminCommand(MessageService messageService, RegionManager regionManager,
                                   AdminRegionService adminRegionService,
                                   RegionDeletionService regionDeletionService) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.adminCommandHandler = new AdminCommandHandler(messageService, regionManager,
                adminRegionService, regionDeletionService);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messageService.send(sender, "command.player-only");
            return true;
        }

        String[] adminArgs = new String[args.length + 1];
        adminArgs[0] = "admin";
        System.arraycopy(args, 0, adminArgs, 1, args.length);
        adminCommandHandler.handle(player, adminArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(AdminCommandHandler.PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            return matching(ADMIN_SUBCOMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return matching(ADMIN_CREATE_SUGGESTIONS, args[1]);
        }

        return List.of();
    }

    private List<String> matching(List<String> values, String input) {
        String normalized = input.toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();
        for (String value : values) {
            if (value.startsWith(normalized)) {
                completions.add(value);
            }
        }

        return completions;
    }
}
