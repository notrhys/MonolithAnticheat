package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInCustomPayload;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.MinecraftValues;
import me.rhys.anticheat.util.StreamUtil;
import me.rhys.anticheat.util.block.BlockUtil;
import me.rhys.anticheat.util.block.CollideEntry;
import me.rhys.anticheat.util.location.BoundingBox;
import me.rhys.anticheat.util.math.EventTimer;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Step;
import org.bukkit.material.WoodenStep;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Getter
public class CollisionProcessor extends MinecraftValues {
    private final User user;

    private int groundTicks;
    private int stairTicks;
    private int slabTicks;
    private int liquidTicks;
    private int collideHorizontalTicks;
    private int iceTicks;
    private int blockAboveTicks;
    private int slimeTicks;
    private int climbableTicks;
    private int snowTicks;
    private int lillyPadTicks;
    private int carpetTicks;
    private int nearBoatTicks;
    private int mountTicks;
    private int webTicks;
    private int halfBlockTicks;
    private boolean halfBlock;
    private int movingTicks;
    private int soulSandTicks;
    private int enderPortalTicks;
    private int pistionTicks;
    private int wallTicks;
    private int cauldronTicks;
    private int hopperTicks;

    private boolean chunkLoaded = true;

    private boolean collideHorizontal, serverGround;

    private final EventTimer halfBlockTimer;
    private final EventTimer blockAboveTimer;
    private final EventTimer lastSoulsandTimer;

    public CollisionProcessor(User user) {
        this.user = user;
        this.halfBlockTimer = new EventTimer(20, user);
        this.blockAboveTimer = new EventTimer(20, user);
        this.lastSoulsandTimer = new EventTimer(20, user);
    }

