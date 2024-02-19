package me.rhys.anticheat.base.nms.impl;

import me.rhys.anticheat.base.nms.NmsAbstraction;
import me.rhys.anticheat.base.user.User;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Instance_1_8_R3 extends NmsAbstraction {

    @Override
    public Block getBlock(User user, Location location) {
        WorldServer worldServer = ((CraftWorld) user.getPlayer().getWorld()).getHandle();

        return worldServer.getChunkIfLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4) != null
                ? location.getBlock() : null;
    }

    @Override
    public Material getType(World world, double x, double y, double z) {
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        return CraftMagicNumbers.getMaterial(((CraftWorld) world).getHandle().getType(blockPosition).getBlock());
    }

    @Override
    public List<PotionEffect> potionEffectList(User user) {
        List<PotionEffect> effects = new ArrayList();

        for (Object obj : ((CraftPlayer) user.getPlayer()).getHandle().effects.values()) {
            if (obj instanceof MobEffect) {
                MobEffect handle = (MobEffect) obj;
                effects.add(new PotionEffect(PotionEffectType.getById(handle.getEffectId()), handle.getDuration(),
                        handle.getAmplifier(), handle.isAmbient(), handle.isShowParticles()));
            }
        }

        return effects;
    }

    @Override
    public void sendBlockUpdate(User user, double x, double y, double z) {
        PacketPlayOutBlockChange packetPlayOutBlockChange =
                new PacketPlayOutBlockChange((((CraftWorld) user.getPlayer().getWorld()).getHandle()),
                        new BlockPosition(x, y, z));
        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutBlockChange);
    }

    @Override
    public int getMaxDamageTicks(User user) {
        return ((CraftPlayer) user.getPlayer()).getHandle().maxNoDamageTicks;
    }

    @Override
    public void crashPlayer(User user) {
        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutExplosion(
                Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                Float.MAX_VALUE, Collections.EMPTY_LIST,
                new Vec3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)));

        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(new PacketPlayOutExplosion(
                Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                Float.MAX_VALUE, Collections.EMPTY_LIST,
                new Vec3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)));
    }
}
