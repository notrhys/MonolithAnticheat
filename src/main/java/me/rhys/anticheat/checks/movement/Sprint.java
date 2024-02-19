package me.rhys.anticheat.checks.movement;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.location.CustomLocation;
import me.rhys.anticheat.util.math.MathUtil;

@Experimental
@CheckInfo(name = "Sprint", type = "A", checkType = CheckType.MOVEMENT, enabled = true)
public class Sprint extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement()
                || ((!getUser().getMovementProcessor().isSprintingTransaction()
                || getUser().getMovementProcessor().isServerValidMovement()
                || getUser().getCollisionProcessor().getPistionTicks() > 0
                || !getUser().getMovementProcessor().isGround()
                || getUser().getMovementProcessor().getGroundTicks() < 15
                || getUser().getCollisionProcessor().getStairTicks() > 0
                || getUser().getCollisionProcessor().getSlabTicks() > 0
                || !getUser().getMovementProcessor().isLastGround())
                && getUser().getCheckManager().isLoadedAll())) return;

        CustomLocation from = getUser().getMovementProcessor().getFrom().clone();

        double angle = MathUtil.getAngle(getUser().getMovementProcessor().getTo().clone().add(
                        -from.getX(), -from.getY(), -from.getZ()).toVector(),
                MathUtil.getDirection(from));

        if (angle > 1.59 && getUser().getMovementProcessor().getLastBlockPlace().passed(20)) {
            double xz = getUser().getMovementProcessor().getDeltaXZ();

            if (xz > .210995) {

                if (this.threshold++ > 6) {
                    this.flag("xz=" + this.threshold);
                }

            } else {
                this.threshold -= this.threshold > 0 ? .02 : 0;
            }
        } else {
            this.threshold -= this.threshold > 0 ? .05 : 0;
        }
    }
}
