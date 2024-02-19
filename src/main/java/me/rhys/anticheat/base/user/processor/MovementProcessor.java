package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInEntityActionPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.location.CustomLocation;
import me.rhys.anticheat.util.math.EventTimer;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
public class MovementProcessor {
    private final User user;

    private int ticks;

    private final CustomLocation to = new CustomLocation(0, 0, 0);
    private CustomLocation from = new CustomLocation(0, 0, 0);

    private int positionTicks, kickTicks;

    private double lastMovementDeltaX, lastMovementDeltaZ, movementDeltaX, movementDeltaZ,
            deltaXZ, deltaY, deltaYAbs, lastDeltaY, lastDeltaYAbs, absYawDelta, absPitchDelta;

    private float yawDelta, pitchDelta, lastYawDelta, lastPitchDelta, yawAcelleration, pitchAcelleration;

    private boolean ground, lastGround;

    private boolean sneaking, lastSneaking;

    private int airTicks, groundTicks, flightTicks;

    private WrappedInFlyingPacket lastFlying, lastLastFlying, lastLastLastFlying;

    private int rotationTicks;

    private double walkSpeed, lastWalkSpeed;
    private boolean serverValidMovement;

    private EventTimer lastBlockPlace, lastVehicleTimer, blockJumpTimer, walkSpeedReset;

    private double blockJumpAcelleration;

    private long pitchGCD, yawGCD;

    private final double gcdOffset = Math.pow(2.0, 24.0);

    private long lastFlyingPacket;

    private boolean crashPacket;

    private int lastBlockY = -1337;

    private int serverGroundTicks;

    private boolean sprinting, sprintingTransaction;

    private int lowTimerCounter;

    public MovementProcessor(User user) {
        this.user = user;
        this.createTimers();
    }

    void createTimers() {
        this.lastVehicleTimer = new EventTimer(20, this.user);
        this.lastBlockPlace = new EventTimer(20, this.user);
        this.blockJumpTimer = new EventTimer(25, this.user);
        this.walkSpeedReset = new EventTimer(20, this.user);
    }