    public void handle(String type, Object packet, long now) {
        switch (type) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {

                WrappedInFlyingPacket wrappedInFlyingPacket = getUser().getMovementProcessor().getLastFlying();
                if (wrappedInFlyingPacket == null) return;

                if (this.user.getActionProcessor().getServerTeleportTimer().hasNotPassed()
                        || this.user.getActionProcessor().getLastTransactionTeleport().hasNotPassed()
                        || this.user.getMovementProcessor().getTicks() < 250) {
                    this.chunkLoaded = BlockUtil.isChunkLoaded(this.user.getMovementProcessor().getTo().
                            toLocation(this.user.getPlayer().getWorld()));
                }

                if (this.user.getPlayer().getVehicle() != null) {
                    this.mountTicks += this.mountTicks < 20 ? 1 : 0;
                } else {
                    this.mountTicks -= this.mountTicks > 0 ? 1 : 0;
                }

                boolean badVector = Math.abs(user.getMovementProcessor().getTo().toVector().length()
                        - user.getMovementProcessor().getFrom().toVector().length()) >= 1;

                this.user.boundingBox = new BoundingBox((badVector ? user.getMovementProcessor().getTo().toVector()
                        : user.getMovementProcessor().getFrom().toVector()),
                        user.getMovementProcessor().getTo().toVector())
                        .grow(0.3f, 0, 0.3f).add(0, 0, 0, 0, 1.84f, 0);

                List<CollideEntry> collideEntries = user.getBoundingBox().getCollidedBlocks(this.user);

                BlockResult blockResult = new BlockResult();
                collideEntries.forEach(blockResult::process);

                BoundingBox boundingBox = new BoundingBox((float) wrappedInFlyingPacket.getX() - 0.3F,
                        (float) wrappedInFlyingPacket.getY(), (float) wrappedInFlyingPacket.getZ() - 0.3F,
                        (float) wrappedInFlyingPacket.getX() + 0.3F,
                        (float) wrappedInFlyingPacket.getY() + 1.8F,
                        (float) wrappedInFlyingPacket.getZ() + 0.3F);


                double minX = boundingBox.minX;
                double minZ = boundingBox.minZ;

                double maxX = boundingBox.maxX;
                double maxZ = boundingBox.maxZ;

                // save performance

                boolean velocity = this.user.getActionProcessor().getLastTransactionVelocity().hasNotPassed();
                double offset = Math.abs(user.getMovementProcessor().getDeltaY() - this.BLOCK_COLLIDE);

                if (offset < .212 || velocity) {
                    blockResult.checkBlockAbove(user);
                }

                if (this.testCollision(minX) || this.testCollision(minZ)
                        || this.testCollision(maxX) || this.testCollision(maxZ)) {
                    blockResult.checkHorizontal(user);
                }

                this.processTicks(blockResult);
                break;
            }
        }
    }

    void processTicks(BlockResult blockResult) {

        if (blockResult.isHopper()) {
            this.hopperTicks += this.hopperTicks < 20 ? 1 : 0;
        } else {
            this.hopperTicks -= this.hopperTicks > 0 ? 1 : 0;
        }

        if (blockResult.isCauldron()) {
            this.cauldronTicks += this.cauldronTicks < 20 ? 1 : 0;
        } else {
            this.cauldronTicks -= this.cauldronTicks > 0 ? 1 : 0;
        }

        if (blockResult.isWall()) {
            this.wallTicks += this.wallTicks < 20 ? 1 : 0;
        } else {
            this.wallTicks -= this.wallTicks > 0 ? 1 : 0;
        }

        if (blockResult.isPiston()) {
            this.pistionTicks += this.pistionTicks < 20 ? 1 : 0;
        } else {
            this.pistionTicks -= this.pistionTicks > 0 ? 1 : 0;
        }

        if (blockResult.isEnderPortal()) {
            this.enderPortalTicks += this.enderPortalTicks < 20 ? 1 : 0;
        } else {
            this.enderPortalTicks -= this.enderPortalTicks > 0 ? 1 : 0;
        }

        if (blockResult.isSoulSand()) {
            this.lastSoulsandTimer.reset();
            this.soulSandTicks += this.soulSandTicks < 20 ? 1 : 0;
        } else {
            this.soulSandTicks -= this.soulSandTicks > 0 ? 1 : 0;
        }

        if (blockResult.isMovingUp()) {
            this.movingTicks += this.movingTicks < 50 ? 10 : 0;
        } else {
            this.movingTicks -= this.movingTicks > 0 ? 1 : 0;
        }

        if (blockResult.isHalfBlock()) {
            this.halfBlockTimer.reset();
            this.halfBlockTicks += this.halfBlockTicks < 20 ? 1 : 0;
        } else {
            this.halfBlockTicks -= this.halfBlockTicks > 0 ? 1 : 0;
        }

        this.halfBlock = blockResult.isHalfBlock();

        if (blockResult.isWeb()) {
            this.webTicks += (this.webTicks < 20 ? 1 : 0);
        } else {
            this.webTicks -= (this.webTicks > 0 ? 1 : 0);
        }

        if (blockResult.isServerGround()) {
            this.groundTicks += this.groundTicks < 20 ? 1 : 0;
        } else {
            this.groundTicks -= this.groundTicks > 0 ? 1 : 0;
        }

        if (blockResult.isStair()) {
            this.halfBlockTimer.reset();
            this.stairTicks += this.stairTicks < 20 ? 1 : 0;
        } else {
            this.stairTicks -= this.stairTicks > 0 ? 1 : 0;
        }

        if (blockResult.isSlab()) {
            this.halfBlockTimer.reset();
            this.slabTicks += this.slabTicks < 20 ? 1 : 0;
        } else {
            this.slabTicks -= this.slabTicks > 0 ? 1 : 0;
        }

        if (blockResult.isLiquid()) {
            this.liquidTicks += this.liquidTicks < 20 ? 1 : 0;
        } else {
            this.liquidTicks -= this.liquidTicks > 0 ? 1 : 0;
        }

        if (blockResult.isCollideHorizontal()) {
            this.collideHorizontalTicks += this.collideHorizontalTicks < 20 ? 1 : 0;
        } else {
            this.collideHorizontalTicks -= this.collideHorizontalTicks > 0 ? 1 : 0;
        }

        if (blockResult.isIce()) {
            this.iceTicks += this.iceTicks < 20 ? 3 : 0;
        } else {
            this.iceTicks -= this.iceTicks > 0 ? 1 : 0;
        }

        if (blockResult.isBlockAbove()) {
            this.blockAboveTimer.reset();
            this.blockAboveTicks += (this.blockAboveTicks < 20 ? 5 : 0);
        } else {
            this.blockAboveTicks -= (this.blockAboveTicks > 0 ? 1 : 0);
        }

        if (blockResult.isSlime()) {
            this.slimeTicks += (this.slimeTicks < 20 ? 5 : 0);
        } else {
            this.slimeTicks -= (this.slimeTicks > 0 ? 1 : 0);
        }

        if (blockResult.isClimbable()) {
            this.climbableTicks += (this.climbableTicks < 20 ? 1 : 0);
        } else {
            this.climbableTicks -= (this.climbableTicks > 0 ? 1 : 0);
        }

        if (blockResult.isSnow()) {
            this.snowTicks += (this.snowTicks < 20 ? 1 : 0);
        } else {
            this.snowTicks -= (this.snowTicks > 0 ? 1 : 0);
        }

        if (blockResult.isLillyPad()) {
            this.lillyPadTicks += (this.lillyPadTicks < 20 ? 1 : 0);
        } else {
            this.lillyPadTicks -= (this.lillyPadTicks > 0 ? 1 : 0);
        }

        if (blockResult.isCarpet()) {
            this.carpetTicks += (this.carpetTicks < 20 ? 1 : 0);
        } else {
            this.carpetTicks -= (this.carpetTicks > 0 ? 1 : 0);
        }

        double offset = this.user.getMovementProcessor().getTo().getY() % 0.015625;

        if (this.user.getMovementProcessor().isGround() && offset > 0 && offset < 0.009) {

            if (MathUtil.getEntitiesWithinRadius(user.getPlayer().getLocation(), 2).stream()
                    .anyMatch(entity -> entity.getType() == EntityType.BOAT)) {
                this.nearBoatTicks = 20;
            } else {
                this.nearBoatTicks -= this.nearBoatTicks > 0 ? 1 : 0;
            }
        } else {
            this.nearBoatTicks -= this.nearBoatTicks > 0 ? 1 : 0;
        }


        this.serverGround = blockResult.isServerGround();
        this.collideHorizontal = blockResult.isCollideHorizontal();
    }

    @Getter
    public static final class BlockResult {

        private boolean serverGround;

        private boolean liquid;
        private boolean stair;
        private boolean slab;
        private boolean ice;
        private boolean slime;
        private boolean climbable;
        private boolean snow;
        private boolean lillyPad;
        private boolean carpet;
        private boolean web;
        private boolean halfBlock;
        private boolean movingUp;
        private boolean soulSand;
        private boolean enderPortal;
        private boolean piston;
        private boolean cauldron;
        private boolean hopper;

        private boolean collideHorizontal;
        private boolean blockAbove;
        private boolean wall;

        private double lastBoundingBoxY;

        public void checkBlockAbove(User user) {
            this.blockAbove = StreamUtil.anyMatch(new BoundingBox(
                    (float) user.getMovementProcessor().getTo().getX(),
                    (float) user.getPlayer().getEyeLocation().getY(),
                    (float) user.getMovementProcessor().getTo().getZ(),
                    (float) user.getMovementProcessor().getTo().getX(),
                    (float) user.getPlayer().getEyeLocation().getY(),
                    (float) user.getMovementProcessor().getTo().getZ()).expand(.3, .0, .3)
                    .addXYZ(0, .4, 0).getCollidedBlocks(user), collideEntry ->
                    collideEntry.getBlock().isSolid());
        }

        public void checkHorizontal(User user) {
            this.collideHorizontal = StreamUtil.anyMatch(new BoundingBox(
                    (float) user.getMovementProcessor().getTo().getX(),
                    (float) user.getPlayer().getEyeLocation().getY(),
                    (float) user.getMovementProcessor().getTo().getZ(),
                    (float) user.getMovementProcessor().getTo().getX(),
                    (float) user.getPlayer().getEyeLocation().getY(),
                    (float) user.getMovementProcessor().getTo().getZ()).expand(1.2, .0, 1.2)
                    .getCollidedBlocks(user), collideEntry -> collideEntry.getBlock().isSolid());
        }

        public void process(CollideEntry collideEntry) {
            Material material = collideEntry.getBlock();
            Class<? extends MaterialData> blockData = material.getData();

            double minY = collideEntry.getBoundingBox().minY;


            if (material.isSolid()) {
                serverGround = true;
            }

            switch (material) {

                case HOPPER:{
                    this.hopper = true;
                    break;
                }

                case CAULDRON: {
                    this.cauldron = true;
                    break;
                }

                case COBBLE_WALL: {
                    this.wall = true;
                    break;
                }

                case PISTON_BASE:
                case PISTON_EXTENSION:
                case PISTON_MOVING_PIECE:
                case PISTON_STICKY_BASE: {
                    this.piston = true;
                    break;
                }

                case ENDER_PORTAL:
                case ENDER_PORTAL_FRAME: {
                    this.enderPortal = true;
                    break;
                }

                case SOUL_SAND: {
                    this.soulSand = true;
                    break;
                }

                case WEB: {
                    this.web = true;
                    break;
                }

                case CARPET: {
                    this.carpet = true;
                    break;
                }

                case WATER_LILY: {
                    this.lillyPad = true;
                    break;
                }

                case SNOW_BLOCK:
                case SNOW: {
                    this.snow = true;
                    break;
                }

                case VINE:
                case LADDER: {
                    this.climbable = true;
                    break;
                }

                case SLIME_BLOCK: {
                    this.slime = true;
                    break;
                }

                case ICE:
                case PACKED_ICE: {
                    this.ice = true;
                    break;
                }

                case LAVA:
                case STATIONARY_LAVA:
                case STATIONARY_WATER:
                case WATER: {
                    this.liquid = true;
                    break;
                }

                case SANDSTONE_STAIRS:
                case SMOOTH_STAIRS:
                case SPRUCE_WOOD_STAIRS:
                case ACACIA_STAIRS:
                case BIRCH_WOOD_STAIRS:
                case BRICK_STAIRS:
                case COBBLESTONE_STAIRS:
                case DARK_OAK_STAIRS:
                case JUNGLE_WOOD_STAIRS:
                case NETHER_BRICK_STAIRS:
                case QUARTZ_STAIRS:
                case RED_SANDSTONE_STAIRS:
                case WOOD_STAIRS: {
                    this.stair = true;
                    this.halfBlock = true;
                    break;
                }

                case BREWING_STAND:
                case CHEST:
                case TRAPPED_CHEST:
                case ENDER_CHEST:
                case ENCHANTMENT_TABLE:
                case IRON_BARDING:
                case FENCE:
                case FENCE_GATE:
                case ACACIA_FENCE:
                case BIRCH_FENCE:
                case ACACIA_FENCE_GATE:
                case DARK_OAK_FENCE:
                case IRON_FENCE:
                case JUNGLE_FENCE:
                case BIRCH_FENCE_GATE:
                case DARK_OAK_FENCE_GATE:
                case JUNGLE_FENCE_GATE:
                case NETHER_FENCE:
                case SPRUCE_FENCE:
                case SPRUCE_FENCE_GATE:
                case STAINED_GLASS_PANE:
                case BED_BLOCK:
                case SKULL:
                case BED: {
                    this.halfBlock = true;
                    break;
                }
            }

            if (this.slab || this.stair) {
                this.halfBlock = true;
            }

            if (material == Material.STEP || blockData == Step.class || blockData == WoodenStep.class) {
                this.halfBlock = true;
                this.slab = true;
            }

            if (this.halfBlock) {
                double y = Math.abs(this.lastBoundingBoxY - minY);
                double round = y % 1;

                if ((round == .5 || round == 1.5) || (round > .4995 && round < .732)) {
                    this.movingUp = true;
                }
            }

            this.lastBoundingBoxY = minY;
        }
    }

    boolean testCollision(double value) {
        return Math.abs(value % 0.015625D) < 1E-10;
    }
}
