package me.rhys.anticheat.base.tracker;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class PastLocationTracker {

    public void start() {
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Map.Entry<UUID, User> entry : Plugin.getInstance().getUserManager().getUserMap().entrySet()) {

                    if (entry.getValue().pastLocationTicks++ >= Integer.MAX_VALUE) {
                        entry.getValue().pastLocationTicks = 0;
                    }

                    if (entry.getValue().getCombatProcessor().getLastAttacked() == null) continue;

                    User targetUser = Plugin.getInstance().getUserManager()
                            .getUser(entry.getValue().getCombatProcessor().getLastAttacked().getUniqueId());

                    if (targetUser != null) {
                        entry.getValue().getPastLocation().addLocationTick(
                                entry.getValue(),
                                targetUser.getMovementProcessor().getTo().toLocation(targetUser.getPlayer().getWorld())
                        );
                    }
                }
            }
        }.runTaskTimerAsynchronously(Plugin.getServerInstance(), 0L, 0L);
    }
}
