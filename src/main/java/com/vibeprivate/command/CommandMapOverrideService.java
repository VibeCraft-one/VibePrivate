package com.vibeprivate.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;

public final class CommandMapOverrideService {
    private final JavaPlugin plugin;

    public CommandMapOverrideService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void claimPrimaryCommands(String... commandNames) {
        Bukkit.getScheduler().runTask(plugin, () -> claimNow(commandNames));
    }

    private void claimNow(String... commandNames) {
        CommandMap commandMap = resolveCommandMap();
        if (commandMap == null) {
            plugin.getLogger().warning("Could not access Bukkit command map; /home may still be handled by another plugin.");
            return;
        }

        for (String commandName : commandNames) {
            PluginCommand command = plugin.getCommand(commandName);
            if (command == null) {
                plugin.getLogger().warning("Command is missing from plugin.yml: " + commandName);
                continue;
            }

            String label = commandName.toLowerCase(Locale.ROOT);
            removeKnownCommand(commandMap, label);
            command.unregister(commandMap);
            commandMap.register(label, plugin.getName().toLowerCase(Locale.ROOT), command);
            plugin.getLogger().info("Claimed /" + label + " command for VibePrivate.");
        }
    }

    @SuppressWarnings("unchecked")
    private void removeKnownCommand(CommandMap commandMap, String label) {
        if (!(commandMap instanceof SimpleCommandMap)) {
            return;
        }

        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            knownCommands.remove(label);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Could not remove existing /" + label + " command: " + exception.getMessage());
        }
    }

    private CommandMap resolveCommandMap() {
        try {
            Method method = plugin.getServer().getClass().getMethod("getCommandMap");
            Object commandMap = method.invoke(plugin.getServer());
            if (commandMap instanceof CommandMap map) {
                return map;
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall back to CraftServer's private field below.
        }

        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            Object commandMap = commandMapField.get(plugin.getServer());
            if (commandMap instanceof CommandMap map) {
                return map;
            }
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Could not resolve Bukkit command map: " + exception.getMessage());
        }

        return null;
    }
}
