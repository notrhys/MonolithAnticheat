package me.rhys.anticheat.checks.movement.fly;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.processor.ActionProcessor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

@CheckInfo(name = "Fly", type = "A", checkType = CheckType.MOVEMENT, enabled = true)
public class FlyA extends Check {

    private final double DRAG = .9800000190734863D;
    private final double MIN_XZ = .09;
    private final double ZERO_THREE = .003016261509046103D;
    private final double MAX_PREDICTION = 3.780309398848658E-14;
    private final double AIR_MOTION = .6349722830977471;

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isMovement()) {

            WrappedInFlyingPacket wrappedInFlyingPacket = getUser().getMovementProcessor().getLastFlying();
            if ((wrappedInFlyingPacket == null || !wrappedInFlyingPacket.isPos()
                    || getUser().getMovementProcessor().isServerValidMovement()
                    || !getUser().getCollisionProcessor().isChunkLoaded()
                    || ((getUser().getCollisionProcessor().isCollideHorizontal()
                    || getUser().getCollisionProcessor().getWebTicks() > 0
                    || getUser().getCollisionProcessor().getGroundTicks() > 0)
                    && getUser().getActionProcessor().getLastTransactionVelocity().hasNotPassed()))
                    && getUser().getCheckManager().isLoadedAll()) return;

            if (getUser().getWorldChangeEvent().hasNotPassed()) {
                this.threshold = 0;
                return;
            }

            double deltaY = getUser().getMovementProcessor().getDeltaY();
            double deltaXZ = getUser().getMovementProcessor().getDeltaXZ();

            boolean ground = getUser().getMovementProcessor().isGround();
            boolean lastGround = getUser().getMovementProcessor().isLastGround();

            int airTicks = getUser().getMovementProcessor().getAirTicks();

            double predictionY = (getUser().getMovementProcessor().getLastDeltaY() - 0.08) * this.DRAG;

            if (ground || lastGround || getUser().getCollisionProcessor().getBlockAboveTimer().hasNotPassed()) {
                this.threshold -= this.threshold > 0 ? .005 : 0;
                return;
            }

            double deltaPrediction = Math.abs(deltaY - predictionY);

            double expected;

            boolean zeroThree = false;

            // fixes 0.003
            if (deltaXZ < this.MIN_XZ) {
                expected = 0.004995;
                zeroThree = true;
            } else {
                if (Math.abs(predictionY) < 0.005) {
                    expected = this.ZERO_THREE;
                } else {
                    expected = this.MAX_PREDICTION;
                }
            }

            // fixes 8th and 9th tick
            if (airTicks > 7 && airTicks < 10) {
                expected = this.AIR_MOTION;
            }

            // velocity accounting

            for (ActionProcessor.VelocityQueue velocityQueue : getUser().getActionProcessor().getVelocityQueues()) {
                if (!velocityQueue.isValidPing(3) || !getUser().getCheckManager().isLoadedAll()) continue;

                expected += (Math.abs(velocityQueue.getY()) + .625);
            }

            // teleport accounting

            if (getUser().getActionProcessor().getServerTeleportTimer().hasNotPassedNoPing(5)
                    && getUser().getActionProcessor().getTeleport() != null) {
                expected += (getUser().getActionProcessor().getTeleport().getSY() * 1.5);
            }

            if (deltaPrediction > expected && airTicks > 3) {

                if (zeroThree && this.isNearGround()) {
                    this.threshold = 0;
                    return;
                }

                if (this.ignore()) {
                    this.threshold = 0;
                    return;
                }

                if (this.threshold++ > 5) {
                    this.flag("pred=" + predictionY, "dPred=" + deltaPrediction, "expected=" + expected);
                }
            } else {
                this.threshold -= this.threshold > 0 ? .05 : 0;
            }
        }
    }

    boolean ignore() {
        return getUser().getCollisionProcessor().getLiquidTicks() > 0
                || getUser().getCollisionProcessor().getClimbableTicks() > 0;
    }

    private boolean isNearGround() {
        double max = 0.3;

        World world = getUser().getPlayer().getWorld();
        Location location = getUser().getMovementProcessor().getTo().toLocation(getUser().getPlayer().getWorld());

        for (double x = -max; x <= max; x += max) {
            for (double z = -max; z <= max; z += max) {
                for (double y = 0; y < 3; y += 1) {

                    Location currentLocation = location.clone().add(x, -y, z);

                    if (Plugin.getInstance().getNmsManager().getNmsAbstraction().getType(world,
                            currentLocation.getX(), currentLocation.getY(), currentLocation.getZ()) != Material.AIR) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
