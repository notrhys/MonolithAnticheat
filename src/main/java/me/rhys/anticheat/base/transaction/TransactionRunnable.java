package me.rhys.anticheat.base.transaction;

import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.RunUtils;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;

@Getter
public class TransactionRunnable implements Runnable {

    private BukkitTask bukkitTask;
    private short action = Short.MIN_VALUE;

    public TransactionRunnable() {
        start();
    }

    @Override
    public void run() {

        if (++action > 0) {
            action = Short.MIN_VALUE;
        }

        long now = System.currentTimeMillis();

        WrappedOutTransaction wrappedOutTransaction = new WrappedOutTransaction(0, this.action, false);

        for (Map.Entry<UUID, User> uuidUserEntry : Plugin.getInstance().getUserManager().getUserMap().entrySet()) {
            User user = uuidUserEntry.getValue();

            user.getConnectionProcessor().sendTransaction(now, wrappedOutTransaction);
        }
    }

    void start() {
        if (bukkitTask == null) {
            bukkitTask = RunUtils.taskTimerAsync(this, 0L, 0L);
        }
    }
}
