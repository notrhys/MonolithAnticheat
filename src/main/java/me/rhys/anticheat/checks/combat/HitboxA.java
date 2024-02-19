package me.rhys.anticheat.checks.combat;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.PlayerUtil;
import me.rhys.anticheat.util.RayTrace;
import me.rhys.anticheat.util.StreamUtil;
import me.rhys.anticheat.util.location.BoundingBox;
import me.rhys.anticheat.util.location.CustomLocation;
import me.rhys.anticheat.util.location.PastLocation;
import me.rhys.anticheat.util.math.MathUtil;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@CheckInfo(name = "Hitbox", type = "A", checkType = CheckType.COMBAT, enabled = true)
public class HitboxA extends Check {

    private double buffer;
    private final PastLocation pastLocation = new PastLocation();

    @Override
    public void onPacket(PacketEvent event) {

        if (getUser().getMovementProcessor().isServerValidMovement()) return;

        if (event.isMovement() && getUser().getCombatProcessor().getAttackTimer().hasNotPassed(100)
                && getUser().getCombatProcessor().getLastAttacked() != null) {
            this.pastLocation.addLocation(getUser().getCombatProcessor().getLastAttacked().getLocation());
        }

        if (event.isCombat()) {
            WrappedInUseEntityPacket wrapped = getUser().getCombatProcessor().getLastUseEntity();

            if (wrapped == null
                    || wrapped.getAction() != WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK
                    || !PlayerUtil.isOnline(wrapped.getEntity().getUniqueId())) {
                return;
            }

            User target = Plugin.getInstance().getUserManager().getUser(wrapped.getPlayer());

            if (target == null || getUser().getPlayer().getLocation()
                    .distance(getUser().getCombatProcessor().getLastAttacked().getLocation()) < 2) {
                return;
            }

            Location eyeLocation = getEyeLocation(getUser(), event.getNow());
            Vector eyeVector = eyeLocation.toVector();
            Vector direction = MathUtil.getDirection(eyeLocation);
            AtomicReference<Double> rayTracedDistance = new AtomicReference<>((double) 0);
            double expand = 0.125D;

            int count = StreamUtil.filter(this.pastLocation.getEstimatedLocation(Math.max(60,
                            getUser().getConnectionProcessor().getPing()), 200,
                    event.getNow()), cl -> {

                BoundingBox box = fromPlayerLocation(cl);

                box.expand(expand, expand, expand);

                double rayTraced = box.rayTrace(eyeVector, direction,
                        (getUser().getMovementProcessor().getDeltaXZ() < .185 ? 3.01 : 3.26));

                rayTracedDistance.updateAndGet(v -> v + rayTraced);
                return rayTraced != -1;
            }).size();

            if (getUser().getCombatProcessor().getLastAttacked() == null) return;

            double entityDeltaY = Math.abs(Plugin.getInstance().getUserManager().getUser(getUser().getCombatProcessor()
                    .getLastAttacked().getUniqueId()).getMovementProcessor().getTo().getY()
                    - getUser().getMovementProcessor().getTo().getY());

            if (count == 0 && entityDeltaY < 4) {
                if (this.buffer++ > 4) {
                    this.flag("count=" + count, "buffer=" + this.buffer);
                }
            } else {
                this.buffer -= this.buffer > 0 ? .45 : 0;
            }
        }
    }

    private BoundingBox fromPlayerLocation(CustomLocation customLocation) {

        double x = customLocation.getX();
        double y = customLocation.getY();
        double z = customLocation.getZ();

        double minX = x - 0.3D;
        double minZ = z - 0.3D;

        double maxX = x + 0.3D;
        double maxY = y + 1.8D;
        double maxZ = z + 0.3D;

        return new BoundingBox((float) minX, (float) y, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
    }

    private Location getEyeLocation(User user, long now) {
        Location location = user.getMovementProcessor().getTo().toLocation(user.getPlayer().getWorld());

        return new Location(
                location.getWorld(),
                location.getX(),
                location.getY() + getEyeHeight(user),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    private float getEyeHeight(User user) {
        float height = 1.62F;

        if (user.getMovementProcessor().isSneaking()) {
            height -= user.isBelow1_8() ? .08F : .35000002384F;
        }

        return height;
    }
}
