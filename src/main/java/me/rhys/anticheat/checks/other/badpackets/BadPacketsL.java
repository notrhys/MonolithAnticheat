package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutHeldItemSlot;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import org.bukkit.Bukkit;

@CheckInfo(name = "BadPackets", type = "L", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsL extends Check {

    private long lastFlying;
    private double threshold;

    /**
     * Post BlockPlace Packet check, detects Auto-Pots, Post Scaffolds, and anything that sends a BlockPlace Packet late.
     */

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {

            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {
                this.lastFlying = System.currentTimeMillis();
                break;
            }

            case Packet.Client.BLOCK_PLACE: {
                WrappedInBlockPlacePacket placePacket =
                        new WrappedInBlockPlacePacket(event.getPacket(), getUser().getPlayer());

                if (getUser().getPlayer().getAllowFlight() || getUser().getMovementProcessor().getTicks() < 60) {
                    threshold -= Math.min(threshold, 1);
                    return;
                }

                if (getUser().getConnectionProcessor().getPingDrop() > 100
                        || getUser().getConnectionProcessor().getSkippedPackets() > 5) {
                    this.threshold -= this.threshold > 0 ? threshold * 1.5 : 0;
                    return;
                }

                if (placePacket.getItemStack() != null) {

                    long timeDifference = System.currentTimeMillis() - this.lastFlying;

                    if (timeDifference < 5L) {
                        if (++threshold > 9) {
                            flag("Sending Packets Late, t=" + timeDifference);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.75);
                    }
                }
                break;
            }
        }
    }
}