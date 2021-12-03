package se.mickelus.tetra.generation;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
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
