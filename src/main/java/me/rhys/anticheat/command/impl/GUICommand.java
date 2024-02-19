package me.rhys.anticheat.command.impl;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.command.api.Command;
import me.rhys.anticheat.command.api.CommandInfo;
import me.rhys.anticheat.util.GUIUtils;
import me.rhys.anticheat.util.StaticUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

@CommandInfo(
        name = "gui",
        usage = "/%s gui",
        description = "GUI command",
        permission = "monolith.gui",
        subCommand = true
)
public class GUICommand extends Command {

    private final int maxSlots = 27;

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {

            Inventory inventory = Bukkit.getServer().createInventory(null, this.maxSlots,
                    ChatColor.RED + StaticUtil.getAnticheatName());

            int totalChecks = Plugin.getInstance().getStaticCheckManager().getCheckList().size();

            int enabledChecks = (int) Plugin.getInstance().getStaticCheckManager().getCheckList().stream()
                    .filter(Check::isEnabled).count();

            int disabledChecks = (int) Plugin.getInstance().getStaticCheckManager().getCheckList().stream()
                    .filter(check -> !check.isEnabled()).count();

            inventory.setItem(11, GUIUtils.generateItem(new ItemStack(Material.COMPASS, 1),
                    ChatColor.RED + "Checks",
                    Collections.singletonList(ChatColor.GRAY + "Click to manage checks.")));

            inventory.setItem(13, GUIUtils.generateItem(new ItemStack(Material.DIAMOND, 1),
                    ChatColor.RED + String.format("%s Anticheat", StaticUtil.getAnticheatName()),
                    Arrays.asList(ChatColor.GRAY + "Version: " +
                            ChatColor.GREEN + Plugin.getInstance().getManifest().getVersion(),
                            ChatColor.GRAY + "Checks: " + ChatColor.YELLOW + totalChecks + ChatColor.GRAY + "/"
                                    + ChatColor.GREEN + enabledChecks
                                    + ChatColor.GRAY + "/" + ChatColor.RED + disabledChecks
                    )
            ));

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
