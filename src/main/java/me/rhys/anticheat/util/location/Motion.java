package me.rhys.anticheat.util.location;

import lombok.Getter;
import org.bukkit.util.Vector;

@Getter
public final class Motion {

    private Double motionX, motionY, motionZ;

    /**
     * Create an empty constructor if we do not want initial values for our motion.
     */
    public Motion() {

    }

    /**
     * Set an initial value for our base motion.
     */
    public Motion(final double x, final double y, final double z) {
        this.motionX = new Double(x);
        this.motionY = new Double(y);
        this.motionZ = new Double(z);
    }

    /**
     * Set an initial value for our base motion.
     */
    public Motion(final Double motionX, final Double motionY, final Double motionZ) {
        this.motionX = new Double(motionX.get());
        this.motionY = new Double(motionY.get());
        this.motionZ = new Double(motionZ.get());
    }

    public void set(final Vector vector) {
        this.motionX.set(vector.getX());
        this.motionY.set(vector.getY());
        this.motionZ.set(vector.getZ());
    }

    public void add(final Vector vector) {
        this.motionX.add(vector.getX());
        this.motionY.add(vector.getY());
        this.motionZ.add(vector.getZ());
    }

    public void apply(final MoveFlyingResult moveFlyingResult) {
        this.motionX.add(moveFlyingResult.getX());
        this.motionZ.add(moveFlyingResult.getZ());
    }

    public void round() {
        if (Math.abs(this.motionX.get()) < 0.005D) this.motionX.set(0.0D);
        if (Math.abs(this.motionY.get()) < 0.005D) this.motionY.set(0.0D);
        if (Math.abs(this.motionZ.get()) < 0.005D) this.motionZ.set(0.0D);
    }

    public double distanceSquared(final Motion other) {
        return Math.pow(this.motionX.get() - other.getMotionX().get(), 2) +
                Math.pow(this.motionY.get() - other.getMotionY().get(), 2) +
                Math.pow(this.motionZ.get() - other.getMotionZ().get(), 2);
    }

    public Motion clone() {
        return new Motion(motionX.get(), motionY.get(), motionZ.get());
    }
}