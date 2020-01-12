package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.fluid.Fluids.WATER;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class CoreExtractorBaseBlock extends TetraWaterloggedBlock {
    public static final DirectionProperty facingProp = HorizontalBlock.HORIZONTAL_FACING;

    private static final VoxelShape capShape = makeCuboidShape(3, 14, 3, 13, 16, 13);
    private static final VoxelShape shaftShape = makeCuboidShape(4, 13, 4, 12, 14, 12);
    private static final VoxelShape smallCoverShapeZ = makeCuboidShape(1, 0, 0, 15, 12, 16);
    private static final VoxelShape largeCoverShapeZ = makeCuboidShape(0, 0, 1, 16, 13, 15);
    private static final VoxelShape smallCoverShapeX = makeCuboidShape(0, 0, 1, 16, 12, 15);
    private static final VoxelShape largeCoverShapeX = makeCuboidShape(1, 0, 0, 15, 13, 16);

    private static final VoxelShape combinedShapeZ
            = VoxelShapes.or(VoxelShapes.combine(smallCoverShapeZ, largeCoverShapeZ, IBooleanFunction.OR), capShape, shaftShape);
    private static final VoxelShape combinedShapeX
            = VoxelShapes.or(VoxelShapes.combine(smallCoverShapeX, largeCoverShapeX, IBooleanFunction.OR), capShape, shaftShape);

    public static final String unlocalizedName = "core_extractor";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static CoreExtractorBaseBlock instance;

    public CoreExtractorBaseBlock() {
        super(ForgedBlockCommon.properties);
        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (Direction.Axis.X.equals(state.get(facingProp).getAxis())) {
            return combinedShapeX;
        }

        return combinedShapeZ;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.hintTooltip);
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new TranslationTextComponent("block.multiblock_hint.1x2x1")
                .setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true)));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        if (!pos.offset(world.getBlockState(pos).get(facingProp)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, CoreExtractorBaseTile.class)
                    .ifPresent(CoreExtractorBaseTile::updateTransferState);
        }
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(facingProp);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CoreExtractorBaseTile();
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (Direction.UP.equals(facing) && !CoreExtractorPistonBlock.instance.equals(facingState.getBlock())) {
            return state.get(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
        }

        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    // based on same method implementation in BedBlock
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockState pistonState = CoreExtractorPistonBlock.instance.getDefaultState()
                .with(WATERLOGGED, world.getFluidState(pos.up()).getFluid() == WATER);
        world.setBlockState(pos.up(), pistonState, 3);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        if (context.getWorld().getBlockState(context.getPos().up()).isReplaceable(context)) {
            return super.getStateForPlacement(context)
                    .with(facingProp, context.getPlacementHorizontalFacing().getOpposite());
        }

        // returning null here stops the block from being placed
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction) {
        return state.with(facingProp, direction.rotate(state.get(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(facingProp)));
    }
}
