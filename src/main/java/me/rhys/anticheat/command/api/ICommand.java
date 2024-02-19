package me.rhys.anticheat.command.api;

import org.bukkit.command.CommandSender;

public interface ICommand {
    boolean onCommand(String[] args, String s, CommandSender commandSender);
}
