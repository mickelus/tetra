package se.mickelus.tetra.generation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GenerationFeature {

    /**
     * A list of biome types that this feature can generate in. Biomes have a primary type and if that type matches any
     * of the types in this list then the feature is allowed to generate in the biome. Mods may add additional types,
     * but the following types are possible in vanilla:
     * hot, cold, sparse, dense, wet, dry, savanna, coniferous, jungle, spooky, dead, lush, nether, end, mushroom, void,
     * magical, rare, ocean, river, water, mesa, forest, plains, mountain, hills, swamp, sandy, snowy, wasteland, beach
     */
    String[] biomes = new String[0];

    /**
     * The minimum Y level that this feature is allowed to generate at.
     */
    int minY = 4;

    /**
     * The maximum Y level that this feature is allowed to generate at.
     */
    int maxY = 4;

    /**
     * Min and max values for structure integrity, a random value between min and max will be used on generation.
     * Must be between 0.0 and 1.0, and min should not be larger than max. Does not propagate to children.
     */
    float integrityMin = 1;
    float integrityMax = 1;

    /**
     *
     */
    public BlockPos origin = new BlockPos(0, 0, 0);

    /**
     * Indicates if this feature can be rotated or mirrored when placed. Useful when feature has to align with cardinal
     * directions, or when using items from other mods which do not support rotations and mirroring.
     */
    boolean transform = true;

    /**
     * Used to specify which features can generated as a part of this feature, and how/where to do so.
     */
    FeatureChild[] children = new FeatureChild[0];

    /**
     * Used to specify where and which loot can be generated within this feature.
     */
    FeatureLoot[] loot = new FeatureLoot[0];


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // generated fields below
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Automatically set resource location used to identify this feature and its template
    public ResourceLocation location;

    public GenerationFeature() {}
}
