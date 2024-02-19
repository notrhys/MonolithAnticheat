package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import org.bukkit.Bukkit;

@Experimental
@CheckInfo(name = "BadPackets", type = "M", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsM extends Check {

    private double threshold;
    private int swingTicks;

    /**
     * A basic No-Swing detection for 1.9.x.
     */

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            this.swingTicks = 20;
        }

        if (event.getType().equalsIgnoreCase(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket useEntityPacket =
                    new WrappedInUseEntityPacket(event.getPacket(), getUser().getPlayer());

            if (useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {

                if (isNotValid()) {
                    threshold = 0;
                    return;
                }

                if (swingTicks == 0) {
                    if (++threshold > 20) {
                        flag("No Swing");
                    }
                } else {
                    threshold -= Math.min(threshold, 1.2);
                }
            }
        }

        if (event.isMovement()) {
            if (this.swingTicks > 0) {
                this.swingTicks--;
            }
        }
    }

    private boolean isNotValid() {
        return !getUser().isOrAbove1_9() || getUser().getPlayer().getAllowFlight()
                || getUser().getConnectionProcessor().getSkippedPackets() > 5
                || getUser().getConnectionProcessor().getPingDrop() > 500;
    }
}