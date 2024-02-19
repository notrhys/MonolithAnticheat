package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.event.impl.events.VelocityEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.EvictingList;
import me.rhys.anticheat.util.math.EventTimer;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Material;

import java.util.List;

@Getter
public class ActionProcessor {
    private final User user;

    private final EventTimer lastTransactionVelocity;
    private final EventTimer lastTransactionTeleport;
    private final EventTimer serverTeleportTimer;
    private final EventTimer blockPlaceTimer, blockPlaceValidTimer;
    private final EventTimer enderPearlTimer;

    private final List<VelocityQueue> velocityQueues = new EvictingList<>(20);
    private final List<TeleportQueue> teleportQueues = new EvictingList<>(20);
    private final List<ServerTeleport> teleportMovementQueues = new EvictingList<>(20);

    private TeleportQueue lastTeleportQueue;
    private VelocityQueue lastVelocityQueue;

    private boolean expectTeleport;
    private PreTeleportData teleportData;
    private ClientPositionTeleport teleport;

    private double teleportSpeed;
    private double velocitySpeed, preVelocitySpeed;

    public ActionProcessor(User user) {
        this.user = user;
        this.lastTransactionVelocity = new EventTimer(20, user);
        this.lastTransactionTeleport = new EventTimer(20, user);
        this.serverTeleportTimer = new EventTimer(20, user);
        this.enderPearlTimer = new EventTimer(20, user);
        this.blockPlaceTimer = new EventTimer(20, user);
        this.blockPlaceValidTimer = new EventTimer(20, user);
    }

    public void handle(String type, Object packet, long now) {
        switch (type) {

            case Packet.Client.BLOCK_PLACE: {
                WrappedInBlockPlacePacket wrapped = new WrappedInBlockPlacePacket(packet, this.user.getPlayer());

                Material placedMaterial = Plugin.getInstance().getNmsManager().getNmsAbstraction().getType(
                        user.getPlayer().getWorld(),
                        wrapped.getBlockPosition().getX(),
                        wrapped.getBlockPosition().getY(),
                        wrapped.getBlockPosition().getZ()
                );

                if (placedMaterial != null && placedMaterial != Material.AIR) {
                    this.blockPlaceValidTimer.reset();
                }

                this.blockPlaceTimer.reset();
                break;
            }

            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.LOOK: {
                this.handleQueueCheck();
                break;
            }

            // Aka the C06 when the player teleports so we can fall back to this if transaction fails
            case Packet.Client.POSITION_LOOK: {

                if (this.expectTeleport) {
                    this.expectTeleport = false;

                    WrappedInFlyingPacket wrappedInFlyingPacket = new WrappedInFlyingPacket(packet,
                            this.user.getPlayer());

                    double x = wrappedInFlyingPacket.getX();
                    double y = wrappedInFlyingPacket.getY();
                    double z = wrappedInFlyingPacket.getZ();

                    double expectedX = this.teleportData.getX();
                    double expectedY = this.teleportData.getY();
                    double expectedZ = this.teleportData.getZ();

                    double offsetX = Math.abs(x - expectedX);
                    double offsetY = Math.abs(y - expectedY);
                    double offsetZ = Math.abs(z - expectedZ);

                    this.teleport = new ClientPositionTeleport(x, y, z, expectedX, expectedY,
                            expectedZ, offsetX, offsetY, offsetZ);

                    this.serverTeleportTimer.reset();
                }

                break;
            }

            case Packet.Server.POSITION: {
                WrappedOutPositionPacket wrappedOutPositionPacket = new WrappedOutPositionPacket(packet,
                        this.user.getPlayer());

                this.teleportData = new PreTeleportData(
                        wrappedOutPositionPacket.getX(),
                        wrappedOutPositionPacket.getY(),
                        wrappedOutPositionPacket.getZ()
                );

                this.expectTeleport = true;

                double speed = MathUtil.hypot(
                        (user.getMovementProcessor().getTo().getX() - wrappedOutPositionPacket.getX()),
                        (user.getMovementProcessor().getTo().getZ() - wrappedOutPositionPacket.getZ()));

                this.teleportSpeed = speed;

                this.teleportMovementQueues.add(new ServerTeleport(speed, user.getMovementProcessor().getTicks(),
                        getUser()));

                this.user.getConnectionProcessor().queue(() ->
                        this.handleServerPosition(speed, wrappedOutPositionPacket));
                break;
            }

            case Packet.Server.ENTITY_VELOCITY: {
                WrappedOutVelocityPacket wrappedOutVelocityPacket = new WrappedOutVelocityPacket(packet,
                        this.user.getPlayer());

                if (wrappedOutVelocityPacket.getId() == this.user.getPlayer().getEntityId()) {

                    this.user.getConnectionProcessor().queue(() ->
                            this.handleTransactionVelocity(wrappedOutVelocityPacket));
                }

                break;
            }
        }
    }

