package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import se.mickelus.mutil.network.PacketHandler;
import se.mickelus.mutil.util.TileEntityOptional;
import se.mickelus.tetra.TetraItemAbilities;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Predicates.equalTo;

@ParametersAreNonnullByDefault
public class ForgedContainerBlock extends TetraWaterloggedBlock implements IInteractiveBlock, EntityBlock {
    public static final String identifier = "forged_container";
    public static final DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty flippedProp = BooleanProperty.create("flipped");
    public static final BooleanProperty locked1Prop = BooleanProperty.create("locked1");
    public static final BooleanProperty locked2Prop = BooleanProperty.create("locked2");
    public static final BooleanProperty anyLockedProp = BooleanProperty.create("locked_any");
    public static final BooleanProperty openProp = BooleanProperty.create("open");
    public static final BlockInteraction[] interactions = new BlockInteraction[]{
            new BlockInteraction(TetraItemAbilities.hammer, 3, Direction.SOUTH, 5, 7, 2, 5,
                    new PropertyMatcher().where(locked1Prop, equalTo(true)).where(flippedProp, equalTo(false)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 0, hand)),
            new BlockInteraction(TetraItemAbilities.hammer, 3, Direction.SOUTH, 11, 13, 2, 5,
                    new PropertyMatcher().where(locked2Prop, equalTo(true)).where(flippedProp, equalTo(false)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 1, hand)),
            new BlockInteraction(TetraItemAbilities.hammer, 3, Direction.SOUTH, 17, 19, 2, 5,
                    new PropertyMatcher().where(locked1Prop, equalTo(true)).where(flippedProp, equalTo(true)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 2, hand)),
            new BlockInteraction(TetraItemAbilities.hammer, 3, Direction.SOUTH, 23, 25, 2, 5,
                    new PropertyMatcher().where(locked2Prop, equalTo(true)).where(flippedProp, equalTo(true)),
                    (world, pos, blockState, player, hand, hitFace) -> breakLock(world, pos, player, 3, hand)),
            new BlockInteraction(TetraItemAbilities.pry, 1, Direction.SOUTH, 1, 15, 3, 4,
                    new PropertyMatcher()
                            .where(anyLockedProp, equalTo(false))
                            .where(openProp, equalTo(false))
                            .where(flippedProp, equalTo(false)),
                    ForgedContainerBlock::open),
            new BlockInteraction(TetraItemAbilities.pry, 1, Direction.SOUTH, 15, 28, 3, 4,
                    new PropertyMatcher()
                            .where(anyLockedProp, equalTo(false))
                            .where(openProp, equalTo(false))
                            .where(flippedProp, equalTo(true)),
                    ForgedContainerBlock::open)
    };
    private static final VoxelShape shapeZ1 = box(1, 0, -15, 15, 12, 15);
    private static final VoxelShape shapeZ2 = box(1, 0, 1, 15, 12, 31);
    private static final VoxelShape shapeX1 = box(-15, 0, 1, 15, 12, 15);
    private static final VoxelShape shapeX2 = box(1, 0, 1, 31, 12, 15);
    private static final VoxelShape shapeZ1Open = box(1, 0, -15, 15, 9, 15);
    private static final VoxelShape shapeZ2Open = box(1, 0, 1, 15, 9, 31);
    private static final VoxelShape shapeX1Open = box(-15, 0, 1, 15, 9, 15);
    private static final VoxelShape shapeX2Open = box(1, 0, 1, 31, 9, 15);
    public static Supplier<ForgedContainerBlock> instance;

    public ForgedContainerBlock() {
        super(ForgedBlockCommon.propertiesSolid);

        registerDefaultState(defaultBlockState()
                .setValue(facingProp, Direction.EAST)
                .setValue(flippedProp, false)
                .setValue(openProp, true)
                .setValue(locked1Prop, false)
                .setValue(locked2Prop, false)
                .setValue(anyLockedProp, false));
    }

    private static boolean breakLock(Level world, BlockPos pos, @Nullable Player player, int index, @Nullable InteractionHand hand) {
        ForgedContainerBlockEntity te = (ForgedContainerBlockEntity) world.getBlockEntity(pos);
        if (te != null) {
            te.getOrDelegate().breakLock(player, index, hand);
        }

        return true;
    }

