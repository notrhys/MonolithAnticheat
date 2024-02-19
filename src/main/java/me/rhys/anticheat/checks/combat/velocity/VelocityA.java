package me.rhys.anticheat.checks.combat.velocity;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.event.impl.events.VelocityEvent;
import org.bukkit.Bukkit;

@CheckInfo(name = "Velocity", type = "A", checkType = CheckType.COMBAT, enabled = true)
public class VelocityA extends Check {

    private boolean runCheck;
    private VelocityEvent velocityEvent;

    private double threshold;

    private int lastPost;
    private double lastPostServerY;
    private double lastPostDeltaY;

    @Override
    public void onMovementProcess() {
        if (this.velocityEvent != null && this.runCheck) {
            this.runCheck = false;

            // Ignore some stuff

            if ((getUser().getCollisionProcessor().getBlockAboveTicks() > 0
                    || getUser().getCollisionProcessor().getLiquidTicks() > 0
                    || getUser().getMovementProcessor().isServerValidMovement()
                    || getUser().getPlayer().getVehicle() != null
                    || getUser().getCollisionProcessor().getWebTicks() > 0)
                    && getUser().getCheckManager().isLoadedAll()) return;

            double deltaY = getUser().getMovementProcessor().getDeltaY();
            double serverY = (this.velocityEvent.getY() * .995f);

            // filter out any server velocity issues

            if (serverY < .225) return;

            // check if delta Y is less than velocity confirmed by the transaction

            if (deltaY >= 0 && deltaY < serverY
                    && this.lastPostServerY > .2 && this.lastPostDeltaY < this.lastPostServerY
                    && Math.abs(getUser().getMovementProcessor().getTicks() - this.lastPost) <= 1) {

                if (this.threshold > (getUser().getCheckManager().isLoadedAll() ? 3.2 : 7)) {
                    this.flag("dy=" + deltaY, "sy=" + serverY, "threshold=" + this.threshold);
                }

                this.threshold += (this.threshold < 10 ? 1.5 : 0);
            } else {
                this.threshold -= this.threshold > 0 ? .65 : 0;
            }
        }
    }

    @Override
    public void onTransactionVelocity(VelocityEvent event) {
        this.velocityEvent = event;
        this.runCheck = true;

        getUser().getConnectionProcessor().queue(() -> {
            this.lastPostServerY = event.getY();
            this.lastPostDeltaY = Math.abs(getUser().getMovementProcessor().getDeltaY());
            this.lastPost = getUser().getMovementProcessor().getTicks();
        });
    }
}
