package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "BadPackets", type = "J", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsJ extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.BLOCK_PLACE)) {
            WrappedInBlockPlacePacket packet = new WrappedInBlockPlacePacket(event.getPacket(), getUser().getPlayer());

            if (getUser().getPlayer().getAllowFlight() || getUser().getMovementProcessor().getTicks() < 60) {
                threshold -= Math.min(threshold, 1);
                return;
            }

            double x = packet.getBlockPosition().getX();
            double y = packet.getBlockPosition().getY();
            double z = packet.getBlockPosition().getZ();

            if (x != 0 && y != 0 && z != 0) {
                if (x != -1 && y != -1 && z != -1) {
                    if (getUser().getCombatProcessor().getAttackTimer().hasNotPassed(1)) {
                        if (++threshold > 6) {
                            flag();
                        }
                    }
                } else {
                    threshold -= Math.min(threshold, 0.5);
                }
            }
        }
    }
}