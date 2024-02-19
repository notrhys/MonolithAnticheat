package me.rhys.anticheat.command.impl;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.command.api.Command;
import me.rhys.anticheat.command.api.CommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        name = "alerts",
        usage = "/%s alerts",
        description = "Alerts command",
        permission = "monolith.command.alert",
        subCommand = true
)
public class AlertsCommand extends Command {

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {
            User user = Plugin.getInstance().getUserManager().getUser((Player) commandSender);

            user.setAlerts(!user.isAlerts());

            if (user.isAlerts()) {
                commandSender.sendMessage(ChatColor.GREEN + "Alerts enabled.");
            } else {
                commandSender.sendMessage(ChatColor.RED + "Alerts disabled.");
            }

        } else {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
        }

        return false;
    }
}
