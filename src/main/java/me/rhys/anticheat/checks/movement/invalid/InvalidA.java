package me.rhys.anticheat.checks.movement.invalid;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;

@CheckInfo(name = "Invalid", type = "A", checkType = CheckType.MOVEMENT, enabled = true)
public class InvalidA extends Check {

    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement() || getUser().getMovementProcessor().getTicks() < 45) return;

        boolean clientGround = getUser().getMovementProcessor().isGround();
        double deltaY = getUser().getMovementProcessor().getDeltaY();

        if (getUser().getActionProcessor().getLastTransactionTeleport().hasNotPassed(5)
                || getUser().getCollisionProcessor().getClimbableTicks() > 0
                || getUser().getCollisionProcessor().getSlimeTicks() > 0
                || getUser().getCollisionProcessor().getHalfBlockTicks() > 0
                || getUser().getCollisionProcessor().getWallTicks() > 0
                || getUser().getCollisionProcessor().getSnowTicks() > 0
                || getUser().getCollisionProcessor().getPistionTicks() > 0
                || getUser().getActionProcessor().getLastTransactionVelocity().hasNotPassed()
                || getUser().getCollisionProcessor().getEnderPortalTicks() > 0
                || getUser().getMovementProcessor().isServerValidMovement()
                || getUser().getCollisionProcessor().getLastSoulsandTimer().hasNotPassed()) {
            this.buffer -= this.buffer > 0 ? .3 : 0;
            return;
        }

        if (clientGround && deltaY > 0.007) {

            if (this.buffer++ > 3) {
                this.flag(
                        "buffer=" + this.buffer,
                        "dy=" + deltaY
                );
            }

        } else {
            this.buffer -= this.buffer > 0 ? .003 : 0;
        }
    }
}
