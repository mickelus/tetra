package se.mickelus.tetra.blocks.geode;

import java.util.Random;

import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GeodeGenerator implements IWorldGenerator {

    private final int density = 8;
    private final int minY = 5;
    private final int rangeY = 25;

    private static Predicate<IBlockState> predicate;

    public GeodeGenerator() {
        predicate = BlockMatcher.forBlock(Blocks.STONE);

    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        for (int i = 0; i < density; i++) {
            final BlockPos blockPos = chunkPos.getBlock(random.nextInt(16),
                    minY + random.nextInt(rangeY), random.nextInt(16));
            final IBlockState state = world.getBlockState(blockPos);
            // if (state.getBlock().isReplaceableOreGen(state, world, blockPos, predicate)) { todo: add support for other stones?
            if (predicate.apply(state)) {
                world.setBlockState(blockPos, BlockGeode.instance.getDefaultState(), 16);
            }
        }
    }
}
