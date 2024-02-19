package me.rhys.anticheat.command.impl;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.command.api.Command;
import me.rhys.anticheat.command.api.CommandInfo;
import me.rhys.anticheat.util.StaticUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

@CommandInfo(
        name = "%s",
        usage = "/%s",
        description = "Main anticheat command",
        permission = "monolith.command",
        subCommand = false
)

public class MainCommand extends Command {

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "Only player's can execute this command!");
            return true;
        }

        if (this.hasPermission(commandSender)) {

            if (args.length > 0) {
                Command foundCommand = Plugin.getInstance().getCommandManager().getCommandList()
                        .stream().filter(command -> command.getName()
                                .equalsIgnoreCase(args[0].toLowerCase(Locale.ROOT))).findAny().orElse(null);

                if (foundCommand != null) {
                    foundCommand.onCommand(args, s, commandSender);
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Sub-command not found!");
                }

                return true;
            }

            commandSender.sendMessage(ChatColor.RED + String.format("%s Anticheat ", StaticUtil.getAnticheatName())
                    + ChatColor.GRAY + "(" + ChatColor.GREEN + Plugin.getInstance().getManifest().getVersion()
                    + ChatColor.GRAY + ")");

            commandSender.sendMessage(ChatColor.GRAY + "Commands:");

            Plugin.getInstance().getCommandManager().getCommandList().stream().filter(command -> command != this)
                    .forEach(command -> {
                        TextComponent textComponent = new TextComponent(ChatColor.DARK_GRAY + " ► "
                                + ChatColor.GRAY + command.getCommandUsage());

                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                                ChatColor.GRAY + command.getCommandUsage()).create()));

                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                command.getCommandUsage()));

                        ((Player) commandSender).spigot().sendMessage(textComponent);

            });
        } else {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
        }

        return true;
    }
}
