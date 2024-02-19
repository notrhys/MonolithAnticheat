package me.rhys.anticheat.checks.movement.invalid;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import org.bukkit.Bukkit;

@CheckInfo(name = "Invalid", type = "B", checkType = CheckType.MOVEMENT, enabled = true)
public class InvalidB extends Check {

    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement()) return;

        boolean ground = getUser().getMovementProcessor().isGround();
        boolean lastGround = getUser().getMovementProcessor().isLastGround();
        double deltaY = getUser().getMovementProcessor().getDeltaY();

        if (getUser().getActionProcessor().getLastTransactionTeleport().hasNotPassed(10)
                || getUser().getCollisionProcessor().getClimbableTicks() > 0
                || getUser().getCollisionProcessor().getSlimeTicks() > 0
                || getUser().getMovementProcessor().getLastVehicleTimer().hasNotPassed(3)
                || getUser().getCollisionProcessor().getSnowTicks() > 0
                || getUser().getCollisionProcessor().getLiquidTicks() > 0
                || getUser().getCollisionProcessor().getWebTicks() > 0
                || getUser().getCollisionProcessor().getPistionTicks() > 0
                || getUser().getCollisionProcessor().getHalfBlockTicks() > 0
                || getUser().getCollisionProcessor().getEnderPortalTicks() > 0
                || getUser().getCollisionProcessor().getMountTicks() > 0
                || getUser().getCollisionProcessor().getCauldronTicks() > 0
                || getUser().getActionProcessor().getLastTransactionVelocity().hasNotPassed()
                || getUser().getMovementProcessor().isServerValidMovement()
                || getUser().getCollisionProcessor().getHopperTicks() > 0
                || getUser().getCollisionProcessor().getBlockAboveTicks() > 0
                || getUser().getCollisionProcessor().getBlockAboveTimer().hasNotPassed(20)
                || getUser().getCollisionProcessor().getLastSoulsandTimer().hasNotPassed()) {
            this.buffer -= this.buffer > 0 ? .6 : 0;
            return;
        }

        if (deltaY < -0.0855 || deltaY == .5) {
            this.buffer -= this.buffer > 0 ? .087 : 0;
            return;
        }

        if (!ground && lastGround && deltaY != .42F) {

            if (this.buffer++ > 5) {
                this.flag(
                        "buffer=" + this.buffer,
                        "deltaY=" + deltaY
                );
            } else {
                this.buffer -= this.buffer > 0 ? .025 : 0;
            }
        }
    }
}
