package me.rhys.anticheat.checks.combat.aim;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import org.bukkit.Bukkit;

@Experimental
@CheckInfo(name = "Aim", type = "C", checkType = CheckType.COMBAT, enabled = true)
public class AimC extends Check {

    private double buffer;

    @Override
    public void onPacket(PacketEvent event) {
        if (!event.isMovement() || getUser().getCombatProcessor().getAttackTimer().passed(2)) return;

        float sensitivityValue = getUser().getCombatProcessor().getSensitivityValue();
        long sensitivity = getUser().getCombatProcessor().getSensitivity();

        if (sensitivity == -1L || sensitivity == 67L) return;

        float yawDelta = getUser().getMovementProcessor().getYawDelta();
        float pitchDelta = getUser().getMovementProcessor().getPitchDelta();

        if (yawDelta < .5 || pitchDelta < .5) return;

        float f = sensitivityValue * .6F + .2F;
        float f1 = f * f * f * 1.2F;

        float packetPitch = getUser().getMovementProcessor().getTo().getPitch();
        float lastPitch = getUser().getMovementProcessor().getFrom().getPitch();

        float packetYaw = getUser().getMovementProcessor().getTo().getYaw();
        float lastYaw = getUser().getMovementProcessor().getFrom().getYaw();

        float yaw = packetYaw;
        float pitch = packetPitch;

        yaw += .000001F;
        pitch += .000001F;

        float deltaYaw = yaw - lastYaw;
        deltaYaw -= deltaYaw % f1;
        yaw = lastYaw + deltaYaw;

        float deltaPitch = pitch - lastPitch;
        deltaPitch -= deltaPitch % f1;
        pitch = lastPitch + deltaPitch;

        float pitchOffset = Math.abs(pitch - packetPitch);
        float yawOffset = Math.abs(yaw - packetYaw);
        float offset = Math.abs(pitchOffset - yawOffset);

        if (offset == 0) {
            if (this.buffer++ > 10) {
                this.flag(
                        "buffer=" + this.buffer,
                        "deltaYaw=" + deltaYaw,
                        "deltaPitch= " + deltaPitch
                );
            }
        } else {
            this.buffer -= this.buffer > 0 ? .050 : 0;
        }
    }
}
