package se.mickelus.tetra.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ITetraTicker {
	void tick(Level level, BlockPos pos, BlockState state);
}
