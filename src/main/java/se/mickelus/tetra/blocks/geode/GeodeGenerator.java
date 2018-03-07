package se.mickelus.tetra.blocks.geode;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GeodeGenerator implements IWorldGenerator {

    private final int density = 4;
    private final int minY = 5;
    private final int rangeY = 25;

    public GeodeGenerator() {
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        for (int i = 0; i < density; i++) {
            final BlockPos blockPos = chunkPos.getBlock(random.nextInt(15), minY + random.nextInt(rangeY), random.nextInt(15));
            world.setBlockState(blockPos, BlockGeode.instance.getDefaultState(), 2);
        }
    }
}
