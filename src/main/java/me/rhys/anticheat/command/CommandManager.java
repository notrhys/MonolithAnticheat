package me.rhys.anticheat.command;

import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.command.api.Command;
import me.rhys.anticheat.command.impl.*;
import me.rhys.anticheat.util.CommandUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandManager {
    private final List<Command> commandList = new ArrayList<>();

    public void reload() {
        this.unRegisterAll();
        this.setup();
    }

    public void setup() {
        this.commandList.add(new MainCommand());
        this.commandList.add(new GUICommand());
        this.commandList.add(new ReloadCommand());
        this.commandList.add(new AlertsCommand());
        this.commandList.add(new ForceBanCommand());
        this.commandList.add(new LogsCommand());
        this.commandList.add(new CrashCommand());
        this.commandList.add(new InfoCommand());

        this.commandList.forEach(command -> command.setCommandUsage(String.format(command.getCommandUsage(),
                Plugin.getInstance().getConfigValues().isHider()
                        ? Plugin.getInstance().getConfigValues().getHiderName() : "monolith")));

        this.commandList.forEach(CommandUtil::registerCommand);
    }

    public void unRegisterAll() {
        this.commandList.forEach(command -> CommandUtil.unRegisterBukkitCommand(command.getName()));
        this.commandList.clear();
    }
}
