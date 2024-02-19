package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "BadPackets", type = "C", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsC extends Check {

    private long last;
    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {
            case Packet.Client.POSITION_LOOK: {
                this.last = event.getNow();
                break;
            }

            case Packet.Client.ARM_ANIMATION: {

                long delta = (event.getNow() - this.last);

                if (getUser().getConnectionProcessor().getPingDrop() > 100
                        || getUser().getConnectionProcessor().getSkippedPackets() > 5
                        || getUser().getActionProcessor().getLastTransactionTeleport().hasNotPassed(20)) {
                    this.buffer -= this.buffer > 0 ? 1 : 0;
                    return;
                }

                if (delta < 5L) {

                    if (this.buffer++ > 5) {
                        this.flag("delta= " + delta);
                    }

                } else {
                    this.buffer -= this.buffer > 0 ? .65 : 0;
                }

                break;
            }
        }
    }
}
