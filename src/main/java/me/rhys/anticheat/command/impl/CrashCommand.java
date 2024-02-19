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
        name = "crash",
        usage = "/%s crash",
        description = "Crash command",
        permission = "monolith.crash",
        subCommand = true
)
public class CrashCommand extends Command {

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {

            if (args.length > 1) {
                final String username = args[1];

                if (username != null) {
                    Player player = Bukkit.getPlayer(username);

                    if (player != null && player.isOnline()) {
                        commandSender.sendMessage(ChatColor.GREEN + "Player should now be crashed.");

                        Plugin.getInstance().getNmsManager().getNmsAbstraction().crashPlayer(Plugin.getInstance()
                                .getUserManager().getUser(player));
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
