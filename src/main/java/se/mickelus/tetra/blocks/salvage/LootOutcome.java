package se.mickelus.tetra.blocks.salvage;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.block.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.util.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Hand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class LootOutcome implements InteractionOutcome {
    @Override
    public boolean apply(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction hitFace) {

        return false;
    }
}
