package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.Collection;

public interface IInteractiveBlock {
    public BlockInteraction[] getPotentialInteractions(World world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolType> tools);
}
