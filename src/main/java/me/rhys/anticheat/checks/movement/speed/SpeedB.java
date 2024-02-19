package me.rhys.anticheat.checks.movement.speed;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.base.user.processor.ActionProcessor;
import me.rhys.anticheat.util.math.EventTimer;
import org.bukkit.Bukkit;

@CheckInfo(name = "Speed", type = "B", checkType = CheckType.MOVEMENT, enabled = true)
public class SpeedB extends Check {

    private EventTimer lastJumpTimer;
    private EventTimer lastFallTimer;

    private double threshold;
    private int totalVl;

    @Override
    public void onSetup(User user) {
        this.lastJumpTimer = new EventTimer(30, user);
        this.lastFallTimer = new EventTimer(10, user);
    }

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement() || ((getUser().getMovementProcessor().getTicks() < 50
                || getUser().getMovementProcessor().isServerValidMovement()
                || getUser().getCollisionProcessor().getMountTicks() > 0
                || getUser().getCollisionProcessor().getHalfBlockTicks() > 0
                || (getUser().getPotionProcessor().getSpeedTicks() > 0 && !getUser().getPotionProcessor().isSpeed())
                || getUser().getMovementProcessor().getWalkSpeedReset().hasNotPassed()
                || getUser().getCollisionProcessor().getBlockAboveTimer().hasNotPassed(20)
                || !getUser().getCollisionProcessor().isChunkLoaded())
                && getUser().getCheckManager().isLoadedAll())) return;

        double speed = getUser().getMovementProcessor().getDeltaXZ();
        double deltaY = getUser().getMovementProcessor().getDeltaY();

        boolean ground = getUser().getMovementProcessor().isGround();
        boolean lastGround = getUser().getMovementProcessor().isLastGround();

        double expected = 0;

        if (lastGround && !ground && deltaY >= .42f) {
            this.lastJumpTimer.reset();
        }

        if (!lastGround && ground && deltaY < -0.009) {
            this.lastFallTimer.reset();
        }

        if (ground) {

            if (getUser().getCheckManager().isLoadedAll()) {
                if (this.lastJumpTimer.hasNotPassed()) {
                    expected = .485;
                }

                if (this.lastFallTimer.hasNotPassed()) {
                    expected = .421f;
                }

                if (this.lastJumpTimer.passed() && this.lastFallTimer.passed()) {
                    expected = .299384;
                }
            }
        } else if (!lastGround) {
            expected = .365;

            if (getUser().getMovementProcessor().getBlockJumpTimer().hasNotPassed()) {
                expected += .375 + getUser().getMovementProcessor().getBlockJumpAcelleration();
            }
        }

        if (expected != 0) {

            if (getUser().getCheckManager().isLoadedAll()) {
                if (getUser().getActionProcessor().getVelocitySpeed() > 0) {
                    expected += (getUser().getActionProcessor().getVelocitySpeed() + .3);
                }

                if (getUser().getCollisionProcessor().getIceTicks() > 0) {
                    expected *= 1.5;
                }

                if (getUser().getCollisionProcessor().getSlimeTicks() > 0) {
                    expected *= 3;
                }

                if (getUser().getPotionProcessor().getSpeedTicks() > 0) {
                    expected += (getUser().getPotionProcessor().getSpeedAmplifer() * .075);
                }

                if (getUser().getActionProcessor().getServerTeleportTimer().hasNotPassed(5)) {
                    expected += (getUser().getActionProcessor().getTeleportSpeed() +
                            (getUser().getActionProcessor().getEnderPearlTimer().hasNotPassed(3) ? 15 : 1));
                }
            }

            for (ActionProcessor.VelocityQueue velocityQueue : getUser().getActionProcessor().getVelocityQueues()) {
                if (!velocityQueue.isValid() || !getUser().getCheckManager().isLoadedAll()) continue;

                expected += (Math.abs(velocityQueue.getSpeed()) + .345);
            }

            if (speed > expected && !this.ignore()) {

                if (this.threshold > (speed > .755 ? 1 : 1.2) && this.totalVl++ > 0) {
                    this.flag("speed=" + speed, "expected=" + expected);
                }

                this.threshold += (this.threshold < 20 ? 1 : 0);
            } else {
                this.threshold -= this.threshold > 0 ? .00115 : 0;
            }
        }
    }

    boolean ignore() {
        return getUser().getCollisionProcessor().getBlockAboveTicks() > 0
                || getUser().getCollisionProcessor().getStairTicks() > 0
                || getUser().getCollisionProcessor().getWallTicks() > 0
                || getUser().getCollisionProcessor().getSlabTicks() > 0;
    }
}
