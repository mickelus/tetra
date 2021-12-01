package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.*;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
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
import java.util.Collection;
import java.util.List;

import static net.minecraft.fluid.Fluids.WATER;
import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;


import net.minecraft.block.AbstractBlock.Properties;

public class ForgedCrateBlock extends FallingBlock implements ITetraBlock, IInteractiveBlock, IWaterLoggable {
    static final String unlocalizedName = "forged_crate";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ForgedCrateBlock instance;

    public static final DirectionProperty propFacing = HorizontalBlock.FACING;
    public static final BooleanProperty propStacked = BooleanProperty.create("stacked");
    public static final IntegerProperty propIntegrity = IntegerProperty.create("integrity", 0, 3);

    static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(ToolTypes.pry, 1, Direction.EAST, 6, 8, 6, 8,
                    BlockStateMatcher.ANY,
                    ForgedCrateBlock::attemptBreakPry),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 1, 4, 1, 4,
                    BlockStateMatcher.ANY,
                    ForgedCrateBlock::attemptBreakHammer),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 10, 13, 10, 13,
                    BlockStateMatcher.ANY,
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
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    private static boolean attemptBreakHammer(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction facing) {
        return attemptBreak(world, pos, blockState, player, hand, player.getItemInHand(hand), ToolTypes.hammer, 2, 1);
    }

    private static boolean attemptBreakPry(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction facing) {
        return attemptBreak(world, pos, blockState, player, hand, player.getItemInHand(hand), ToolTypes.pry, 0, 2);
    }

    private static boolean attemptBreak(World world, BlockPos pos, BlockState blockState, @Nullable PlayerEntity player, @Nullable Hand hand,
            ItemStack itemStack, ToolType toolType, int min, int multiplier) {

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
                world.playSound(player, pos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 1, 0.5f);
            } else {
                world.playSound(player, pos, SoundEvents.LADDER_STEP, SoundCategory.PLAYERS, 0.7f, 2f);
            }

            world.setBlockAndUpdate(pos, blockState.setValue(propIntegrity, integrity - progress));
        } else {
            boolean didBreak = EffectHelper.breakBlock(world, player, itemStack, pos, blockState, false);
            if (didBreak && world instanceof ServerWorld) {
                BlockInteraction.getLoot(interactionLootTable, player, hand, (ServerWorld) world, blockState)
                        .forEach(lootStack -> popResource(world, pos, lootStack));
            }
        }

        return true;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(World world, BlockPos pos, BlockState state, Direction face, Collection<ToolType> tools) {
            return interactions;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(propFacing, propStacked, propIntegrity, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context)
                .setValue(propFacing, context.getHorizontalDirection())
                .setValue(propStacked, equals(context.getLevel().getBlockState(context.getClickedPos().below()).getBlock()))
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos,
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
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
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