    void handleServerPosition(double speed, WrappedOutPositionPacket wrappedOutPositionPacket) {
        this.teleportQueues.add(this.lastTeleportQueue = new TeleportQueue(
                wrappedOutPositionPacket.getX(),
                wrappedOutPositionPacket.getY(),
                wrappedOutPositionPacket.getZ(),
                speed,
                getUser().getMovementProcessor().getTicks(),
                getUser()
        ));

        this.lastTransactionTeleport.reset();
        this.user.getCheckManager().getChecks().forEach(check -> check.onTransactionTeleport(wrappedOutPositionPacket));
    }

    void handleTransactionVelocity(WrappedOutVelocityPacket wrappedOutVelocityPacket) {

        VelocityQueue velocityQueue;

        this.velocityQueues.add(velocityQueue = (new VelocityQueue(
                wrappedOutVelocityPacket.getX(),
                wrappedOutVelocityPacket.getY(),
                wrappedOutVelocityPacket.getZ(),
                getUser().getMovementProcessor().getTicks(),
                getUser()
        )));

        this.velocitySpeed += Math.abs(velocityQueue.getSpeed());
        this.lastTransactionVelocity.reset();

        // handle velocity checks
        VelocityEvent velocityEvent = new VelocityEvent(
                wrappedOutVelocityPacket.getX(),
                wrappedOutVelocityPacket.getY(),
                wrappedOutVelocityPacket.getZ(),
                user.getMovementProcessor().getDeltaY(),
                this.user.getMovementProcessor().getTicks()
        );

        this.user.getCheckManager().getChecks().forEach(check -> check.onTransactionVelocity(velocityEvent));
    }

    void handleQueueCheck() {

        // flush queues to reduce cpu usage overall..

        if (this.getLastTransactionVelocity().passed(60)) {
            this.getVelocityQueues().clear();
        }

        if (this.getLastTransactionTeleport().passed(60) && !this.expectTeleport) {
            this.getTeleportQueues().clear();
        }
    }

    @Getter @AllArgsConstructor
    public static final class ServerTeleport {
        private final double speed;
        private final int tick;
        private final User user;

        public int getDelta() {
            return Math.abs(this.user.getMovementProcessor().getTicks() - tick);
        }
    }

    @Getter @AllArgsConstructor
    public static final class ClientPositionTeleport {
        private final double sX, sY, sZ;
        private final double cX, cY, cZ;
        private final double offsetX, offsetY, offsetZ;
    }

    @Getter @AllArgsConstructor
    public static final class PreTeleportData {
        private final double x;
        private final double y;
        private final double z;
    }

    @Getter @AllArgsConstructor
    public static final class TeleportQueue {
        private final double x;
        private final double y;
        private final double z;
        private final double teleportSpeed;
        private final int tick;
        private final User user;

        public boolean isValid() {
            return (user.getMovementProcessor().getTicks() - tick) < 2;
        }

        public boolean isLongValid() {
            return (user.getMovementProcessor().getTicks() - tick) <= 3;
        }
    }

    @Getter @AllArgsConstructor
    public static final class VelocityQueue {
        private final double x;
        private final double y;
        private final double z;
        private final int tick;
        private final User user;

        public boolean isValid() {
            return (user.getMovementProcessor().getTicks() - tick) < 2 + this.user.getConnectionProcessor().getPingTicks();
        }

        public boolean isValidPing() {
            return (user.getMovementProcessor().getTicks() - tick) < (2 + this.user.getConnectionProcessor().getPingTicks());
        }

        public boolean isValidPing(int customTick) {
            return (user.getMovementProcessor().getTicks() - tick) < (customTick + this.user.getConnectionProcessor().getPingTicks());
        }

        public boolean isValid(int customTick) {
            return (user.getMovementProcessor().getTicks() - tick) < customTick;
        }

        public double getSpeed() {
            return MathUtil.sqrt(x * x + z * z);
        }
    }

    public void reduceVelocitySpeed(double amount, boolean set) {

        if (set) {
            this.velocitySpeed = 0;
        } else {
            this.velocitySpeed -= this.velocitySpeed > 0 ? amount : 0;
        }
    }
}
