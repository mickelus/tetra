package se.mickelus.tetra.blocks.geode;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class GeodeGenerator implements IWorldGenerator {

	private final int minY = 10;
	private final int maxY = 40;

	private WorldGenMinable[] generators;

	public GeodeGenerator() {
		generators = new WorldGenMinable[] {
			new WorldGenMinable(BlockGeode.instance.getDefaultState(), 1, BlockMatcher.forBlock(Blocks.STONE))
		};
	}


	@Override
	public void generate(final Random random, final int chunkX, final int chunkZ, final World world,
			final IChunkGenerator chunkGenerator,
			final IChunkProvider chunkProvider) {

		final ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

		for (int x = 0; x < 16; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = 0; z < 16; z++) {
					final BlockPos blockPos = chunkPos.getBlock(x, y, z);
					Arrays.stream(generators).forEach((generator -> generator.generate(world, random, blockPos)));
				}
			}
		}
	}
}
