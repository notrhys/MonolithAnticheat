package me.rhys.anticheat.base.user;

import lombok.Getter;
import me.rhys.anticheat.Plugin;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UserManager {
    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();

    public User getUser(Player player) {
        return this.userMap.get(player.getUniqueId());
    }

    public User getUser(UUID uuid) {
        return this.userMap.get(uuid);
    }

    public void add(Player player) {
        this.userMap.put(player.getUniqueId(), new User(player));
    }

    public void remove(Player player) {
        Plugin.getInstance().getThreadManager().shutdownThread(this.userMap.remove(player.getUniqueId()));
    }
}
