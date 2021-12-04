package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.util.TickProvider;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static net.minecraft.world.level.material.Fluids.WATER;
@ParametersAreNonnullByDefault
public class CoreExtractorBaseBlock extends TetraWaterloggedBlock implements EntityBlock {
    public static final DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;
	public static final TickProvider<CoreExtractorBaseTile> TILE_TICK_PROVIDER = new TickProvider<>(CoreExtractorBaseTile.type, CoreExtractorBaseTile::new);
    private static final VoxelShape capShape = box(3, 14, 3, 13, 16, 13);
    private static final VoxelShape shaftShape = box(4, 13, 4, 12, 14, 12);
    private static final VoxelShape smallCoverShapeZ = box(1, 0, 0, 15, 12, 16);
    private static final VoxelShape largeCoverShapeZ = box(0, 0, 1, 16, 13, 15);
    private static final VoxelShape smallCoverShapeX = box(0, 0, 1, 16, 12, 15);
    private static final VoxelShape largeCoverShapeX = box(1, 0, 0, 15, 13, 16);

    private static final VoxelShape combinedShapeZ
            = Shapes.or(Shapes.joinUnoptimized(smallCoverShapeZ, largeCoverShapeZ, BooleanOp.OR), capShape, shaftShape);
    private static final VoxelShape combinedShapeX
            = Shapes.or(Shapes.joinUnoptimized(smallCoverShapeX, largeCoverShapeX, BooleanOp.OR), capShape, shaftShape);

    public static final String unlocalizedName = "core_extractor";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static CoreExtractorBaseBlock instance;

    public CoreExtractorBaseBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);
        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (Direction.Axis.X.equals(state.getValue(facingProp).getAxis())) {
            return combinedShapeX;
        }

        return combinedShapeZ;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
        tooltip.add(new TextComponent(" "));
        tooltip.add(new TranslatableComponent("block.multiblock_hint.1x2x1")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        if (!pos.relative(world.getBlockState(pos).getValue(facingProp)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, CoreExtractorBaseTile.class)
                    .ifPresent(CoreExtractorBaseTile::updateTransferState);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (Direction.UP.equals(facing) && !CoreExtractorPistonBlock.instance.equals(facingState.getBlock())) {
            return state.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    // based on same method implementation in BedBlock
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockState pistonState = CoreExtractorPistonBlock.instance.defaultBlockState()
                .setValue(WATERLOGGED, world.getFluidState(pos.above()).getType() == WATER);
        world.setBlock(pos.above(), pistonState, 3);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
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

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return TILE_TICK_PROVIDER.create(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
		return TILE_TICK_PROVIDER.forTileType(entityType).orElseGet(() -> EntityBlock.super.getTicker(level, state, entityType));
	}
}
