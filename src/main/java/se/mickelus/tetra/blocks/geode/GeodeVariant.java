package se.mickelus.tetra.blocks.geode;

import net.minecraft.block.Block;
import net.minecraft.state.Property;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class GeodeVariant {

    /**
     * A list of biome types that this geode variant can generate in. Biomes have a primary type and if that type matches
     * any of the types in this list then the geode is allowed to generate in the biome. if this is empty or unset then
     * all biomes will be allowed.
     *
     * Mods may add additional types, but the following types are possible in vanilla:
     * hot, cold, sparse, dense, wet, dry, savanna, coniferous, jungle, spooky, dead, lush, nether, end, mushroom, void, magical,
     * rare, ocean, river, water, mesa, forest, plains, mountain, hills, swamp, sandy, snowy, wasteland, beach
     * Optional, but can only generate as child feature if not present.
     *
     * Example json: ["hot", "water", "rare"]
     */
    public String[] biomes = new String[0];

    /**
     * Min & max y levels, the geode may only generate at y levels between (inclusive) the two specified values.
     */
    public int minY;
    public int maxY;

    /**
     * The density specifies how many times the generator should attempt to generate a geode per chunk.
     */
    public int density;

    /**
     * The block that the geode can replace, and will attempt to imitiate.
     *
     * Example json for diorite:
     *   "block": "minecraft:stone",
     *   "blockMeta: 3
     */
    public Block block;
    public int blockMeta = 0;

    /**
     * The hardness of the block, should be the same as the block it replaces. Decides how fast it's mined.
     */
    float hardness;

    /**
     * The blast resistance of the block.
     */
    float resistance;

    /**
     * The metadata that will be set on the dropped geode. Useful for dropping variants of the geode item with different
     * loot tables.
     */
    public int dropMeta = 0;

    public GeodeVariant() {}
}
