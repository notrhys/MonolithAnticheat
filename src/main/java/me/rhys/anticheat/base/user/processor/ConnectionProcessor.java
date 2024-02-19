package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTransactionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutTransaction;
import cc.funkemunky.api.utils.RunUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
public class ConnectionProcessor {
    private final User user;

    private final Map<Short, Long> transactionMap = new ConcurrentHashMap<>(60);
    private final Map<Short, TransactionQueue> transactionQueueMap = new HashMap<>();

    private long ping, lastPing, pingDrop, lastTransaction;

    private final long MAX_TIMEOUT = TimeUnit.SECONDS.toMillis(7L);

    private int sentTransactions, pingTicks, skippedPackets;

    private int offset;
    private long tick;

    private int test;

    public ConnectionProcessor(User user) {
        this.user = user;
    }

    public void handle(String type, Object packet, long now) {
        switch (type) {
            case Packet.Client.TRANSACTION: {
                WrappedInTransactionPacket wrappedInTransactionPacket = new WrappedInTransactionPacket(packet,
                        this.user.getPlayer());

                short action = wrappedInTransactionPacket.getAction();

                if (this.transactionQueueMap.containsKey(action)) {
                    this.transactionQueueMap.remove(action).getRunnable().run();
                }

                if (this.transactionMap.containsKey(action)) {
                    this.lastPing = this.ping;
                    this.ping = (now - this.transactionMap.remove(action));

                    this.pingDrop = (this.ping - this.lastPing);
                    this.lastTransaction = now;

                    this.pingTicks = (int) ((this.ping / 50.0) + 1);
                }
                break;
            }

            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {

                if ((now - this.lastTransaction) > this.MAX_TIMEOUT && this.sentTransactions > 10
                        && getUser().getMovementProcessor().getTicks() > 60
                        && Plugin.getInstance().getConfigValues().isFaggotKick()) {
                    this.lastTransaction = now;
                    user.kick("Ignoring too many transactions");
                }
                break;
            }
        }
    }

    void handleSkippedPackets(long now) {
        if ((now - this.user.getMovementProcessor().getLastFlyingPacket()) < 30L) {
            this.skippedPackets += this.skippedPackets < 20 ? 3 : 0;
        } else {
            this.skippedPackets -= this.skippedPackets > 0 ? 1 : 0;
        }
    }

    public void sendTransaction(long now, WrappedOutTransaction wrappedOutTransaction) {
        TinyProtocolHandler.sendPacket(this.user.getPlayer(), wrappedOutTransaction.getObject());
        this.transactionMap.put(wrappedOutTransaction.getAction(), now);

        if (this.sentTransactions < 20) {
            this.sentTransactions++;
        }
    }

    public void queue(Runnable runnable) {
        short action = (short) (Plugin.getInstance().getTransactionRunnable().getAction() + 1 +
                this.transactionQueueMap.size());

        this.transactionQueueMap.put(action, new TransactionQueue(action, runnable));

        // make sure they get the transaction by send the same ID twice, this will help us if the packet gets missed
        TinyProtocolHandler.sendPacket(
                this.user.getPlayer(),
                new WrappedOutTransaction(0, action, false).getObject()
        );
    }

    @Getter @AllArgsConstructor
    public static final class TransactionQueue {
        private final short action;
        private final Runnable runnable;
    }
}
