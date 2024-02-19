package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.RunUtils;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.block.BlockUtil;
import me.rhys.anticheat.util.location.CustomLocation;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
public class GhostBlockProcessor {
    private final User user;
    private CustomLocation customLocation;

    private int cooldownTicks;
    private int triggerTicks;
    private int teleportingTicks;
    private long lastFlying;

    private int teleports;
    private long lastTeleports;

    private double lastPredictionY;
    private int liquidGhostChecks;
    private double liquidThreshold;
    private int liquidTotal;

    private float fallDamage;
    private boolean setFallDamage;
    private boolean didTeleportForDamage;

    private int checkIgnoreTeleports;

    private final long reset = TimeUnit.SECONDS.toMillis(6L);

    public GhostBlockProcessor(User user) {
        this.user = user;
    }

    public void handle(String type, Object packet, long now) {

        if (Plugin.getInstance().isLagging()) {
            return;
        }

        switch (type) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {

                boolean clientGround = this.user.getMovementProcessor().isGround();
                boolean serverGround = this.user.getCollisionProcessor().isServerGround();
                double moduloY = user.getMovementProcessor().getTo().getY() % 0.015625;

                this.handlePrediction(moduloY);

                // reset fall-damage
                if (serverGround && moduloY == 0) {


                    if (this.setFallDamage && this.didTeleportForDamage) {
                        this.setFallDamage = false;
                        this.didTeleportForDamage = false;

                        if (getUser().getCollisionProcessor().getLiquidTicks() < 1) {
                            this.user.getPlayer().setFallDistance(this.fallDamage);
                        }

                        this.fallDamage = 0;
                    }

                    if (this.user.getMovementProcessor().getServerGroundTicks() > 10) {
                        this.fallDamage = 0;
                    }
                }

                // set fall-damage
                if (!serverGround && moduloY > 0 && this.user.getMovementProcessor().getDeltaY() < 0) {
                    this.fallDamage += (this.user.getMovementProcessor().getDeltaXZ() + .5);
                    this.setFallDamage = true;
                }

                // kick if tried to exploit

                if (this.teleports > 2 && this.user.getMovementProcessor().getDeltaYAbs() > .8) {
                    this.teleports = 0;
                    this.user.kick("Ignoring teleports from ghost-blocks");
                }

                if ((now - this.lastTeleports) > this.reset) {
                    this.teleports -= this.teleports > 0 ? 1 : 0;
                }

                // last ground location
                long delta = (now - this.lastFlying);

                if (serverGround && this.user.getMovementProcessor().getTicks() % 5 == 0
                        && this.teleportingTicks < 1 && delta > 0 && delta < 100L) {
                    this.customLocation = this.user.getMovementProcessor().getTo().clone();
                }

                // ghost block
                if (!serverGround && clientGround && this.triggerTicks < 1 && this.teleportingTicks < 15) {
                    this.triggerTicks = 5;
                }

                if (this.triggerTicks > 0) {

                    // ignore
                    if (this.user.getCollisionProcessor().getSnowTicks() > 0
                            || this.user.getCollisionProcessor().getLillyPadTicks() > 0
                            || this.user.getCollisionProcessor().getCarpetTicks() > 0
                            || this.user.getCollisionProcessor().getNearBoatTicks() > 0
                            || this.user.getCollisionProcessor().isHalfBlock()
                            || this.user.getCollisionProcessor().getClimbableTicks() > 0) {
                        this.triggerTicks = 0;
                        return;
                    }

                    // any possible mis-detections
                    if (serverGround && this.teleportingTicks < 1) {
                        this.triggerTicks = 0;
                        return;
                    }

                    // once fully valid teleport
                    if (this.triggerTicks-- < 2 && this.customLocation != null) {
                        this.teleportingTicks = 5;

                        double dy = getUser().getMovementProcessor().getDeltaYAbs();

                        if (dy > 0) {
                            this.didTeleportForDamage = true;
                            RunUtils.task(() -> this.user.getPlayer().teleport(
                                    this.getGroundLocation(user.getMovementProcessor().getTo().toLocation(getUser()
                                            .getPlayer().getWorld())), PlayerTeleportEvent.TeleportCause.UNKNOWN));
                        } else {
                            RunUtils.task(() -> this.user.getPlayer().teleport(this.customLocation.toLocation(
                                    this.user.getPlayer().getWorld()), PlayerTeleportEvent.TeleportCause.UNKNOWN));
                        }

                        this.checkIgnoreTeleports = 5;

                        this.lastTeleports = now;
                        this.teleports++;

                        if (Plugin.getInstance().getConfigValues().isFaggotKick() &&
                                this.teleports > 5) {
                            this.teleports = 0;

                            user.kick("Too many ghost-block teleports.");
                        }
                    } else {
                        if (this.customLocation == null) {
                            RunUtils.task(() -> this.user.getPlayer().teleport(this.getGroundLocation(this.
                                    user.getPlayer().getLocation())));
                        }
                    }
                }

                this.teleportingTicks -= this.teleportingTicks > 0 ? 1 : 0;
                this.lastFlying = now;
                break;
            }
        }
    }

    private void handlePrediction(double moduloY) {
        double deltaY = user.getMovementProcessor().getDeltaY();

        // Wait
        if (this.user.getMovementProcessor().getTicks() < 60) return;

        // Reset on server ground
        if (this.user.getCollisionProcessor().isServerGround() && moduloY == 0) {
            this.liquidThreshold = 0;
            this.liquidTotal = 0;
        }

        //Prediction calculations
        double predictedDist = (this.lastPredictionY - 0.08D) * 0.9800000190734863D;

        //Check if the prediction is rounded, this is done in the client but we need to make it here as well
        if (Math.abs(predictedDist) <= 0.005D) {
            predictedDist = 0;
        }

        //Delta between the last 2 ticks of the prediction in the air
        double prediction = Math.abs(deltaY - predictedDist);

        boolean clientGround = user.getMovementProcessor().isGround();
        boolean lastClientGround = user.getMovementProcessor().isLastGround();

        //Reset when on client ground, if they spoof their loss
        if (clientGround || lastClientGround) {
            this.liquidGhostChecks = 0;
        }

        //Check 2 ticks client ground
        if (!clientGround && !lastClientGround) {

            //Check if the prediction is over a specific amount
            if (prediction > 1E-12) {

                //Buffer to be safe
                if (this.liquidThreshold++ > 4) {
                    this.liquidThreshold = 0;

                    boolean movingUp = getUser().getCollisionProcessor().getMovingTicks() > 0
                            && getUser().getMovementProcessor().getDeltaY() < 0.05;

                    // Exempts
                    if (getUser().getCollisionProcessor().getClimbableTicks() > 0
                            || getUser().getCollisionProcessor().getLiquidTicks() > 0
                            || (getUser().getCollisionProcessor().getHalfBlockTicks() > 0 && movingUp)
                            || getUser().getCollisionProcessor().getStairTicks() > 0
                            || !getUser().getCollisionProcessor().isChunkLoaded()
                            || getUser().getMovementProcessor().isServerValidMovement()
                            || getUser().getActionProcessor().getLastTransactionTeleport().hasNotPassed()
                            || getUser().getActionProcessor().getLastTransactionVelocity().hasNotPassed()
                            || getUser().getCollisionProcessor().getMountTicks() > 0
                            || (getUser().getCollisionProcessor().getSlabTicks() > 0 && movingUp)) {
                        this.liquidThreshold = 0;
                        this.liquidTotal = 0;
                        return;
                    }


                    double absDelta = Math.abs(deltaY);

                    if ((deltaY > 0 && deltaY < .115) || (deltaY < 0 && absDelta > 0.005 && absDelta < .115)
                            || (deltaY > .07 && deltaY < .2)) {

                        if (this.liquidTotal++ > 5) return;

                        World world = user.getPlayer().getWorld();
                        Location location = user.getMovementProcessor().getTo().toLocation(world);

                        int radius = 2;

                        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
                            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {

                                    Plugin.getInstance().getNmsManager().getNmsAbstraction().sendBlockUpdate(
                                            user, x, y, z
                                    );
                                }
                            }
                        }
                    }
                }
            } else {
                this.liquidThreshold -= this.liquidThreshold > 0 ? .50 : 0;
            }
        } else {
            this.liquidThreshold = 0;
        }

        this.lastPredictionY = deltaY;
    }

    private Location getGroundLocation(Location location) {
        int i = 0;

        while (!Objects.requireNonNull(BlockUtil.getBlock(location)).getRelative(BlockFace.DOWN).getType().isSolid()
                && location.getY() != 0) {
            if (i++ > 20) {
                break;
            }
            location.add(0, -1, 0);
        }

        if (location.getY() == 0) {
            return location;
        }

        location.add(0, .2, 0);
        return location;
    }
}
