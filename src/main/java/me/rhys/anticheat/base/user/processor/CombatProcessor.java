package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.EvictingList;
import me.rhys.anticheat.util.math.EventTimer;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.List;

@Getter
public class CombatProcessor {
    private final User user;

    private final EventTimer attackTimer;
    private Entity lastAttacked;

    private int invalidHits;

    private int lastAttackTick;
    private int lastBlockTick;

    public int sensitivityCycles;
    private final EvictingList<Float> sensitivitySamples = new EvictingList<>(30);
    public float sensitivityValue;
    public long sensitivity;

    private int hitDelay;

    private double mouseX, mouseY;

    private WrappedInUseEntityPacket lastUseEntity;

    public CombatProcessor(User user) {
        this.user = user;
        this.attackTimer = new EventTimer(20, user);
    }

    public void handle(String type, Object packet, long now) {
        switch (type) {

            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK:
            case Packet.Client.FLYING: {
                WrappedInFlyingPacket wrapped = getUser().getMovementProcessor().getLastFlying();

                this.hitDelay = Plugin.getInstance().getNmsManager().getNmsAbstraction().getMaxDamageTicks(this.user);

                if (wrapped.isPos()) {
                    this.findSensitivity();
                }

                this.processMouse();
                break;
            }

            case Packet.Client.SETTINGS: {
                this.sensitivityCycles = 0;
                break;
            }

            case Packet.Client.BLOCK_DIG: {
                WrappedInBlockDigPacket wrapped = new WrappedInBlockDigPacket(packet, this.user.getPlayer());

                if (wrapped.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {
                    if (wrapped.getBlockPosition().getX() == 0
                            && wrapped.getBlockPosition().getY() == 0
                            && wrapped.getBlockPosition().getZ() == 0
                            && getUser().isSword(getUser().getPlayer().getItemInHand())) {
                        this.lastBlockTick = this.user.getMovementProcessor().getTicks();
                    }
                }
                break;
            }

            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket wrapped = new WrappedInUseEntityPacket(packet, this.user.getPlayer());
                this.lastUseEntity = wrapped;

                if (wrapped.getEntity() != null
                        && wrapped.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
                    this.attackTimer.reset();
                    this.lastAttacked = wrapped.getEntity();
                    this.lastAttackTick = this.user.getMovementProcessor().getTicks();
                }

                // Fixes ping-spoofing to bypass some checks
                if (Plugin.getInstance().getConfigValues().isFaggotKick() &&
                        (this.user.getConnectionProcessor().getPing() > 600L
                        || this.user.getConnectionProcessor().getPingDrop() > 150)
                        && this.user.getConnectionProcessor().getSkippedPackets() < 5) {

                    if (this.invalidHits++ > 20) {
                        this.invalidHits = 0;

                        user.kick("Tried to delay transactions.");
                    }
                } else {
                    this.invalidHits -= this.invalidHits > 0 ? 1 : 0;
                }

                break;
            }
        }
    }

    void findSensitivity() {
        if (this.sensitivityCycles > 5) return;

        float deltaPitch = getUser().getMovementProcessor().getPitchDelta();

        if (deltaPitch == 0F || Math.abs(getUser().getMovementProcessor().getTo().getPitch()) == 90F) return;

        this.sensitivitySamples.add(deltaPitch);

        if (this.sensitivitySamples.size() < 29) return;

        float gcd = sensGcd(this.sensitivitySamples);

        if (MathUtil.hasNotation(gcd)) return;

        double f1 = Math.exp(Math.log(gcd / .15 / 8) / 3);
        double sensitivityOne = ((f1 - .2) / .6) * 200;

        if (sensitivityOne > 99.9D && sensitivityOne < 100D) sensitivityOne += .1D;

        long sensitivity = (long) sensitivityOne;

        if (sensitivity >= 0L && sensitivity <= 200L) {
            this.sensitivity = sensitivity;
            this.sensitivityValue = MathUtil.sensitityValues.get(sensitivity);
            this.sensitivityCycles++;
        } else {
            this.sensitivityValue = -1F;
        }
    }

    private void processMouse() {
        double yawGCD = user.getMovementProcessor().getYawGCD() / getUser().getMovementProcessor().getGcdOffset();
        double pitchGCD = user.getMovementProcessor().getPitchGCD() / getUser().getMovementProcessor().getGcdOffset();

        this.mouseX = Math.abs((getUser().getMovementProcessor().getTo().getYaw()
                - getUser().getMovementProcessor().getFrom().getYaw()) / yawGCD);

        this.mouseY = Math.abs((getUser().getMovementProcessor().getTo().getPitch()
                - getUser().getMovementProcessor().getFrom().getPitch()) / pitchGCD);
    }

    private float sensGcd(List<Float> numbers) {
        float result = (float) numbers.toArray()[0];
        for (int i = 1; i < numbers.size(); i++) {
            result = sensGcd((float) numbers.toArray()[i], result);
        }

        return result;
    }

    private float sensGcd(float a, float b) {
        if (a <= .0001) return b;

        int quotient = getIntQuotient(b, a);
        float remainder = ((b / a) - quotient) * a;
        if (Math.abs(remainder) < Math.max(a, b) * 1E-3F) remainder = 0;
        return sensGcd(remainder, a);
    }

    private int getIntQuotient(float dividend, float divisor) {
        float ans = dividend / divisor;
        float error = Math.max(dividend, divisor) * 1E-3F;
        return (int) (ans + error);
    }
}
