package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Function;

public class TileEntityOutcome<T extends TileEntity> implements InteractionOutcome {

    Class<T> tileEntityClass;
    Function<T, Boolean> outcome;

    public TileEntityOutcome(Class<T> tileEntityClass, Function<T, Boolean> outcome) {
        this.tileEntityClass = tileEntityClass;
        this.outcome = outcome;
    }

    @Override
    public boolean apply(World world, BlockPos pos, IBlockState blockState, PlayerEntity player, EnumHand hand, EnumFacing hitFace) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntityClass.isInstance(tileEntity)) {
            boolean result = outcome.apply(tileEntityClass.cast(tileEntity));
            world.notifyBlockUpdate(pos, blockState, blockState, 3);
            return result;
        }
        return false;
    }
}
