package se.mickelus.tetra.generation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * A loot entry used in a generation feature. During feature generation the inventory at the given position will have
 * contents added from the given loot table.
 * Example json:
 * {
 *     position: [1, 4, 1],
 *     table: "tetra:ancient/chest_small"
 * }
 */
public class FeatureLoot {
    /**
     * The position where loot should be generated, this is relative to the feature and there should be tile entity
     * implementing IInventory at the position.
     * Json format: [x, y, z]
     */
    BlockPos position = new BlockPos(0, 0, 0);

    /**
     * The resourcelocation for the loot table.
     * Json format: "domain:path"
     */
    ResourceLocation table;
}
