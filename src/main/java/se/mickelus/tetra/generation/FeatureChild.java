package se.mickelus.tetra.generation;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Used to specify where and which features can be generated as a part of another feature.
 * Example json:
 * {
 *     "offset": [3, 2, 6],
 *     "facing": "SOUTH",
 *     "features": [
 *         "tetra:ancient_hallway_1",
 *         "tetra:ancient_wall_1"
 *     ]
 * }
 */
public class FeatureChild {
    /**
     * The position offset relative to the parent at which this child feature should be generated.
     * Json format: [x, y, z]
     */
    public BlockPos offset = new BlockPos(0, 0 ,0);

    /**
     * The direction in which the child feature should be facing.
     * Possible json values: "DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST"
     */
    public EnumFacing facing = EnumFacing.NORTH;

    /**
     * An array of resource locations for features that can be used as child features at this position.
     * Json format: ["domain:path"]
     */
    public ResourceLocation[] features = new ResourceLocation[0];
}
