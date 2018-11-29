package se.mickelus.tetra.blocks.salvage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class TileEntityOutcome<T extends TileEntity> implements InteractionOutcome {

    Class<T> tileEntityClass;
    Consumer<T> outcome;

    public TileEntityOutcome(Class<T> tileEntityClass, Consumer<T> outcome) {
        this.tileEntityClass = tileEntityClass;
        this.outcome = outcome;
    }

    @Override
    public void apply(World world, BlockPos pos, IBlockState blockState, EntityPlayer player, EnumFacing hitFace) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntityClass.isInstance(tileEntity)) {
            outcome.accept(tileEntityClass.cast(tileEntity));
            world.notifyBlockUpdate(pos, blockState, blockState, 3);
        }
    }
}
