package se.mickelus.tetra.generation;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Used to specify additional details for feature generation, containing information about where and how a feature is to
 * be generated. All json files in the structures resource folder are deserialized into this class. The name of the
 * structure template defaults to the same name as the json file.
 *
 * Example json:
 * {
 *     "biomes": ["cold", "snowy"],
 *     "origin": [3, 0, 3],
 *     "probability": 0.05,
 *     "minY": 7,
 *     "maxY": 22,
 *     "integrityMin": 0.95,
 *     "children": [
 *         {
 *             "offset": [3, 1, 3],
 *             "facing": "WEST",
 *             "features": ["tetra:forged_hammer", "tetra:collapse_gravel"]
 *         },
 *         {
 *             "offset": [3, 2, 0],
 *             "features": ["tetra:forged_hallway_1", "tetra:forged_collapse_1"]
 *         }
 *     ],
 *     "loot": [
 *         {
 *             "position": [0, 0, 0],
 *             "table": "tetra:forged/chest_large"
 *         }
 *     ]
 * }
 */
public class FeatureParameters {

    /**
     * A list of biome categories that this feature can generate in. Biomes have a category and if that type matches any of the types
     * in this list then the feature is allowed to generate in the biome. Mods may add additional types, but the following types are
     * possible in vanilla:
     * hot, cold, sparse, dense, wet, dry, savanna, coniferous, jungle, spooky, dead, lush, nether, end, mushroom, void, magical,
     * rare, ocean, river, water, mesa, forest, plains, mountain, hills, swamp, sandy, snowy, wasteland, beach
     *
     * Optional, but can only generate as child feature if not present.
     *
     * Example json: ["hot", "water", "rare"]
     */
    String[] biomes = new String[0];

    /**
     * A list of resource locations denoting the dimensions that this feature can generate in. The vanilla dimensions are:
     * minecraft:overworld, minecraft:the_nether, minecraft:the_end
     *
     * Optional, defaults to the overworld if not provided
     *
     * Example json: ["minecraft:overworld", "minecraft:the_end"]
     */
    ResourceLocation[] dimensions = new ResourceLocation[] { new ResourceLocation("minecraft:overworld") };

    /**
     * The probability that this feature will generate in applicable chunks. Should be between 0 and 1, where a value of 1 would
     * cause it to generate in every chunk, 0.5 would cause it to generate in 50% of the chunks and a value of 0 would cause it to
     * never generate.
     */
    float probability = 0.01f;

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
     * The origin of this feature. Primarily used in child features, to adjust positioning and rotation relative to the
     * parent.
     *
     * Json format: [x, y, z]
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

    public FeatureParameters() {}

    public FeatureParameters(ResourceLocation location) {
        this.location = location;
    }
}
