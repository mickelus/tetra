package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.fluid.Fluids.WATER;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class ForgedWorkbenchBlock extends AbstractWorkbenchBlock implements IWaterLoggable {
    public static final EnumProperty<Direction.Axis> axis = BlockStateProperties.HORIZONTAL_AXIS;

    public static final String unlocalizedName = "forged_workbench";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static AbstractWorkbenchBlock instance;

    private static final VoxelShape zShape = VoxelShapes.or(
            makeCuboidShape(1, 0, 3, 15, 2, 13),
            makeCuboidShape(2, 2, 4, 14, 9, 12),
            makeCuboidShape(0, 9, 2, 16, 16, 14));
    private static final VoxelShape xShape = VoxelShapes.or(
            makeCuboidShape(3, 0, 1, 13, 2, 15),
            makeCuboidShape(4, 2, 2, 12, 9, 14),
            makeCuboidShape(2, 9, 0, 14, 16, 16));

    public ForgedWorkbenchBlock() {
        super(ForgedBlockCommon.propertiesSolid);

        setRegistryName(unlocalizedName);

        hasItem = true;

        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction.Axis axis = state.get(ForgedWorkbenchBlock.axis);

        if (axis == Direction.Axis.Z) {
            return zShape;
        }

        return xShape;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(WATERLOGGED, axis);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState()
                .with(WATERLOGGED, context.getWorld().getFluidState(context.getPos()).getFluid() == WATER)
                .with(axis, context.getPlacementHorizontalFacing().getAxis());
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
