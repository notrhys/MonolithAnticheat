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
        name = "info",
        usage = "/%s info",
        description = "Info command",
        permission = "monolith.info",
        subCommand = true
)
public class InfoCommand extends Command {

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {

            if (args.length > 1) {
                final String username = args[1];

                if (username != null) {
                    Player player = Bukkit.getPlayer(username);

                    if (player != null && player.isOnline()) {
                        User targetUser = Plugin.getInstance().getUserManager().getUser(player);

                        commandSender.sendMessage(ChatColor.GRAY + "Information for " + ChatColor.GREEN
                                + targetUser.getUsername());

                        commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.RED + "Version: " + ChatColor.GREEN
                                + targetUser.getClientVersion().name());

                        commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.RED + "Ping: " + ChatColor.GREEN
                                + targetUser.getConnectionProcessor().getPing());
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Player is not online.");
                    }
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Invalid usage.");
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Invalid usage.");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
        }

        return false;
    }
}
