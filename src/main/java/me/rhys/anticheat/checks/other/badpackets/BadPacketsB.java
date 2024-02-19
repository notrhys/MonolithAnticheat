package me.rhys.anticheat.checks.other.badpackets;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "BadPackets", type = "B", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsB extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement() || getUser().getMovementProcessor().getTicks() < 20
                || getUser().getCombatProcessor().getAttackTimer().passed(5)
                || !getUser().isSword(getUser().getPlayer().getItemInHand())) return;

        int attacked = getUser().getCombatProcessor().getLastAttackTick();
        int blocked = getUser().getCombatProcessor().getLastBlockTick();

        int tickDelta = Math.abs(attacked - blocked);

        if (attacked > 20 && blocked > 20 && tickDelta == 0) {
            this.flag();
        }
    }
}
