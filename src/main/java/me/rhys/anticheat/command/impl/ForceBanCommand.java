package me.rhys.anticheat.command.impl;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.command.api.Command;
import me.rhys.anticheat.command.api.CommandInfo;
import me.rhys.anticheat.util.StaticUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandInfo(
        name = "forceban",
        usage = "/%s forceban",
        description = "Force Ban command",
        permission = "monolith.command.forceban",
        subCommand = true
)
public class ForceBanCommand extends Command {

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {

            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Please specify a player.");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);

            if (player != null) {
                User user = Plugin.getInstance().getUserManager().getUser(player);

                new BukkitRunnable() {
                    final String username = user.getUsername();

                    @Override
                    public void run() {
                        Plugin.getInstance().getConfigValues().getPunishCommands().forEach(s ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%PLAYER%",
                                        username)));

                        Plugin.getInstance().getConfigValues().getPunishMessages().forEach(s ->
                                Bukkit.broadcastMessage(s.replace("%PLAYER%", username)));
                    }
                }.runTask(Plugin.getServerInstance());
            } else {
                commandSender.sendMessage(ChatColor.RED + "Player is not online!");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
        }

        return false;
    }
}
