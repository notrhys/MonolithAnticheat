package me.rhys.anticheat.util.location;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

@Getter
public class CustomLocation {
    public double x;
    public double y;
    public double z;
    public double xz;
    public float yaw, pitch, clampedYaw;
    private long timeStamp;
    private World world;
    private boolean clientGround;

    @Setter
    private long nextPacketTime;

    @Setter
    private int transactionTick;

    public CustomLocation(World world, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(World world, double x, double y, double z, float yaw, float pitch, long now) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double z) {
        this.x = x;
        this.z = z;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double y, double z, long now) {
        this.x = x;
        this.y = y;
        this.z = z;

        timeStamp = now;
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch, World world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;

        timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch, World world, boolean clientGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.clientGround = clientGround;

        timeStamp = System.currentTimeMillis();
    }


    public CustomLocation(double x, double y, double z, float yaw, float pitch, long timeStamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = timeStamp;
    }

    public CustomLocation(Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();

        this.timeStamp = System.currentTimeMillis();
    }

    public CustomLocation(CustomLocation loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();

        this.timeStamp = System.currentTimeMillis();
    }

    public long getDelta() {
        return this.nextPacketTime - this.timeStamp;
    }

    public CustomLocation clone() {
        return new CustomLocation(x, y, z, yaw, pitch, timeStamp);
    }

    public CustomLocation clone2() {
        return new CustomLocation(x, y, z, yaw, pitch, timeStamp);
    }

    public CustomLocation add(double x, double y, double z) {
        return new CustomLocation(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Vector getDirection() {
        Vector vector = new Vector();
        double rotX = this.getYaw();
        double rotY = this.getPitch();
        vector.setY(-Math.sin(Math.toRadians(rotY)));
        double xz = Math.cos(Math.toRadians(rotY));
        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));
        return vector;
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }


    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setClientGround(boolean clientGround) {
        this.clientGround = clientGround;
    }

    public boolean isClientGround() {
        return clientGround;
    }
}