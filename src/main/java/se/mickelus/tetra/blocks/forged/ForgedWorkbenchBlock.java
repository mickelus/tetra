package se.mickelus.tetra.blocks.forged;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static net.minecraft.world.level.material.Fluids.WATER;

@ParametersAreNonnullByDefault
public class ForgedWorkbenchBlock extends AbstractWorkbenchBlock implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction.Axis> axis = BlockStateProperties.HORIZONTAL_AXIS;

    public static final String unlocalizedName = "forged_workbench";
    private static final VoxelShape zShape = Shapes.or(
            box(1, 0, 3, 15, 2, 13),
            box(2, 2, 4, 14, 9, 12),
            box(0, 9, 2, 16, 16, 14));
    private static final VoxelShape xShape = Shapes.or(
            box(3, 0, 1, 13, 2, 15),
            box(4, 2, 2, 12, 9, 14),
            box(2, 9, 0, 14, 16, 16));
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static AbstractWorkbenchBlock instance;

    public ForgedWorkbenchBlock() {
        super(ForgedBlockCommon.propertiesSolid);

        setRegistryName(unlocalizedName);

        hasItem = true;

        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(axis, Direction.Axis.X));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(ForgedWorkbenchBlock.axis);

        if (axis == Direction.Axis.Z) {
            return zShape;
        }

        return xShape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(WATERLOGGED, axis);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == WATER)
                .setValue(axis, context.getHorizontalDirection().getAxis());
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
            BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, WATER, WATER.getTickDelay(worldIn));
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }
}
