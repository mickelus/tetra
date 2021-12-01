package se.mickelus.tetra.blocks.forged;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static net.minecraft.world.level.material.Fluids.WATER;

@ParametersAreNonnullByDefault
public class ForgedCrateBlock extends FallingBlock implements ITetraBlock, IInteractiveBlock, SimpleWaterloggedBlock {
    static final String unlocalizedName = "forged_crate";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ForgedCrateBlock instance;

    public static final DirectionProperty propFacing = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty propStacked = BooleanProperty.create("stacked");
    public static final IntegerProperty propIntegrity = IntegerProperty.create("integrity", 0, 3);

    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(ToolTypes.pry, 1, Direction.EAST, 6, 8, 6, 8,
                    BlockStatePredicate.ANY,
                    ForgedCrateBlock::attemptBreakPry),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 1, 4, 1, 4,
                    BlockStatePredicate.ANY,
                    ForgedCrateBlock::attemptBreakHammer),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 10, 13, 10, 13,
                    BlockStatePredicate.ANY,
                    ForgedCrateBlock::attemptBreakHammer),
    };

    public static final ResourceLocation interactionLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/crate_content");

    private static final VoxelShape shape = box(1, 0, 1, 15, 14, 15);
    private static final VoxelShape[] shapesNormal = new VoxelShape[4];
    private static final VoxelShape[] shapesOffset = new VoxelShape[4];
    static {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            shapesNormal[dir.get2DDataValue()] = shape.move(dir.getStepX() / 16f, dir.getStepY() / 16f, dir.getStepZ() / 16f);
            shapesOffset[dir.get2DDataValue()] = shapesNormal[dir.get2DDataValue()].move(0, -1 / 8f, 0);
        }
    }

    public ForgedCrateBlock() {
        super(Properties.of(ForgedBlockCommon.forgedMaterial)
                .sound(SoundType.METAL)
                .strength(5));

        setRegistryName(unlocalizedName);

        this.registerDefaultState(defaultBlockState()
                .setValue(propFacing, Direction.EAST)
                .setValue(propStacked, false)
                .setValue(propIntegrity, 3)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    private static boolean attemptBreakHammer(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction facing) {
        return attemptBreak(world, pos, blockState, player, hand, player.getItemInHand(hand), ToolTypes.hammer, 2, 1);
    }

    private static boolean attemptBreakPry(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction facing) {
        return attemptBreak(world, pos, blockState, player, hand, player.getItemInHand(hand), ToolTypes.pry, 0, 2);
    }

    private static boolean attemptBreak(Level world, BlockPos pos, BlockState blockState, @Nullable Player player, @Nullable InteractionHand hand,
            ItemStack itemStack, ToolAction toolType, int min, int multiplier) {

        if (player == null) {
            return false;
        }

        int integrity = blockState.getValue(propIntegrity);

        int progress = CastOptional.cast(itemStack.getItem(), IToolProvider.class)
                .map(item -> item.getToolLevel(itemStack, toolType))
                .map(level -> ( level - min ) * multiplier)
                .orElse(1);

        if (integrity - progress >= 0) {
            if (ToolTypes.hammer.equals(toolType)) {
                world.playSound(player, pos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 1, 0.5f);
            } else {
                world.playSound(player, pos, SoundEvents.LADDER_STEP, SoundSource.PLAYERS, 0.7f, 2f);
            }

            world.setBlockAndUpdate(pos, blockState.setValue(propIntegrity, integrity - progress));
        } else {
            boolean didBreak = EffectHelper.breakBlock(world, player, itemStack, pos, blockState, false);
            if (didBreak && world instanceof ServerLevel) {
                BlockInteraction.getLoot(interactionLootTable, player, hand, (ServerLevel) world, blockState)
                        .forEach(lootStack -> popResource(world, pos, lootStack));
            }
        }

        return true;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState state, Direction face, Collection<ToolAction> tools) {
            return interactions;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(propFacing, propStacked, propIntegrity, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(propFacing, context.getHorizontalDirection())
                .setValue(propStacked, equals(context.getLevel().getBlockState(context.getClickedPos().below()).getBlock()))
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
            BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(currentPos, WATER, WATER.getTickDelay(world));
        }

        if (Direction.DOWN.equals(facing)) {
            return super.updateShape(state, facing, facingState, world, currentPos, facingPos)
                    .setValue(propStacked, equals(facingState.getBlock()));
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (!state.getValue(propStacked)) {
            return shapesNormal[state.getValue(propFacing).get2DDataValue()];
        }
        return shapesOffset[state.getValue(propFacing).get2DDataValue()];
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(propFacing, rotation.rotate(state.getValue(propFacing)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(propFacing)));
    }

    @Override
    public boolean hasItem() {
        return true;
    }

    @Override
    public void registerItem(IForgeRegistry<Item> registry) {
        registerItem(registry, this);
    }
}
