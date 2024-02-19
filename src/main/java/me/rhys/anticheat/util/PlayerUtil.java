package me.rhys.anticheat.util;

import org.bukkit.Bukkit;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerUtil {
    private static final List<UUID> cache = new LinkedList<>();

    public static boolean isOnline(UUID uuid) {

        try {

            if (cache.contains(uuid)) {
                return true;
            }

            boolean online = Bukkit.getPlayer(uuid).isOnline();

            if (online && !cache.contains(uuid)) cache.add(uuid);

            return online;
        } catch (Exception ignored) {}

        return false;
    }
}
