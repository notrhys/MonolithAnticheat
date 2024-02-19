package me.rhys.anticheat.checks.combat;

import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.world.EntityData;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.location.CustomLocation;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;

@CheckInfo(name = "Reach", type = "A", checkType = CheckType.COMBAT, enabled = true)
public class Reach extends Check {

    private boolean isFlying;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.isCombat() && this.isFlying) {
            this.isFlying = false;

            if (getUser().getMovementProcessor().isServerValidMovement()
                    || getUser().getPlayer().getVehicle() != null
                    || (getUser().getCombatProcessor().getLastAttacked() != null
                    && getUser().getCombatProcessor().getLastAttacked().getVehicle() != null)) return;

            List<CustomLocation> targetLocations = getUser().getPastLocation().getEstimatedLocation(
                    getUser().getPastLocationTicks(),
                    getUser().getConnectionProcessor().getPingTicks(), 2
            );

            CustomLocation to = getUser().getMovementProcessor().getTo().clone();
            CustomLocation from = getUser().getMovementProcessor().getFrom().clone();

            to.y += getUser().getMovementProcessor().isSneaking() ? 1.54f : 1.62f;
            from.y += getUser().getMovementProcessor().isLastSneaking() ? 1.54f : 1.62f;

            Vector toVector = to.toVector();

            RayCollision toCollision = new RayCollision(toVector, MathUtil.getDirection(to));
            RayCollision fromCollision = new RayCollision(from.toVector(), MathUtil.getDirection(from));

            int hits = 0;
            int misses = 0;
            double distance = Double.MAX_VALUE;

            World world = getUser().getPlayer().getWorld();

            for (CustomLocation toLocations : targetLocations) {
                SimpleCollisionBox simpleCollisionBox = new SimpleCollisionBox(
                        this.fromSimpleBox(this.getHitbox(
                                getUser().getCombatProcessor().getLastAttacked(),
                                toLocations,
                                world
                        )));

                Vector hitRay = toCollision.collisionPoint(simpleCollisionBox);

                if (hitRay != null) {
                    hits++;
                    distance = Math.min(distance, hitRay.distanceSquared(toVector));
                } else {
                    misses++;
                }
            }

            if (hits > 0) {
                distance = MathUtil.sqrt(distance) - .03;
            } else {
                distance = -1;
            }

            if (distance > (getUser().getCheckManager().isLoadedAll() ? 3.165 : 2.85)) {
                if (this.threshold > (getUser().getCheckManager().isLoadedAll() ? 2.5 : 0)) {
                    this.flag(
                            "distance=" + distance,
                            "threshold=" + this.threshold
                    );
                }

                this.threshold += 1.10;
            } else {
                this.threshold -= this.threshold > 0 ? .85 : 0;
            }
        }

        if (event.isMovement() && getUser().getCombatProcessor().getAttackTimer().hasNotPassedNoPing(1)) {
            this.isFlying = true;
        }
    }

    public BoundingBox fromSimpleBox(SimpleCollisionBox simpleCollisionBox) {
        return simpleCollisionBox.toBoundingBox();
    }

    SimpleCollisionBox getHitbox(Entity entity, CustomLocation loc, World world) {
        return (SimpleCollisionBox) EntityData.getEntityBox(loc.toLocation(world), entity);
    }
}
