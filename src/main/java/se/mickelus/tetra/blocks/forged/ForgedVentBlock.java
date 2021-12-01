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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class ForgedVentBlock extends TetraWaterloggedBlock implements IInteractiveBlock {
    static final String unlocalizedName = "forged_vent";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ForgedVentBlock instance;

    public static final IntegerProperty propRotation = IntegerProperty.create("rotation", 0, 3);
    public static final BooleanProperty propX = BooleanProperty.create("x");
    public static final BooleanProperty propBroken = BooleanProperty.create("broken");

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 1, 4, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(0)),
                    ForgedVentBlock::breakBolt),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 1, 4, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(1)),
                    ForgedVentBlock::breakBolt),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 12, 15, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(2)),
                    ForgedVentBlock::breakBolt),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.EAST, 12, 15, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(3)),
                    ForgedVentBlock::breakBolt),

            new BlockInteraction(ToolTypes.hammer, 3, Direction.WEST, 12, 15, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(0)),
                    ForgedVentBlock::breakBolt),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.WEST, 12, 15, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(1)),
                    ForgedVentBlock::breakBolt),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.WEST, 1, 4, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(2)),
                    ForgedVentBlock::breakBolt),
            new BlockInteraction(ToolTypes.hammer, 3, Direction.WEST, 1, 4, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(3)),
                    ForgedVentBlock::breakBolt),

            new BlockInteraction(ToolTypes.pry, 1, Direction.EAST, 7, 11, 8, 12,
                    new PropertyMatcher().where(propBroken, equalTo(true)),
                    ForgedVentBlock::breakBeam),
            new BlockInteraction(ToolTypes.pry, 1, Direction.WEST, 7, 11, 8, 12,
                    new PropertyMatcher().where(propBroken, equalTo(true)),
                    ForgedVentBlock::breakBeam),
    };

    private static final ResourceLocation boltLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/bolt_break");
    private static final ResourceLocation ventLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/vent_break");

    public ForgedVentBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);

        hasItem = true;

        setRegistryName(unlocalizedName);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(propRotation, propX, propBroken);
    }

    private static boolean breakBolt(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction hitFace) {
        world.setBlock(pos, world.getBlockState(pos).setValue(propBroken, true), 2);

        if (!world.isClientSide) {
            ServerLevel serverWorld = (ServerLevel) world;
            if (player != null) {
                BlockInteraction.dropLoot(ventLootTable, player, hand, serverWorld, blockState);
            } else {
                BlockInteraction.dropLoot(ventLootTable, serverWorld, pos, blockState);
            }

            serverWorld.playSound(null, pos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.PLAYERS, 0.4f, 0.5f);
        }

        return true;
    }

    private static boolean breakBeam(Level world, BlockPos pos, BlockState blockState, @Nullable Player player, @Nullable InteractionHand hand, Direction hitFace) {
        List<BlockPos> connectedVents = getConnectedBlocks(world, pos, new LinkedList<>(), blockState.getValue(propX));

        if (connectedVents.stream().anyMatch(blockPos -> !world.getBlockState(blockPos).getValue(propBroken))) {
            if (!world.isClientSide) {
                world.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.4f, 2);
            }
            return false;
        }

        connectedVents.forEach(blockPos -> {
            world.levelEvent(null, 2001, blockPos, Block.getId(world.getBlockState(blockPos)));
            world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
        });

        if (!world.isClientSide) {
            ServerLevel serverWorld = (ServerLevel) world;
            if (player != null) {
                BlockInteraction.dropLoot(ventLootTable, player, hand, serverWorld, blockState);
            } else {
                BlockInteraction.dropLoot(ventLootTable, serverWorld, pos, blockState);
            }
        }

        return true;
    }

    private static List<BlockPos> getConnectedBlocks(Level world, BlockPos pos, List<BlockPos> visited, boolean isXAxis) {
        if (!visited.contains(pos) && world.getBlockState(pos).getBlock() instanceof ForgedVentBlock) {
            visited.add(pos);

            getConnectedBlocks(world, pos.above(), visited, isXAxis);
            getConnectedBlocks(world, pos.below(), visited, isXAxis);

            if (isXAxis) {
                getConnectedBlocks(world, pos.east(), visited, isXAxis);
                getConnectedBlocks(world, pos.west(), visited, isXAxis);
            } else {
                getConnectedBlocks(world, pos.north(), visited, isXAxis);
                getConnectedBlocks(world, pos.south(), visited, isXAxis);
            }
        }

        return visited;
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState state, Direction face, Collection<ToolType> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(world, pos, state, state.getValue(propX) ? Direction.EAST : Direction.SOUTH, face, tools))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTrace) {
        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, rayTrace);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = super.getStateForPlacement(context);

        Direction playerFacing = context.getPlayer() != null ? context.getPlayer().getDirection() : Direction.NORTH;

        blockState = blockState != null ? blockState : defaultBlockState();
        blockState = blockState.setValue(propX, Direction.Axis.X.equals(playerFacing.getAxis()));

        int rotation = 0;

        if (Direction.EAST.equals(playerFacing) || Direction.SOUTH.equals(playerFacing)) {
            rotation = 2;
        }

        if (context.getClickedFace() != Direction.UP
                && (context.getClickedFace() == Direction.DOWN || context.getClickLocation().y - context.getClickedPos().getY() > 0.5)) {
            rotation++;
        }

        blockState = blockState
                .setValue(propRotation, rotation)
                .setValue(propBroken, false);

        return  blockState;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        boolean isXAxis = state.getValue(propX);
        if (rot.equals(Rotation.CLOCKWISE_90) || rot.equals(Rotation.COUNTERCLOCKWISE_90)) {
            state = state.setValue(propX, !isXAxis);
        }
        if (rot.equals(Rotation.CLOCKWISE_180)
                || (!isXAxis && rot.equals(Rotation.CLOCKWISE_90))
                || (isXAxis && rot.equals(Rotation.COUNTERCLOCKWISE_90))) {
            return state.setValue(propRotation, state.getValue(propRotation) ^ 2);
        }

        return state.setValue(propRotation, state.getValue(propRotation));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext contex) {
        if (state.getValue(propX)) {
            return box(0, 0, 7, 16, 16, 9);
        }
        return box(7, 0, 0, 9, 16, 16);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return 0;
    }
}
