package me.rhys.anticheat.checks.other.badpackets;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "BadPackets", type = "A", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsA extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement()) return;

        if (Math.abs(getUser().getMovementProcessor().getTo().getPitch()) > 90) {
            this.flag("pitch=" + getUser().getMovementProcessor().getTo().getPitch());
        }
    }
}
