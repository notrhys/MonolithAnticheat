package me.rhys.anticheat.command.api;

import lombok.Getter;
import lombok.Setter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class Command extends BukkitCommand implements ICommand {

    private String name;

    @Getter
    @Setter
    private String commandUsage;

    @Getter
    private boolean subCommand;

    private String permission;
    private String description;

    public Command() {
        super("");

        if (getClass().isAnnotationPresent(CommandInfo.class)) {
            CommandInfo commandInfo = getClass().getAnnotation(CommandInfo.class);

            this.name = String.format(commandInfo.name(), (Plugin.getInstance().getConfigValues().isHider()
                    ? Plugin.getInstance().getConfigValues().getHiderName() : "monolith"));

            this.commandUsage = commandInfo.usage();
            this.description = commandInfo.description();
            this.permission = commandInfo.permission();
            this.subCommand = commandInfo.subCommand();

            this.createCommand();
        }
    }

    private void createCommand() {

        try {
            Class<?> clazz = Class.forName("org.bukkit.command.Command");
            Field field = clazz.getDeclaredField("name");

            field.setAccessible(true);
            field.set(this, this.name);

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.usageMessage = "/" + name;
        this.setAliases(new ArrayList<>());
    }

    public boolean hasPermission(User user) {
        return user.getPlayer().hasPermission(this.permission) || user.isAllPermissions();
    }

    public boolean hasPermission(Player player) {
        User user = Plugin.getInstance().getUserManager().getUser(player);
        return player.hasPermission(this.permission) || user.isAllPermissions();
    }

    public boolean hasPermission(CommandSender commandSender) {
        User user = Plugin.getInstance().getUserManager().getUser(((Player) commandSender));
        return commandSender.hasPermission(this.permission) || user.isAllPermissions();
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        return this.onCommand(args, commandLabel, commandSender);
    }

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {
        return false;
    }
}