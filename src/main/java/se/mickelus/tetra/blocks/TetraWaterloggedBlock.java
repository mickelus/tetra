package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

import static net.minecraft.fluid.Fluids.WATER;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class TetraWaterloggedBlock extends TetraBlock implements IWaterLoggable {
    public TetraWaterloggedBlock(Properties properties) {
        super(properties);

        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(WATERLOGGED);
    }

    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState()
                .with(WATERLOGGED, context.getWorld().getFluidState(context.getPos()).getFluid() == WATER);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
            BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, WATER, WATER.getTickRate(worldIn));
        }

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }
}
