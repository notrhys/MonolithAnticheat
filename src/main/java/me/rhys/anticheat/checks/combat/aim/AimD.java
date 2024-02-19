package me.rhys.anticheat.checks.combat.aim;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import org.bukkit.Bukkit;

@Experimental
@CheckInfo(name = "Aim", type = "D", checkType = CheckType.COMBAT, enabled = true)
public class AimD extends Check {

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equals(Packet.Client.POSITION_LOOK)) {

             if (getUser().getCombatProcessor().getAttackTimer().passedNoPing(2)
                     || getUser().getMovementProcessor().getTicks() < 20) return;

            float deltaYaw = getUser().getMovementProcessor().getYawDelta();

            double angel = Math.abs(deltaYaw / Math.abs(getUser().getCombatProcessor().getMouseX()));

            if (angel > 1000 && !Double.isInfinite(angel) && Math.abs(deltaYaw) > .5) {
                this.flag("angle=" + angel, "yD=" + deltaYaw);
            }
        }
    }
}
