package me.rhys.anticheat.command.impl;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.command.api.Command;
import me.rhys.anticheat.command.api.CommandInfo;
import me.rhys.anticheat.log.PlayerLogManager;
import me.rhys.anticheat.util.HTTPUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.nio.charset.StandardCharsets;
import java.util.*;

@CommandInfo(
        name = "logs",
        usage = "/%s logs",
        description = "Logs command",
        permission = "monolith.command.logs",
        subCommand = true
)
public class LogsCommand extends Command {

    @Override
    public boolean onCommand(String[] args, String s, CommandSender commandSender) {

        if (this.hasPermission(commandSender)) {

            if (args.length > 1) {
                final String username = args[1];

                if (username != null) {

                    if (username.equalsIgnoreCase("clear") && args.length > 2) {
                        final String clearName = args[2];

                        if (clearName != null && clearName.length() > 0) {

                            commandSender.sendMessage(ChatColor.RED + "Cleared all logs for " + clearName);

                            Plugin.getInstance().getExecutorService().execute(() -> {
                                final String UUID = Bukkit.getServer().getOnlineMode() ?
                                        HTTPUtil.getUUID(clearName) : clearName;

                                if (UUID != null && UUID.length() > 0) {
                                    final Map<String, String> headers = new HashMap<>();
                                    headers.put("authentication", Plugin.getInstance().getLicenseKey());
                                    headers.put("uuid", UUID);
                                    headers.put("mode", "clear");

                                    HTTPUtil.getResponse(
                                            "https://monolith.sparky.ac/service/b05bcf68-7f0e-45e1-b173-3e8a93fc55f9",
                                            headers
                                    );

                                    headers.clear();
                                }
                            });

                            return true;
                        }
                    }

                    Plugin.getInstance().getExecutorService().execute(() -> {
                        commandSender.sendMessage(ChatColor.GRAY + "Getting information for " +
                                ChatColor.GREEN + username);

                        final String UUID = Bukkit.getServer().getOnlineMode()
                                ? HTTPUtil.getUUID(username) : username;

                        if (UUID == null || UUID.length() < 1) {
                            commandSender.sendMessage(ChatColor.RED + "Could not find UUID for "
                                    + ChatColor.GRAY + username);
                            return;
                        }

                        final Map<String, String> headers = new HashMap<>();
                        headers.put("authentication", Plugin.getInstance().getLicenseKey());
                        headers.put("uuid", UUID);

                        final String logsResponse = HTTPUtil.getResponse(
                                "https://monolith.sparky.ac/service/b05bcf68-7f0e-45e1-b173-3e8a93fc55f9",
                                headers
                        );

                        final List<PlayerLogManager.LogEntry> logs = new ArrayList<>();

                        if (logsResponse != null && logsResponse.length() > 5) {
                            for (final String split : logsResponse.split("<LINE>")) {
                                String[] decrypted = new String(Base64.getDecoder()
                                        .decode(split.getBytes(StandardCharsets.UTF_8))).split(":");

                                if (decrypted.length > 0) {
                                    logs.add(new PlayerLogManager.LogEntry(decrypted[0], decrypted[1],
                                            Integer.parseInt(decrypted[2]),
                                            Boolean.parseBoolean(decrypted[3]), decrypted[4]));
                                }
                            }
                        }

                        if (logs.size() < 1) {
                            commandSender.sendMessage(ChatColor.RED + username + ChatColor.GRAY + " has no logs.");
                        } else {
                            commandSender.sendMessage(ChatColor.GREEN + "Logs for " + ChatColor.GRAY
                                    + username + ChatColor.GREEN + " (" + logs.size() + ")");

                            final Map<String, Integer> fixedLogs = new HashMap<>();

                            logs.forEach(logEntry -> {
                                String path = logEntry.getName() + logEntry.getType();
                                fixedLogs.put(path, fixedLogs.getOrDefault(path, 0) + 1);
                            });

                            fixedLogs.forEach((s1, integer) -> commandSender.sendMessage(ChatColor.GRAY + " - "
                                    + ChatColor.GREEN + s1 + ChatColor.RED + " x" + integer));
                        }

                        headers.clear();
                    });
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
