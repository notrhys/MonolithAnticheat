package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@Experimental
@CheckInfo(name = "BadPackets", type = "K", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsK extends Check {

    private int blockTick, digTick, attackTick;
    private double threshold;

    /**
     * Detects a flaw in auto-blocks
     */

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {


            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {

                if (getUser().getPlayer().getAllowFlight() || getUser().getMovementProcessor().getTicks() < 60) {
                    threshold -= Math.min(threshold, 1);
                    return;
                }

                if (digTick == 0 && blockTick == 0) {
                    this.threshold = 0;
                } else {
                    this.threshold += 1;
                }

                if (threshold > 14) {
                    flag();
                }

                this.digTick = this.blockTick = this.attackTick = 0;
                break;
            }

            case Packet.Client.BLOCK_DIG: {
                WrappedInBlockDigPacket digPacket =
                        new WrappedInBlockDigPacket(event.getPacket(), getUser().getPlayer());

                if (digPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {
                    this.digTick++;
                }
                break;
            }

            case Packet.Client.BLOCK_PLACE: {
                this.blockTick++;
                break;
            }
        }
    }
}