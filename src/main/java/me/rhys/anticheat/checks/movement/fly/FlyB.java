package me.rhys.anticheat.checks.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.processor.ActionProcessor;
import org.bukkit.Bukkit;

@CheckInfo(name = "Fly", type = "B", checkType = CheckType.MOVEMENT, enabled = true)
public class FlyB extends Check {

    private double lastY;
    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {
            WrappedInFlyingPacket wrappedInFlyingPacket = getUser().getMovementProcessor().getLastFlying();

            if (wrappedInFlyingPacket == null
                    || !wrappedInFlyingPacket.isPos()) {
                return;
            }

            if (getUser().getWorldChangeEvent().hasNotPassed()) {
                this.buffer = 0;
                return;
            }

            double yPos = wrappedInFlyingPacket.getY();
            double deltaY = Math.abs(yPos - this.lastY);

            // last time they touched the ground
            if (wrappedInFlyingPacket.isGround()) {
                this.lastY = yPos;
            }

            if (getUser().getMovementProcessor().getLastBlockPlace().hasNotPassed()
                    || getUser().getMovementProcessor().isServerValidMovement()
                    || getUser().getCollisionProcessor().getSnowTicks() > 0
                    || getUser().getCollisionProcessor().getLiquidTicks() > 0
                    || getUser().getCollisionProcessor().getSlimeTicks() > 0
                    || getUser().getCollisionProcessor().getMountTicks() > 0
                    || getUser().getCollisionProcessor().getClimbableTicks() > 0
                    || getUser().getMovementProcessor().getTicks() < 150
                    || !getUser().getCollisionProcessor().isChunkLoaded()
                    || (getUser().getCombatProcessor().getHitDelay() != 20
                    && getUser().getActionProcessor().getLastTransactionVelocity().hasNotPassed(20))) {
                this.lastY = wrappedInFlyingPacket.getY();

                if (getUser().getCollisionProcessor().getLiquidTicks() > 0) {
                    this.buffer = 0;
                }
                return;
            }

            // falling
            if ((getUser().getMovementProcessor().getTo().getY() < getUser().getMovementProcessor()
                    .getFrom().getY())) return;

            double maxDelta = 1.8;

            // teleporting

            if (getUser().getActionProcessor().getLastTransactionTeleport().hasNotPassed(3)) {
                ActionProcessor.TeleportQueue teleportQueue = getUser().getActionProcessor().getLastTeleportQueue();

                if (teleportQueue != null) {
                    maxDelta += (teleportQueue.getY() + 2);
                }
            }

            // velocity
            for (ActionProcessor.VelocityQueue velocityQueue : getUser().getActionProcessor().getVelocityQueues()) {
                if (!velocityQueue.isValid()) continue;

                maxDelta += (Math.abs(velocityQueue.getY()) + .550);
            }

            // jump boost
            if (getUser().getPotionProcessor().getJumpTicks() > 0) {
                maxDelta += (getUser().getPotionProcessor().getJumpAmplifer() * .115);
            }

            // velocity accounting
            for (ActionProcessor.VelocityQueue velocityQueue : getUser().getActionProcessor().getVelocityQueues()) {

                if (velocityQueue.isValidPing(5)) {
                    maxDelta += Math.abs(velocityQueue.getY()) + .3;
                }
            }

            // Invalid
            if (deltaY > maxDelta && !getUser().getCollisionProcessor().isServerGround()) {

                if (this.buffer++ > 5) {
                    this.flag("dY= " + deltaY);
                }
            } else {
                this.buffer -= this.buffer > 0 ? .10 : 0;
            }
        }
    }
}
