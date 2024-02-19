package me.rhys.anticheat.checks.combat.aim;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "Aim", type = "A", checkType = CheckType.COMBAT, enabled = true)
public class AimA extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (getUser().getCombatProcessor().getAttackTimer().passedNoPing(3)) return;

            long GCD = getUser().getMovementProcessor().getPitchGCD();
            boolean optifine = getUser().getOptifineProcessor().isCinematic();

            if (GCD > 0 && GCD < (getUser().getCheckManager().isLoadedAll() ? 131072L : 1072L)) {

                if (this.threshold > 26) {
                    this.flag("gcd=" + GCD, "threshold=" + this.threshold);
                }

                if (getUser().getMovementProcessor().getPitchAcelleration() > (optifine ? .4 : .1)) {
                    this.threshold += (this.threshold < 50 ? (optifine ? .95 : 2.50) : 0);
                }
            } else {
                this.threshold -= this.threshold > 0 ? (optifine ? 3 : .25) : 0;
            }
        }
    }
}
