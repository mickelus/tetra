package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface InteractionOutcome {
    public boolean apply(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace);
}
