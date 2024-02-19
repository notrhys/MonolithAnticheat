package me.rhys.anticheat.checks.movement.speed;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import org.bukkit.Bukkit;

@CheckInfo(name = "Speed", type = "C", checkType = CheckType.MOVEMENT, enabled = true)
public class SpeedC extends Check {

    private double lastDeltaXZ;
    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement()) return;

        double deltaXZ = getUser().getMovementProcessor().getDeltaXZ();
        double friction = (deltaXZ - this.lastDeltaXZ * .91f);

        if (!getUser().getMovementProcessor().isGround() && !getUser().getMovementProcessor().isLastGround()) {

            if ((getUser().getMovementProcessor().isServerValidMovement()
                    || getUser().getCollisionProcessor().getMountTicks() > 0
                    || getUser().getCollisionProcessor().getBlockAboveTicks() > 0
                    || getUser().getCollisionProcessor().getIceTicks() > 0
                    || (getUser().getCollisionProcessor().getMovingTicks() > 0
                    && (getUser().getCollisionProcessor().getStairTicks() > 0
                    || getUser().getCollisionProcessor().getSlabTicks() > 0))
                    || getUser().getCollisionProcessor().getLiquidTicks() > 0
                    || getUser().getCollisionProcessor().getClimbableTicks() > 0
                    || !getUser().getCollisionProcessor().isChunkLoaded()
                    || getUser().getMovementProcessor().getWalkSpeedReset().hasNotPassed()
                    || getUser().getCollisionProcessor().getWebTicks() > 0
                    || getUser().getActionProcessor().getBlockPlaceValidTimer().hasNotPassed(5)
                    || getUser().getMovementProcessor().getLastVehicleTimer().hasNotPassed(9)
                    || getUser().getActionProcessor().getLastTransactionVelocity().hasNotPassed(9)
                    || getUser().getActionProcessor().getLastTransactionTeleport().hasNotPassed(2)
                    || getUser().getMovementProcessor().getTicks() < 60) && getUser().getCheckManager().isLoadedAll()) {
                this.buffer -= this.buffer > 0 ? .1 : 0;
                return;
            }

            if (friction > .026) {
                if (this.buffer++ > 7) {
                    this.flag(
                            "friction=" + friction,
                            "deltaXZ=" + deltaXZ,
                            "buffer=" + this.buffer
                    );
                }
            } else {
                this.buffer -= this.buffer > 0 ? .005 : 0;
            }
        }

        this.lastDeltaXZ = deltaXZ;
    }
}
