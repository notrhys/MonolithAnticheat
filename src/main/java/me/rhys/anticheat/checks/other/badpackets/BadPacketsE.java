package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.VersionUtil;

@CheckInfo(name = "BadPackets", type = "E", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsE extends Check {

    private double threshold, maxThreshold = 20;
    private boolean sentSwing;

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            this.sentSwing = true;
        }

        if (event.getType().equalsIgnoreCase(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket useEntityPacket =
                    new WrappedInUseEntityPacket(event.getPacket(), getUser().getPlayer());

            if (useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {

                //temp fix until I make a no swing check for 1.9
                if (getUser().isOrAbove1_9()) {
                    this.maxThreshold = 40;
                    this.threshold = 0;
                    return;
                }

                if (getUser().getConnectionProcessor().getPingDrop() > 500
                        || getUser().getConnectionProcessor().getSkippedPackets() > 5) {
                    this.threshold -= Math.min(threshold, 10);
                    return;
                }

                if (!this.sentSwing) {
                    if (++threshold >= this.maxThreshold) {
                        flag("No swing while attacking");
                    }
                } else {
                    this.threshold -= Math.min(threshold, 1.5);
                }
            }
        }

        if (event.isMovement()) {
            this.sentSwing = false;
        }
    }
}