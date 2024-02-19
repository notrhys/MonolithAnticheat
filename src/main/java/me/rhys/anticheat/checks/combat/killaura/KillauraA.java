package me.rhys.anticheat.checks.combat.killaura;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "KillAura", type = "A", checkType = CheckType.COMBAT, enabled = true)
public class KillauraA extends Check {

    private long last;
    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {
            case Packet.Client.POSITION_LOOK: {
                this.last = event.getNow();
                break;
            }

            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket wrapped = getUser().getCombatProcessor().getLastUseEntity();
                if (wrapped == null || wrapped.getEntity() == null
                        || wrapped.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) return;

                long delta = (event.getNow() - this.last);

                if (getUser().getConnectionProcessor().getPingDrop() > 100
                        || getUser().getConnectionProcessor().getSkippedPackets() > 5) {
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
