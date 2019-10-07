package se.mickelus.tetra.blocks.geode;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GeodeGenerator implements IWorldGenerator {
    public GeodeGenerator() { }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        GeodeVariant[] variants = BlockGeode.instance.variants;
        for (int i = 0; i < variants.length; i++) {
            if (isvalidBiome(variants[i], world, chunkPos)) {
                generateGeodes(variants[i], world, chunkPos, random, i);
            }
        }
    }

    private void generateGeodes(GeodeVariant variant, World world, ChunkPos chunkPos, Random random, int index) {
        for (int i = 0; i < variant.density; i++) {
            final BlockPos blockPos = chunkPos.getBlock(random.nextInt(16),
                    getY(variant, random), random.nextInt(16));
            final BlockState state = world.getBlockState(blockPos);
            if (isValidBlockState(variant, state)) {
                world.setBlockState(blockPos, BlockGeode.instance.getStateFromMeta(index), 16);
            }
        }
    }

    private boolean isValidBlockState(GeodeVariant variant, BlockState blockState) {
        return variant.block.equals(blockState.getBlock())
                && variant.blockMeta == blockState.getBlock().getMetaFromState(blockState);
    }

    private boolean isvalidBiome(GeodeVariant variant, World world, ChunkPos chunkPos) {
        if (variant.biomes.length == 0) {
            return true;
        }

        Biome biome = world.getBiome(chunkPos.getBlock(0, 0, 0));
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);
        return types.stream().anyMatch(type ->
                Arrays.stream(variant.biomes).anyMatch(biomeName ->
                        biomeName.equals(type.getName().toLowerCase())));
    }

    private int getY(GeodeVariant variant, Random random) {
        if (variant.maxY > variant.minY) {
            return variant.minY + random.nextInt(variant.maxY - variant.minY);
        }

        return variant.minY;
    }
}
