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
        name = "gui",
        usage = "/%s gui",
        description = "GUI command",
        permission = "monolith.gui",
        subCommand = true
)
public class ChecksCommand extends Command {

    private final int maxSlots = 27;

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {
            Inventory inventory = Bukkit.getServer().createInventory(null, this.maxSlots,
                    ChatColor.RED + "Checks");

            for (int slots = 0; slots < maxSlots; slots++) {
                if (inventory.getItem(slots) == null) inventory.setItem(slots, GUIUtils.createSpacer((byte) 7));
            }

            ((Player) commandSender).openInventory(inventory);
        } else {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
        }

        return false;
    }
}
