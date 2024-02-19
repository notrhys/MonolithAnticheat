package me.rhys.anticheat.base.nms.impl;

import me.rhys.anticheat.base.nms.NmsAbstraction;
import me.rhys.anticheat.base.user.User;
import net.minecraft.server.v1_7_R4.MobEffect;
import net.minecraft.server.v1_7_R4.PacketPlayOutBlockChange;
import net.minecraft.server.v1_7_R4.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.util.CraftMagicNumbers;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Instance_1_7_R4 extends NmsAbstraction {

    @Override
    public Block getBlock(User user, Location location) {
        WorldServer worldServer = ((CraftWorld) user.getPlayer().getWorld()).getHandle();

        return worldServer.getChunkIfLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4) != null
                ? location.getBlock() : null;
    }

    @Override
    public Material getType(World world, double x, double y, double z) {
        return CraftMagicNumbers.getMaterial(((CraftWorld) world).getHandle().getType((int) x, (int) y, (int) z));
    }

    @Override
    public List<PotionEffect> potionEffectList(User user) {
        List<PotionEffect> effects = new ArrayList();

        for (Object obj : ((CraftPlayer) user.getPlayer()).getHandle().effects.values()) {
            if (obj instanceof MobEffect) {
                MobEffect handle = (MobEffect) obj;

                effects.add(new PotionEffect(this.getById(handle.getEffectId()), handle.getDuration(),
                        handle.getAmplifier(), handle.isAmbient()));
            }
        }

        return effects;
    }

    @Override
    public void sendBlockUpdate(User user, double x, double y, double z) {
        PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange((int) x, (int) y, (int) z,
                ((CraftWorld) user.getPlayer().getWorld()).getHandle());
        ((CraftPlayer) user.getPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutBlockChange);
    }

    @Override
    public int getMaxDamageTicks(User user) {
        return ((CraftPlayer) user.getPlayer()).getHandle().maxNoDamageTicks;
    }

    @Override
    public void crashPlayer(User user) {
        //
    }

    //1.7.10 doesn't have this for some builds so we have to add it our self's.
    PotionEffectType getById(int id) {
        switch (id) {
            case 1: {
                return PotionEffectType.SPEED;
            }

            case 2: {
                return PotionEffectType.SLOW;
            }

            case 3: {
                return PotionEffectType.FAST_DIGGING;
            }

            case 4: {
                return PotionEffectType.SLOW_DIGGING;
            }

            case 5: {
                return PotionEffectType.INCREASE_DAMAGE;
            }

            case 6: {
                return PotionEffectType.HEAL;
            }

            case 7: {
                return PotionEffectType.HARM;
            }

            case 8: {
                return PotionEffectType.JUMP;
            }

            case 9: {
                return PotionEffectType.CONFUSION;
            }

            case 10: {
                return PotionEffectType.REGENERATION;
            }

            case 11: {
                return PotionEffectType.DAMAGE_RESISTANCE;
            }

            case 12: {
                return PotionEffectType.FIRE_RESISTANCE;
            }

            case 13: {
                return PotionEffectType.WATER_BREATHING;
            }

            case 14: {
                return PotionEffectType.INVISIBILITY;
            }

            case 15: {
                return PotionEffectType.BLINDNESS;
            }

            case 16: {
                return PotionEffectType.NIGHT_VISION;
            }

            case 17: {
                return PotionEffectType.HUNGER;
            }

            case 18: {
                return PotionEffectType.WEAKNESS;
            }

            case 19: {
                return PotionEffectType.POISON;
            }

            case 20: {
                return PotionEffectType.WITHER;
            }

            case 21: {
                return PotionEffectType.HEALTH_BOOST;
            }

            case 22: {
                return PotionEffectType.ABSORPTION;
            }

            case 23: {
                return PotionEffectType.SATURATION;
            }
        }

        return null;
    }
}
