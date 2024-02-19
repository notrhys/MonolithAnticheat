package me.rhys.anticheat.util;

import me.rhys.anticheat.util.location.BoundingBox;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RayTrace {

    Vector origin, direction;

    public RayTrace(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public static boolean intersects(Vector position, Vector min, Vector max) {
        if (position.getX() < min.getX() || position.getX() > max.getX()) {
            return false;
        } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
            return false;
        } else return !(position.getZ() < min.getZ()) && !(position.getZ() > max.getZ());
    }

    //get a point on the raytrace at X blocks away
    public Vector getPostion(double blocksAway) {
        return origin.clone().add(direction.clone().multiply(blocksAway));
    }

    //checks if a position is on contained within the position
    public boolean isOnLine(Vector position) {
        double t = (position.getX() - origin.getX()) / direction.getX();
        return position.getBlockY() == origin.getY() + (t * direction.getY()) && position.getBlockZ() == origin.getZ() + (t * direction.getZ());
    }

    //get all postions on a raytrace
    public List<Vector> traverse(double blocksAway, double accuracy) {
        List<Vector> positions = new ArrayList<>();
        for (double d = 0; d <= blocksAway; d += accuracy) {
            positions.add(getPostion(d));
        }
        return positions;
    }

    public List<Vector> traverseLinked(double blocksAway, double accuracy) {
        List<Vector> positions = new LinkedList<>();
        for (double d = 0; d <= blocksAway; d += accuracy) {
            positions.add(getPostion(d));
        }
        return positions;
    }

    public List<Vector> traverse(double skip, double blocksAway, double accuracy) {
        List<Vector> positions = new ArrayList<>();
        for (double d = skip; d <= blocksAway; d += accuracy) {
            positions.add(getPostion(d));
        }
        return positions;
    }

    public List<Block> getBlocks(World world, double blocksAway, double accuracy) {
        List<Block> blocks = new ArrayList<>();

        traverse(blocksAway, accuracy).stream().filter(vector -> vector.toLocation(world).getBlock().getType().isSolid()).forEach(vector -> blocks.add(vector.toLocation(world).getBlock()));
        return blocks;
    }

    public Vector positionOfIntersection(Vector min, Vector max, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, min, max)) {
                return position;
            }
        }
        return null;
    }
    public boolean intersects(Vector min, Vector max, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, min, max)) {
                return true;
            }
        }
        return false;
    }

    public Vector positionOfIntersection(BoundingBox boundingBox, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, boundingBox.getMinimum(), boundingBox.getMaximum())) {
                return position;
            }
        }
        return null;
    }


    public Vector positionOfIntersection(BoundingBox boundingBox, double skip, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(skip, blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, boundingBox.getMinimum(), boundingBox.getMaximum())) {
                return position;
            }
        }
        return null;
    }

    public boolean intersects(BoundingBox boundingBox, double blocksAway, double accuracy) {
        List<Vector> positions = traverseLinked(blocksAway, accuracy);
        int size = positions.size();
        Vector minVector = boundingBox.getMinimum();
        Vector maxVector = boundingBox.getMaximum();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < size; i++) {
            if (intersects(positions.get(i), minVector, maxVector)) {
                return true;
            }
        }
        return false;
    }

    public boolean intersects(BoundingBox boundingBox, double skip, double blocksAway, double accuracy) {
        List<Vector> positions = traverse(blocksAway, accuracy);
        for (Vector position : positions) {
            if (intersects(position, boundingBox.getMinimum(), boundingBox.getMaximum())) {
                return true;
            }
        }
        return false;
    }
}