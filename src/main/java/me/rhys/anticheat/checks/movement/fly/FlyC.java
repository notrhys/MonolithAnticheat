package me.rhys.anticheat.checks.movement.fly;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.processor.ActionProcessor;

@Experimental
@CheckInfo(name = "Fly", type = "C", checkType = CheckType.MOVEMENT, enabled = true, ban = false)
public class FlyC extends Check {


    private final double DRAG = .9800000190734863D;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            if (!getUser().getCollisionProcessor().isChunkLoaded()
                    || getUser().getCollisionProcessor().isHalfBlock()
                    || ignore()
                    || getUser().getCollisionProcessor().getMountTicks() > 0
                    || (getUser().getMovementProcessor().getFlightTicks() -
                    getUser().getConnectionProcessor().getPingTicks()) > 0
                    || getUser().getMovementProcessor().getWalkSpeedReset().hasNotPassed(5)
                    || getUser().getPlayer().getAllowFlight()
                    || getUser().getMovementProcessor().getTicks() < 60
                    || getUser().getActionProcessor().getServerTeleportTimer().hasNotPassed(10)
                    || getUser().getMovementProcessor().getDeltaXZ() < 0.2
                    && getUser().getPotionProcessor().getJumpTicks() > 0
                    || getUser().getCollisionProcessor().getBlockAboveTicks() > 0
                    || getUser().getMovementProcessor().getLastBlockPlace().hasNotPassed(3)
                    || getUser().getCollisionProcessor().getSlimeTicks() > 0
                    || getUser().getCollisionProcessor().getClimbableTicks() > 0
                    || getUser().getCollisionProcessor().getWebTicks() > 0
                    || getUser().getCollisionProcessor().getHalfBlockTimer().hasNotPassed(20)
                    || getUser().getCollisionProcessor().getPistionTicks() > 0) {
                this.threshold = 0;
                return;
            }

            if (getUser().getWorldChangeEvent().hasNotPassed()) {
                this.threshold = 0;
                return;
            }

            double deltaY = getUser().getMovementProcessor().getDeltaY();
            double lastDeltaY = getUser().getMovementProcessor().getLastDeltaY();

            boolean ground = getUser().getMovementProcessor().isGround();
            boolean lastGround = getUser().getMovementProcessor().isLastGround();

            double prediction = (lastDeltaY - 0.08D) * DRAG;

            if (!ground && lastGround && deltaY > 0.0) {
                prediction = 0.42F + (getUser().getPotionProcessor().getJumpAmplifer() * 0.1F);
            }

            double total = Math.abs(deltaY - prediction);

            double maxTotal = 0.005;

            for (ActionProcessor.VelocityQueue velocityQueue : getUser().getActionProcessor().getVelocityQueues()) {
                if (!velocityQueue.isValidPing(3)) continue;

                maxTotal += (Math.abs(velocityQueue.getY()) + .625);
            }

            if (!ground) {
                if (total > maxTotal && Math.abs(prediction) > 0.005) {
                    if (++threshold > 6) {
                        flag("t="+total + " p="+prediction);
                    }
                } else {
                    threshold -= Math.min(threshold, 0.000000001);
                }
            }
        }
    }

    boolean ignore() {
        return getUser().getCollisionProcessor().getLiquidTicks() > 0
                || getUser().getCollisionProcessor().getClimbableTicks() > 0;
    }
}