    public void handle(String type, Object packet, long now) {

        Player player = this.user.getPlayer();

        switch (type) {


            /**
             * Update to the latest Atlas for Attach Packet.
             */
        /*    case Packet.Server.ATTACH: {
                user.getMovementProcessor().getLastVehicleTimer().reset();
                break;
            } */

            case Packet.Client.BLOCK_PLACE: {

                WrappedInBlockPlacePacket wrapped = new WrappedInBlockPlacePacket(packet, player);

                Material material = Plugin.getInstance().getNmsManager().getNmsAbstraction().getType(
                        player.getWorld(),
                        wrapped.getBlockPosition().getX(),
                        wrapped.getBlockPosition().getY(),
                        wrapped.getBlockPosition().getZ()
                );

                if (material != null && material.isBlock()) {
                    this.lastBlockPlace.reset();
                }

                break;
            }

            case Packet.Client.ENTITY_ACTION: {
                WrappedInEntityActionPacket wrapped = new WrappedInEntityActionPacket(packet, this.user.getPlayer());

                switch (wrapped.getAction()) {

                    case STOP_SPRINTING: {
                        this.sprinting = false;
                        user.getConnectionProcessor().queue(() -> this.sprintingTransaction = false);
                        break;
                    }

                    case START_SPRINTING: {
                        this.sprinting = true;
                        user.getConnectionProcessor().queue(() -> this.sprintingTransaction = true);
                        break;
                    }

                    case START_SNEAKING: {
                        this.sneaking = true;
                        break;
                    }

                    case STOP_SNEAKING: {
                        this.sneaking = false;
                        break;
                    }
                }

                break;
            }

            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                WrappedInFlyingPacket wrappedInFlyingPacket = new WrappedInFlyingPacket(packet, player);

                this.handleLargePackets(wrappedInFlyingPacket);
                this.handleLevelChange(wrappedInFlyingPacket);
                this.handleLowTimer(now);

                if (user.getPlayer().getAllowFlight()) {
                    this.flightTicks = 20;
                } else {
                    this.flightTicks--;
                }

                if (this.user.getCollisionProcessor().isServerGround()) {
                    this.serverGroundTicks += (this.serverGroundTicks < 20 ? 1 : 0);
                } else {
                    this.serverGroundTicks -= (this.serverGroundTicks > 0 ? 1 : 0);
                }

                if (wrappedInFlyingPacket.isGround()
                        && user.getActionProcessor().getLastTransactionVelocity().passed(20)) {
                    user.getActionProcessor().reduceVelocitySpeed(0, true);
                }

                boolean serverValidMove = getUser().getPlayer().getGameMode() == GameMode.CREATIVE
                        || getUser().getPlayer().isFlying()
                        || getUser().getPlayer().getAllowFlight();

                if (serverValidMove) {
                    this.serverValidMovement = true;
                }

                if (this.user.getCollisionProcessor().isServerGround() &&
                        this.user.getCollisionProcessor().getGroundTicks() > 7
                        && !serverValidMove) {
                    this.serverValidMovement = false;
                }

                this.lastWalkSpeed = this.walkSpeed;

                this.walkSpeed = getUser().getPlayer().getWalkSpeed();

                if (this.walkSpeed != this.lastWalkSpeed) {
                    this.walkSpeedReset.reset();
                }

                boolean hasPos = wrappedInFlyingPacket.isPos();
                boolean hasLook = wrappedInFlyingPacket.isLook();

                this.lastLastLastFlying = this.lastLastFlying;
                this.lastLastFlying = this.lastFlying;
                this.lastFlying = wrappedInFlyingPacket;

                this.yawDelta = this.to.getYaw() - this.from.getYaw();
                this.absYawDelta = Math.abs(this.yawDelta);

                this.pitchDelta = this.to.getPitch() - this.from.getPitch();
                this.absPitchDelta = Math.abs(this.pitchDelta);

                this.pitchGCD = MathUtil.gcd(
                        (long) (pitchDelta * this.gcdOffset), (long) (this.lastPitchDelta * this.gcdOffset)
                );

                this.yawGCD = MathUtil.gcd(
                        (long) (yawDelta * this.gcdOffset), (long) (this.lastYawDelta * this.gcdOffset)
                );

                this.yawAcelleration = Math.abs(this.yawDelta - this.lastYawDelta);
                this.pitchAcelleration = Math.abs(this.pitchDelta - this.lastPitchDelta);

                this.lastYawDelta = this.yawDelta;
                this.lastPitchDelta = this.pitchDelta;

                this.from = this.to.clone();

                if (wrappedInFlyingPacket.isGround()) {
                    this.airTicks = 0;
                    this.groundTicks += this.groundTicks < 20 ? 1 : 0;
                } else {
                    this.groundTicks = 0;
                    this.airTicks += this.airTicks < 20 ? 1 : 0;
                }

                this.lastSneaking = this.sneaking;

                if (hasPos) {
                    double x = wrappedInFlyingPacket.getX();
                    double y = wrappedInFlyingPacket.getY();
                    double z = wrappedInFlyingPacket.getZ();
                    boolean ground = wrappedInFlyingPacket.isGround();

                    this.to.setX(wrappedInFlyingPacket.getX());
                    this.to.setY(wrappedInFlyingPacket.getY());
                    this.to.setZ(wrappedInFlyingPacket.getZ());
                    this.to.setClientGround(ground);

                    this.lastDeltaY = this.deltaY;
                    this.lastDeltaYAbs = this.deltaYAbs;

                    this.lastGround = this.ground;
                    this.ground = wrappedInFlyingPacket.isGround();

                    this.deltaY = this.to.getY() - this.from.getY();
                    this.deltaYAbs = Math.abs(this.deltaY);

                    this.lastMovementDeltaX = this.movementDeltaX;
                    this.lastMovementDeltaZ = this.movementDeltaZ;

                    this.movementDeltaX = (this.to.getX() - this.from.getX());
                    this.movementDeltaZ = (this.to.getZ() - this.from.getZ());

                    this.positionTicks += (this.positionTicks < 20 ? 1 : 0);
                } else {
                    this.positionTicks -= this.positionTicks > 0 ? 1 : 0;
                }

                if (hasLook) {
                    this.from.setPitch(this.to.getPitch());
                    this.from.setYaw(this.to.getYaw());

                    this.to.setPitch(wrappedInFlyingPacket.getPitch());
                    this.to.setYaw(wrappedInFlyingPacket.getYaw());

                    this.rotationTicks += this.rotationTicks < 20 ? 1 : 0;
                }

                this.deltaXZ = MathUtil.sqrt(this.movementDeltaX * this.movementDeltaX
                        + this.movementDeltaZ * this.movementDeltaZ);

                this.ticks++;

                // run some checks after this processor has ticked

                if (!Plugin.getInstance().isLagging()) {
                    this.user.getCheckManager().getChecks().forEach(Check::onMovementProcess);
                }

                this.lastFlyingPacket = now;

                this.handleLargeMovement();
                break;
            }
        }
    }

    private void handleLargeMovement() {
        if (this.ticks > 25 && this.deltaXZ > .995
                && this.user.getActionProcessor().getLastTransactionTeleport().passed(2)
                && this.user.getActionProcessor().getServerTeleportTimer().passed(2)
                && this.user.getCollisionProcessor().getNearBoatTicks() > 0 && !Plugin.getInstance().isLagging()) {

            for (ActionProcessor.VelocityQueue velocityQueue : getUser().getActionProcessor().getVelocityQueues()) {
                if (!velocityQueue.isValid()) continue;
                if (velocityQueue.getSpeed() > .552) return;
            }

            this.kickTicks = 3;
        }

        if (this.kickTicks-- > 0) {

            if (this.user.getActionProcessor().getLastTransactionTeleport().hasNotPassed(2) ||
                    this.user.getActionProcessor().getServerTeleportTimer().hasNotPassed(2)) {
                this.kickTicks = 0;

                return;
            }

            if (this.kickTicks < 1) {
                this.user.kick("Tried to move too quickly");
            }
        }
    }

    private void handleLargePackets(WrappedInFlyingPacket wrapped) {
        if (!wrapped.isPos() || this.crashPacket) return;

        double x = wrapped.getX();
        double y = wrapped.getY();
        double z = wrapped.getZ();

        if (Math.abs(x) > 3.0E7D || Math.abs(y) > 3.0E7D || Math.abs(z) > 3.0E7D) {
            this.crashPacket = true;
            this.user.kick("Tried crashing anti-cheat thread.");
        }
    }

    private void handleLevelChange(WrappedInFlyingPacket wrapped) {
        if (!wrapped.isPos()) return;

        if (this.blockJumpTimer.passed()) {
            this.blockJumpAcelleration = 0;
        }

        if (this.ground && wrapped.getY() % 0.015625 == 0) {

            this.lastBlockY = (int) wrapped.getY();
        } else {
            double delta = wrapped.getY() - this.lastBlockY;

            if (delta == 1.0) {
                this.blockJumpTimer.reset();

                this.blockJumpAcelleration += this.blockJumpAcelleration < .225 ? .06 : 0;
            }
        }
    }

    private void handleLowTimer(long now) {

        if (!user.isBelow1_8()) return;

        long delta = (now - this.lastFlyingPacket);

        if (delta > 100L && user.getConnectionProcessor().getSkippedPackets() < 2
                && Math.abs(user.getConnectionProcessor().getPingDrop()) < 20 && !Plugin.getInstance().isLagging()) {

            if (this.lowTimerCounter++ > (user.getConnectionProcessor().getPing() > 300 ? 7 : 2)) {
                this.lowTimerCounter = 0;

                if (Plugin.getInstance().getConfigValues().isFaggotKick()) {
                    this.user.kick("Delayed packets (" + delta + "ms)");
                }
            }

        } else {
            this.lowTimerCounter = 0;
        }
    }
}
