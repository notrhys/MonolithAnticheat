package me.rhys.anticheat.checks.combat.aim;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "Aim", type = "B", checkType = CheckType.COMBAT, enabled = true)
public class AimB extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement() && getUser().getCombatProcessor().getAttackTimer().passedNoPing(1)
                && getUser().getMovementProcessor().getTicks() < 20) {
            double pitchDelta = getUser().getMovementProcessor().getPitchDelta();
            double yawDelta = getUser().getMovementProcessor().getAbsYawDelta();
            double deltaXZ = getUser().getMovementProcessor().getDeltaXZ();

            if (pitchDelta == 0 && yawDelta > .6 && deltaXZ > .115) {

                if (this.threshold > 12) {
                    this.flag(
                            "pd=" + pitchDelta,
                            "yd=" + yawDelta,
                            "dxz=" + deltaXZ,
                            "threshold=" + this.threshold
                    );
                }

                this.threshold += (this.threshold < 20 ? 1 : 0);
            } else {
                this.threshold -= this.threshold > 0 ? .20 : 0;
            }
        }
    }
}
