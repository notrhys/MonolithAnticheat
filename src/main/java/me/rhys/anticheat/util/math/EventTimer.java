package me.rhys.anticheat.util.math;

import lombok.Getter;
import me.rhys.anticheat.base.user.User;

@Getter
public class EventTimer {
    private int tick;
    private final int max;
    private final User user;

    public EventTimer(int max, User user) {
        this.tick = 0;
        this.max = max;
        this.user = user;
    }

    public int getDelta() {
        return (user.getMovementProcessor().getTicks() - tick);
    }

    public boolean hasNotPassed(int ctick) {
        int maxTick = user.getConnectionProcessor().getPingTicks() + ctick;
        int connectedTick = this.user.getMovementProcessor().getTicks();

        return ((connectedTick - tick) <= maxTick);
    }

    public boolean hasNotPassedNoPing(int cTick) {
        return ((this.user.getMovementProcessor().getTicks() - tick) <= cTick);
    }

    public boolean hasNotPassed() {
        return ((this.user.getMovementProcessor().getTicks() - tick) <=
                (user.getConnectionProcessor().getPingTicks() + this.max));
    }

    public boolean hasNotPassedNoPing() {
        return ((this.user.getMovementProcessor().getTicks() - tick) <= this.max);
    }

    public boolean passed() {
        return ((this.user.getMovementProcessor().getTicks() - tick) >=
                (this.max + user.getConnectionProcessor().getPingTicks()));
    }

    public boolean passed(int cTick) {
        return ((this.user.getMovementProcessor().getTicks() - tick) >=
                (cTick + user.getConnectionProcessor().getPingTicks()));
    }

    public boolean passedNoPing() {
        return ((this.user.getMovementProcessor().getTicks() - tick) >= this.max);
    }

    public boolean passedNoPing(int cTick) {
        return ((this.user.getMovementProcessor().getTicks() - tick) >= cTick);
    }

    public void reset() {
        this.tick = this.user.getMovementProcessor().getTicks();
    }

    public void fullReset() {
        this.tick = -1;
    }
}
