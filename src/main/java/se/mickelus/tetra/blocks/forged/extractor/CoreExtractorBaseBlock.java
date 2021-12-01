package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.fluid.Fluids.WATER;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class CoreExtractorBaseBlock extends TetraWaterloggedBlock {
    public static final DirectionProperty facingProp = HorizontalBlock.FACING;

    private static final VoxelShape capShape = box(3, 14, 3, 13, 16, 13);
    private static final VoxelShape shaftShape = box(4, 13, 4, 12, 14, 12);
    private static final VoxelShape smallCoverShapeZ = box(1, 0, 0, 15, 12, 16);
    private static final VoxelShape largeCoverShapeZ = box(0, 0, 1, 16, 13, 15);
    private static final VoxelShape smallCoverShapeX = box(0, 0, 1, 16, 12, 15);
    private static final VoxelShape largeCoverShapeX = box(1, 0, 0, 15, 13, 16);

    private static final VoxelShape combinedShapeZ
            = VoxelShapes.or(VoxelShapes.joinUnoptimized(smallCoverShapeZ, largeCoverShapeZ, IBooleanFunction.OR), capShape, shaftShape);
    private static final VoxelShape combinedShapeX
            = VoxelShapes.or(VoxelShapes.joinUnoptimized(smallCoverShapeX, largeCoverShapeX, IBooleanFunction.OR), capShape, shaftShape);

    public static final String unlocalizedName = "core_extractor";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static CoreExtractorBaseBlock instance;

    public CoreExtractorBaseBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);
        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (Direction.Axis.X.equals(state.getValue(facingProp).getAxis())) {
            return combinedShapeX;
        }

        return combinedShapeZ;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
        tooltip.add(new StringTextComponent(" "));
        tooltip.add(new TranslationTextComponent("block.multiblock_hint.1x2x1")
                .withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        if (!pos.relative(world.getBlockState(pos).getValue(facingProp)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, CoreExtractorBaseTile.class)
                    .ifPresent(CoreExtractorBaseTile::updateTransferState);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
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
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        if (Direction.UP.equals(facing) && !CoreExtractorPistonBlock.instance.equals(facingState.getBlock())) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    // based on same method implementation in BedBlock
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockState pistonState = CoreExtractorPistonBlock.instance.defaultBlockState()
                .setValue(WATERLOGGED, world.getFluidState(pos.above()).getType() == WATER);
        world.setBlock(pos.above(), pistonState, 3);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced(context)) {
            return super.getStateForPlacement(context)
                    .setValue(facingProp, context.getHorizontalDirection().getOpposite());
        }

        // returning null here stops the block from being placed
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction) {
        return state.setValue(facingProp, direction.rotate(state.getValue(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(facingProp)));
    }
}
