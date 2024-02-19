package me.rhys.anticheat.util.location;

import lombok.Getter;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PastLocation {
    @Getter
    public final List<CustomLocation> previousLocations = new CopyOnWriteArrayList<>();

    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public List<CustomLocation> getEstimatedLocationFuckOff(long time, long delta, long currentMS) {
        long prevTimeStamp = currentMS - time;

        List<CustomLocation> copy = new LinkedList<>();

        int size = this.previousLocations.size();
        for (int i = 0; i < size; i++) {
            CustomLocation customLocation = this.previousLocations.get(i);
            if (MathUtil.getDelta(prevTimeStamp, customLocation.getTimeStamp()) < delta) {
                copy.add(customLocation);
            }
        }

        return copy;
    }

    public CustomLocation getPreviousLocation(long time) {
        long timeStamp = System.currentTimeMillis() - time;
        return (this.previousLocations.stream()
                .min(Comparator.comparing((loc) -> MathUtil.getDelta(timeStamp, loc.getTimeStamp())))
                .orElse(this.previousLocations.get(0)));
    }


    public List<CustomLocation> getEstimatedLocation(int currentTime, int ping, int delta) {
        int tick = currentTime - ping;

        List<CustomLocation> locs = new ArrayList<>();

        for (CustomLocation previousLocation : previousLocations) {

            if (Math.abs(tick - previousLocation.getTransactionTick()) <= delta) {
                locs.add(previousLocation.clone());
            }
        }
        return locs;
    }


    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public List<CustomLocation> getEstimatedLocation(long time, long delta, long currentMS) {
        long prevTimeStamp = currentMS - time;

        List<CustomLocation> copy = new LinkedList<>();

        int size = this.previousLocations.size();
        for (int i = 0; i < size; i++) {
            CustomLocation customLocation = this.previousLocations.get(i);
            if (MathUtil.getDelta(prevTimeStamp, customLocation.getTimeStamp()) < delta) {
                copy.add(customLocation);
            }
        }

        return copy;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public List<CustomLocation> getEstimatedLocationV2(long time, long ping, long delta) {
        List<CustomLocation> copy = new LinkedList<>();

        int size = this.previousLocations.size();
        for (int i = 0; i < size; i++) {
            CustomLocation customLocation = this.previousLocations.get(i);

            if (time - customLocation.getTimeStamp() > 0 && time - customLocation.getTimeStamp() < ping + delta) {
                copy.add(customLocation);
            }
        }

        return copy;
    }


    public List<CustomLocation> getEstimatedLocation(long time, long delta) {
        long prevTimeStamp = System.currentTimeMillis() - time;
        return this.previousLocations
                .stream()
                .filter(loc -> MathUtil.getDelta(prevTimeStamp, loc.getTimeStamp()) < delta)
                .collect(Collectors.toList());
    }

    public List<CustomLocation> getPreviousRange(long delta) {
        long stamp = System.currentTimeMillis();

        return this.previousLocations.stream()
                .filter(loc -> stamp - loc.getTimeStamp() < delta)
                .collect(Collectors.toList());
    }

    public void addLocationTick(User user, Location location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        CustomLocation customLocation = new CustomLocation(location);
        customLocation.setTransactionTick(user.pastLocationTicks);

        previousLocations.add(customLocation);
    }


    public void addLocation(Location location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        previousLocations.add(new CustomLocation(location));
    }

    public void addLocation(CustomLocation location) {
        if (previousLocations.size() >= 20) {
            previousLocations.remove(0);
        }

        previousLocations.add(location.clone());
    }
}