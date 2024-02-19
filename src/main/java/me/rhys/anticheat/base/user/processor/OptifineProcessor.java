package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInCustomPayload;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.math.EventTimer;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@Getter
public class OptifineProcessor {
    private final User user;

    private double lastYawAcelleration, lastPitchAcelleration;
    private double lastX, lastY, lastLastY;
    private int ticks;

    private final EventTimer lastValid;

    @Getter
    private boolean cinematic;

    @Getter
    private boolean inTick;

    public OptifineProcessor(User user) {
        this.user = user;
        this.lastValid = new EventTimer(80, user);
    }

    public void handle(String type, Object packet, long now) {
        switch (type) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                this.process(now);
                break;
            }
        }
    }

    public void process(long now) {

        float yawAcelleration = getUser().getMovementProcessor().getYawDelta();
        float pitchAcelleration = getUser().getMovementProcessor().getPitchDelta();

        // They are not rotating
        if (yawAcelleration < 0.002 || pitchAcelleration < 0.002) return;

        // Deltas between the current acelleration and last
        double x = Math.abs(yawAcelleration - this.lastYawAcelleration);
        double y = Math.abs(pitchAcelleration - this.lastPitchAcelleration);

        // Deltas between last X & Y
        double deltaX = Math.abs(x - this.lastX);
        double deltaY = Math.abs(y - this.lastY);

        // Pitch delta change
        double pitchChangeAcelleration = Math.abs(this.lastLastY - deltaY);
        this.inTick = false;

        // we have to check something different for pitch due to it being a little harder to check for being smooth
        if (x < .04 || y < .04
                || (pitchAcelleration > .08 && pitchChangeAcelleration > 0
                && !MathUtil.isScientificNotation(pitchChangeAcelleration)
                && pitchChangeAcelleration < .0855)) {

            if (this.isInvalidGCD()) {
                this.ticks += (this.ticks < 20 ? 1 : 0);
            }
        } else {
            this.ticks -= this.ticks > 0 ? 1 : 0;
        }

        this.lastLastY = deltaY;
        this.lastX = x;
        this.lastY = y;

        this.lastYawAcelleration = yawAcelleration;
        this.lastPitchAcelleration = pitchAcelleration;

        this.cinematic = this.ticks > 5 || this.lastValid.hasNotPassedNoPing();
    }

    boolean isInvalidGCD() {
        return this.user.getMovementProcessor().getPitchGCD() < 131072L;
    }
}
