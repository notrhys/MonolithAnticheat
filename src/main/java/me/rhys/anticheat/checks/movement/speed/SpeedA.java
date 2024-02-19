package me.rhys.anticheat.checks.movement.speed;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.base.user.processor.ActionProcessor;
import me.rhys.anticheat.util.StreamUtil;
import me.rhys.anticheat.util.location.BoundingBox;
import me.rhys.anticheat.util.location.Motion;
import me.rhys.anticheat.util.location.MoveFlyingResult;
import me.rhys.anticheat.util.minecraft.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

@CheckInfo(name = "Speed", type = "A", checkType = CheckType.MOVEMENT, enabled = true)
public class SpeedA extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (!getUser().isBelow1_8() || !event.isMovement()) return;

        WrappedInFlyingPacket wrappedInFlyingPacket = getUser().getMovementProcessor().getLastFlying();
        WrappedInFlyingPacket lastFlying = getUser().getMovementProcessor().getLastLastFlying();
        WrappedInFlyingPacket lastLastLast = getUser().getMovementProcessor().getLastLastLastFlying();

        if (wrappedInFlyingPacket == null || lastFlying == null
                || lastLastLast == null || !wrappedInFlyingPacket.isPos()
                || !lastFlying.isPos() || !lastLastLast.isPos()
                || (!getUser().getCollisionProcessor().isChunkLoaded()
                || getUser().getMovementProcessor().getPositionTicks() < 5
                || getUser().getMovementProcessor().getWalkSpeedReset().hasNotPassed()
                || getUser().getMovementProcessor().getRotationTicks() < 5
                || getUser().getMovementProcessor().isServerValidMovement())
                && getUser().getCheckManager().isLoadedAll()) {
            return;
        }

        if (getUser().getCollisionProcessor().getClimbableTicks() > 0) {
            this.threshold -= this.threshold > 0 ? .3 : 0;
            return;
        }

        final Motion realMotion = new Motion(getUser().getMovementProcessor().getMovementDeltaX(),
                0.0D, getUser().getMovementProcessor().getMovementDeltaZ());

        double attributeSpeed = getUser().getMovementProcessor().getWalkSpeed() / 2;

        final BoundingBox boundingBox = new BoundingBox((float) wrappedInFlyingPacket.getX() - 0.3F,
                (float) wrappedInFlyingPacket.getY(), (float) wrappedInFlyingPacket.getZ() - 0.3F,
                (float) wrappedInFlyingPacket.getX() + 0.3F, (float) wrappedInFlyingPacket.getY() + 1.8F,
                (float) wrappedInFlyingPacket.getZ() + 0.3F);

        final double minX = boundingBox.minX;
        final double minZ = boundingBox.minZ;

        final double maxX = boundingBox.maxX;
        final double maxZ = boundingBox.maxZ;

        if ((this.testCollision(minX) || this.testCollision(minZ)
                || this.testCollision(maxX) || this.testCollision(maxZ))
                && getUser().getCheckManager().isLoadedAll()) {
            this.threshold -= this.threshold > 0 ? .25 : 0;
            return;
        }

        if (getUser().getPotionProcessor().isSpeed()) {
            attributeSpeed += getUser().getPotionProcessor().getSpeedAmplifer() * 0.2D * attributeSpeed;
        }

        if (getUser().getPotionProcessor().isSlowness()) {
            attributeSpeed += getUser().getPotionProcessor().getSlownessAmplifer() * -.15D * attributeSpeed;
        }

        Motion predicted;
        double smallest = Double.MAX_VALUE;

        iteration:
        {

            // Yes this looks retarded but its brute forcing every possible thing.
            for (int f = -1; f < 2; f++) {
                for (int s = -1; s < 2; s++) {
                    for (int sp = 0; sp < 2; sp++) {
                        for (int jp = 0; jp < 2; jp++) {
                            for (int ui = 0; ui < 2; ui++) {
                                for (int hs = 0; hs < 2; hs++) {
                                    for (int sn = 0; sn < 2; sn++) {

                                        final boolean sprint = sp == 0;
                                        final boolean jump = jp == 0;
                                        final boolean using = ui == 0;
                                        final boolean hitSlowdown = hs == 0;

                                        final boolean ground = lastFlying.isGround();
                                        final boolean sneaking = sn == 0;

                                        float forward = f;
                                        float strafe = s;

                                        if (using) {
                                            forward *= 0.2D;
                                            strafe *= 0.2D;
                                        }

                                        if (sneaking) {
                                            forward *= (float) 0.3D;
                                            strafe *= (float) 0.3D;
                                        }

                                        forward *= 0.98F;
                                        strafe *= 0.98F;

                                        final Motion motion = new Motion(
                                                getUser().getMovementProcessor().getLastMovementDeltaX(),
                                                0.0D,
                                                getUser().getMovementProcessor().getLastMovementDeltaZ()
                                        );

                                        if (lastLastLast.isGround()) {
                                            motion.getMotionX().multiply(0.6F * 0.91F);
                                            motion.getMotionZ().multiply(0.6F * 0.91F);
                                        } else {
                                            motion.getMotionX().multiply(0.91F);
                                            motion.getMotionZ().multiply(0.91F);
                                        }

                                        if (hitSlowdown) {
                                            motion.getMotionX().multiply(0.6D);
                                            motion.getMotionZ().multiply(0.6D);
                                        }

                                        motion.round();

                                        if (jump && sprint) {
                                            final float radians = getUser().getMovementProcessor().getTo().getYaw()
                                                    * 0.017453292F;

                                            motion.getMotionX().subtract(MathHelper.sin(radians) * 0.2F);
                                            motion.getMotionZ().add(MathHelper.cos(radians) * 0.2F);
                                        }

                                        float slipperiness = 0.91F;
                                        if (ground) slipperiness = 0.6F * 0.91F;

                                        float moveSpeed = (float) attributeSpeed;
                                        if (sprint) moveSpeed += moveSpeed * 0.30000001192092896D;

                                        final float moveFlyingFriction;

                                        if (ground) {
                                            final float moveSpeedMultiplier = 0.16277136F /
                                                    (slipperiness * slipperiness * slipperiness);

                                            moveFlyingFriction = moveSpeed * moveSpeedMultiplier;
                                        } else {
                                            moveFlyingFriction = (float)
                                                    (sprint ? ((double) 0.02F + (double) 0.02F * 0.3D) : 0.02F);
                                        }

                                        motion.apply(this.moveFlying(getUser(),
                                                forward, strafe,
                                                moveFlyingFriction
                                        ));

                                        motion.getMotionY().set(0.0);

                                        final double distance = realMotion.distanceSquared(motion);

                                        if (distance < smallest) {
                                            smallest = distance;
                                            predicted = motion;

                                            if (distance < 1E-8) {
                                                break iteration;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (getUser().getActionProcessor().getServerTeleportTimer().hasNotPassedNoPing(2)) return;

        if (smallest > (getUser().getCheckManager().isLoadedAll() ? 1E-6 : 1E-1)
                && getUser().getMovementProcessor().getDeltaXZ() > .2 && !this.ignore()) {

            if (this.threshold++ > (getUser().getCheckManager().isLoadedAll() ? 12 : 200)) {

                this.flag("offset=" + smallest);
            }
        } else {
            this.threshold -= this.threshold > 0 ? .005 : 0;
        }
    }

    MoveFlyingResult moveFlying(final User data, final float moveForward, final float moveStrafe, final float friction) {
        float diagonal = moveStrafe * moveStrafe + moveForward * moveForward;

        float moveFlyingFactorX = 0.0F;
        float moveFlyingFactorZ = 0.0F;

        if (diagonal >= 1.0E-4F) {
            diagonal = MathHelper.c(diagonal);

            if (diagonal < 1.0F) {
                diagonal = 1.0F;
            }

            diagonal = friction / diagonal;

            final float strafe = moveStrafe * diagonal;
            final float forward = moveForward * diagonal;

            final float rotationYaw = data.getMovementProcessor().getTo().getYaw();

            final float f1 = MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F);
            final float f2 = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F);

            final float factorX = strafe * f2 - forward * f1;
            final float factorZ = forward * f2 + strafe * f1;

            moveFlyingFactorX = factorX;
            moveFlyingFactorZ = factorZ;
        }

        return new MoveFlyingResult(moveFlyingFactorX, moveFlyingFactorZ);
    }

    boolean ignore() {
        return getUser().getCollisionProcessor().getLiquidTicks() > 0
                || (getUser().getCollisionProcessor().isCollideHorizontal() &&
                getUser().getActionProcessor().getBlockPlaceValidTimer().hasNotPassed(10))
                || getUser().getCollisionProcessor().getSlimeTicks() > 0
                || getUser().getActionProcessor().getLastTransactionVelocity().hasNotPassed(15)
                || getUser().getCollisionProcessor().getIceTicks() > 0
                || getUser().getCollisionProcessor().getBlockAboveTicks() > 0;
    }

    boolean testCollision(double value) {
        return Math.abs(value % 0.015625D) < 1E-10;
    }
}
