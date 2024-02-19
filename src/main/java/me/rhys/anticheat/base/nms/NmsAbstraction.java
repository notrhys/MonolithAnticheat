package me.rhys.anticheat.base.nms;

import me.rhys.anticheat.base.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public abstract class NmsAbstraction {

    public abstract Block getBlock(User user, Location location);

    public abstract Material getType(World world, double x, double y, double z);

    public abstract List<PotionEffect> potionEffectList(User user);

    public abstract void sendBlockUpdate(User user, double x, double y, double z);

    public abstract int getMaxDamageTicks(User user);

    public abstract void crashPlayer(User user);
}
