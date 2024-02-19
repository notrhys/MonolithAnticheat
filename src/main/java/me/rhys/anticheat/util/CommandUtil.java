package me.rhys.anticheat.util;

import me.rhys.anticheat.command.api.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.HashMap;

public class CommandUtil {
    public static void registerCommand(Command commandObject) {

        if (commandObject.isSubCommand()) return;

        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(commandObject.getUsage(), commandObject);
            bukkitCommandMap.setAccessible(false);
        } catch (Exception ignored) {
            //
        }
    }

    public static void unRegisterBukkitCommand(String commandName) {

        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap1 = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            org.bukkit.command.Command command = commandMap1.getCommand(commandName);
            if (command != null) {
                try {
                    Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
                    SimpleCommandMap commandMap = (SimpleCommandMap) result;
                    Object map = getPrivateField(commandMap, "knownCommands");
                    @SuppressWarnings("unchecked")
                    HashMap<String, org.bukkit.command.Command> knownCommands =
                            (HashMap<String, org.bukkit.command.Command>) map;
                    knownCommands.remove(command.getName());
                    for (String alias : command.getAliases()) {
                        knownCommands.remove(alias);
                    }
                } catch (Exception ignored) {
                    //
                }
            }
        } catch (Exception ignored) {
            //
        }
    }

    public static void unRegisterBukkitCommand(Command commandObject) {
        if (commandObject.isSubCommand()) return;

        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap1 = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            org.bukkit.command.Command command = commandMap1.getCommand(commandObject.getUsage());
            if (command != null) {
                try {
                    Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
                    SimpleCommandMap commandMap = (SimpleCommandMap) result;
                    Object map = getPrivateField(commandMap, "knownCommands");
                    @SuppressWarnings("unchecked")
                    HashMap<String, org.bukkit.command.Command> knownCommands =
                            (HashMap<String, org.bukkit.command.Command>) map;
                    knownCommands.remove(command.getName());
                    for (String alias : command.getAliases()) {
                        knownCommands.remove(alias);
                    }
                } catch (Exception ignored) {
                    //
                }
            }
        } catch (Exception ignored) {
            //
        }
    }

    private static Object getPrivateField(Object object, String field) throws Exception {
        Class<?> clazz = object.getClass();

        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);

        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }
}
