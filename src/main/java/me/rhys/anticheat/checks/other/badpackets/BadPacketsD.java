package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "BadPackets", type = "D", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsD extends Check {

    private int lastInteract;
    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {

        int tick = getUser().getMovementProcessor().getTicks();

        switch (event.getType()) {

            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket wrapped = getUser().getCombatProcessor().getLastUseEntity();

                if (wrapped == null) return;

                switch (wrapped.getAction()) {
                    case INTERACT:
                    case INTERACT_AT: {
                        this.lastInteract = tick;
                        break;
                    }
                }

                break;
            }

            case Packet.Client.BLOCK_DIG: {
                int digDelta = Math.abs(tick - getUser().getCombatProcessor().getLastBlockTick());
                int interactDelta = Math.abs(tick - this.lastInteract);

                if (digDelta < 5 && getUser().getCombatProcessor().getAttackTimer().getDelta() < 4
                        && interactDelta > 20) {

                    if (this.buffer++ > 6) {
                        this.flag(
                                "id=" + interactDelta,
                                "buffer=" + this.buffer
                        );
                    }

                } else {
                    this.buffer -= this.buffer > 0 ? .1 : 0;
                }

                break;
            }
        }
    }
}
