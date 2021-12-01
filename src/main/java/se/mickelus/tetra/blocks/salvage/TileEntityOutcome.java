package se.mickelus.tetra.blocks.salvage;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class TileEntityOutcome<T extends BlockEntity> implements InteractionOutcome {

    Class<T> tileEntityClass;
    Function<T, Boolean> outcome;

    public TileEntityOutcome(Class<T> tileEntityClass, Function<T, Boolean> outcome) {
        this.tileEntityClass = tileEntityClass;
        this.outcome = outcome;
    }

    @Override
    public boolean apply(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction hitFace) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntityClass.isInstance(tileEntity)) {
            boolean result = outcome.apply(tileEntityClass.cast(tileEntity));
            world.sendBlockUpdated(pos, blockState, blockState, 3);
            return result;
        }
        return false;
    }
}