    private static boolean open(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction facing) {
        ForgedContainerBlockEntity te = (ForgedContainerBlockEntity) world.getBlockEntity(pos);
        if (te != null) {
            te.getOrDelegate().open(player);
        }

        return true;
    }

    @Override
    public void registerPackets(PacketHandler packetHandler) {
        packetHandler.registerServerBoundPacket(ChangeCompartmentPacket.class, ChangeCompartmentPacket::new);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState state, Direction face, Collection<ItemAbility> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(world, pos, state, state.getValue(facingProp), face, tools))
                .toArray(BlockInteraction[]::new);
    }

    private InteractionResult useInternal(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult didInteract = BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);

        if (didInteract != InteractionResult.SUCCESS) {
            if (!world.isClientSide) {
                TileEntityOptional.from(world, pos, ForgedContainerBlockEntity.class)
                        .ifPresent(te -> {
                            ForgedContainerBlockEntity delegate = te.getOrDelegate();
                            if (delegate.isOpen()) {
                                player.openMenu(delegate, delegate.getBlockPos());
                            }
                        });
            }
        } else {
            world.sendBlockUpdated(pos, state, state, 3);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return switch (useInternal(state, world, pos, player, hand, hit)) {
            case SUCCESS, CONSUME -> ItemInteractionResult.sidedSuccess(world.isClientSide);
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return useInternal(state, world, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
            // only drop loot from open, primary/unflipped chests
            if (state.getValue(openProp) && !state.getValue(flippedProp)) {
                dropBlockInventory(this, world, pos, newState);
            } else {
                TileEntityOptional.from(world, pos, ForgedContainerBlockEntity.class).ifPresent(BlockEntity::setRemoved);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(facingProp);
        boolean flipped = state.getValue(flippedProp);
        boolean open = state.getValue(openProp);

        return switch (facing) {
            case NORTH -> flipped ? (open ? shapeX1Open : shapeX1) : (open ? shapeX2Open : shapeX2);
            case EAST -> flipped ? (open ? shapeZ1Open : shapeZ1) : (open ? shapeZ2Open : shapeZ2);
            case SOUTH -> flipped ? (open ? shapeX2Open : shapeX2) : (open ? shapeX1Open : shapeX1);
            case WEST -> flipped ? (open ? shapeZ2Open : shapeZ2) : (open ? shapeZ1Open : shapeZ1);
            default -> Shapes.block();
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp, flippedProp, locked1Prop, locked2Prop, anyLockedProp, openProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos().relative(context.getHorizontalDirection().getClockWise())).canBeReplaced(context)) {
            return super.getStateForPlacement(context).setValue(facingProp, context.getHorizontalDirection());
        }

        // returning null here stops the block from being placed
        return null;
    }

    // based on same method implementation in BedBlock
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(facingProp);
        world.setBlock(pos.relative(facing.getClockWise()), defaultBlockState().setValue(flippedProp, true).setValue(facingProp, facing), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
            BlockPos facingPos) {
        Direction pairedFacing = state.getValue(facingProp);
        if (state.getValue(flippedProp)) {
            pairedFacing = pairedFacing.getCounterClockWise();
        } else {
            pairedFacing = pairedFacing.getClockWise();
        }

        if (pairedFacing == facing && !equals(facingState.getBlock())) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        Direction facing = state.getValue(facingProp);

        if (Rotation.CLOCKWISE_180.equals(rot)
                || Rotation.CLOCKWISE_90.equals(rot) && (Direction.NORTH.equals(facing) || Direction.SOUTH.equals(facing))
                || Rotation.COUNTERCLOCKWISE_90.equals(rot) && (Direction.EAST.equals(facing) || Direction.WEST.equals(facing))) {
            state = state.setValue(flippedProp, state.getValue(flippedProp));
        }

        return state.setValue(facingProp, rot.rotate(facing));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(facingProp)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new ForgedContainerBlockEntity(p_153215_, p_153216_);
    }
}
