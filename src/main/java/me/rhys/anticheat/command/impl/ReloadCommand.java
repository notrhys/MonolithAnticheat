package me.rhys.anticheat.command.impl;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.command.api.Command;
import me.rhys.anticheat.command.api.CommandInfo;
import me.rhys.anticheat.util.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@CommandInfo(
        name = "reload",
        usage = "/%s reload",
        description = "Reload command",
        permission = "monolith.command.reload",
        subCommand = true
)
public class ReloadCommand extends Command {

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {

            Plugin.getInstance().getConfigLoader().loadConfig();
            Plugin.getInstance().getStaticCheckManager().reload();

            Plugin.getInstance().getUserManager().getUserMap().forEach((uuid, user) ->
                    Plugin.getInstance().getUserManager().remove(user.getPlayer()));

            Bukkit.getServer().getOnlinePlayers().forEach(player ->
                    Plugin.getInstance().getUserManager().add(player));

            commandSender.sendMessage(ChatColor.GREEN + "Reloaded.");
        } else {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
        }

        return false;
    }
}
