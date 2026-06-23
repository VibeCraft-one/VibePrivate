package com.vibeprivate;

import com.vibeprivate.command.HomeCommand;
import com.vibeprivate.command.VibePrivateAdminCommand;
import com.vibeprivate.command.VibePrivateCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.Objects;

final class VibePrivateCommandRegistrar {
    private final VibePrivatePlugin plugin;
    private final VibePrivateServices services;

    VibePrivateCommandRegistrar(VibePrivatePlugin plugin, VibePrivateServices services) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.services = Objects.requireNonNull(services, "services");
    }

    void register() {
        VibePrivateCommand command = new VibePrivateCommand(services.messageService(), services.regionManager());
        registerCommand("vp", command);
        registerCommand("privat", command);
        registerCommand("privatadmin", new VibePrivateAdminCommand(services.messageService(), services.regionManager(),
                services.adminRegionService(), services.regionDeletionService()));
        HomeCommand homeCommand = new HomeCommand(services.messageService(), services.configService(),
                services.regionHomeService(), services.pendingTeleportService(), services.commandCooldownService());
        registerCommand("home", homeCommand);
        registerCommand("sethome", homeCommand);
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            plugin.getLogger().warning("Command is missing from plugin.yml: " + name);
            return;
        }

        command.setExecutor(executor);
        if (executor instanceof TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }
}
