package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LootOutcome implements InteractionOutcome {
    @Override
    public void apply(World world, BlockPos pos, IBlockState blockState, EntityPlayer player, EnumFacing hitFace) {

    }
}
