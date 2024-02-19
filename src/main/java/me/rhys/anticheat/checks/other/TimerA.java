package me.rhys.anticheat.checks.other;

import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

import java.util.concurrent.TimeUnit;

@CheckInfo(name = "Timer", type = "A", checkType = CheckType.OTHER, enabled = true)
public class TimerA extends Check {

    private long lastNanos = -1L;
    private long balance;

    private double buffer;
    private long lastFlag;

    private final long bufferReset = TimeUnit.SECONDS.toMillis(45);

    @Override
    public void onTransactionTeleport(WrappedOutPositionPacket wrappedOutPositionPacket) {
        this.balance -= TimeUnit.MILLISECONDS.toNanos(
                250L + this.getUser().getConnectionProcessor().getPingTicks()
        );
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement()) return;

        long now = event.getNow();
        long nanos = System.nanoTime();
        long lastDelta = (nanos - this.lastNanos);

        long delay = (50000000L - lastDelta);

        // cope about it
        if (!getUser().isBelow1_8()) {
            long toMillis = TimeUnit.NANOSECONDS.toMillis(Math.abs(this.balance));

            if (toMillis > 3000L) {
                this.balance = (long) -3e+9;
            }
        }

        if (this.lastNanos > -1L) {
            this.balance += delay;

            if (this.balance > 45000000L && getUser().getMovementProcessor().getTicks() > 60) {

                if (this.buffer++ > 7) {
                    this.flag(
                            "buffer=" + this.buffer,
                            "balance=" + this.balance
                    );
                }

                this.lastFlag = now;
                this.balance = 0;
            }

            if ((now - this.lastFlag) > this.bufferReset) {
                this.buffer -= this.buffer > 0 ? .1 : 0;
            }
        }

        this.lastNanos = nanos;
    }
}
