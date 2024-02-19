package me.rhys.anticheat.util.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.rhys.anticheat.util.location.BoundingBox;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public class CollideEntry {
    private final Material block;
    private final BoundingBox boundingBox;
    private final BoundingBox blockBox;
}