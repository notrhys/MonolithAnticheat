package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;


@Experimental
@CheckInfo(name = "BadPackets", type = "H", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsH extends Check {

    private int blockTick, interactTick;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {

            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {
                this.blockTick -= Math.min(this.blockTick, 1);
                this.interactTick -= Math.min(this.interactTick, 1);
                break;
            }

            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket useEntityPacket =
                        new WrappedInUseEntityPacket(event.getPacket(), getUser().getPlayer());

                if (useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.INTERACT
                        || useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.INTERACT_AT) {
                    this.interactTick = 20;
                }

                if (useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {

                    if (getUser().isSword(getUser().getPlayer().getItemInHand())
                            && getUser().getPlayer().getItemInHand() != null) {
                        if (this.interactTick != this.blockTick) {
                            if (++threshold > 32) {
                                flag();
                            }
                        } else {
                            threshold -= Math.min(threshold, 2.25);
                        }
                    }
                }

                break;
            }
            case Packet.Client.BLOCK_PLACE: {

                if (getUser().isSword(getUser().getPlayer().getItemInHand())
                        && getUser().getPlayer().getItemInHand() != null) {
                    this.blockTick = 20;
                }
                break;
            }
        }
    }
}