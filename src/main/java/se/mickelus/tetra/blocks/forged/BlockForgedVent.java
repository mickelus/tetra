package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
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
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class BlockForgedVent extends TetraBlock implements IBlockCapabilityInteractive {
    static final String unlocalizedName = "forged_vent";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedVent instance;

    public static final IntegerProperty propRotation = IntegerProperty.create("rotation", 0, 3);
    public static final BooleanProperty propX = BooleanProperty.create("x");
    public static final BooleanProperty propBroken = BooleanProperty.create("broken");

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 1, 4, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(0)),
                    BlockForgedVent::breakBolt),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 1, 4, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(1)),
                    BlockForgedVent::breakBolt),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 12, 15, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(2)),
                    BlockForgedVent::breakBolt),
            new BlockInteraction(Capability.hammer, 3, Direction.EAST, 12, 15, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(3)),
                    BlockForgedVent::breakBolt),

            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 12, 15, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(0)),
                    BlockForgedVent::breakBolt),
            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 12, 15, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(1)),
                    BlockForgedVent::breakBolt),
            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 1, 4, 12, 15,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(2)),
                    BlockForgedVent::breakBolt),
            new BlockInteraction(Capability.hammer, 3, Direction.WEST, 1, 4, 1, 4,
                    new PropertyMatcher().where(propBroken, equalTo(false)).where(propRotation, equalTo(3)),
                    BlockForgedVent::breakBolt),

            new BlockInteraction(Capability.pry, 1, Direction.EAST, 7, 11, 8, 12,
                    new PropertyMatcher().where(propBroken, equalTo(true)),
                    BlockForgedVent::breakBeam),
            new BlockInteraction(Capability.pry, 1, Direction.WEST, 7, 11, 8, 12,
                    new PropertyMatcher().where(propBroken, equalTo(true)),
                    BlockForgedVent::breakBeam),
    };

    private static final ResourceLocation boltLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/bolt_break");
    private static final ResourceLocation ventLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/vent_break");

    public BlockForgedVent() {
        super(ForgedBlockCommon.properties);

        hasItem = true;

        setRegistryName(unlocalizedName);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(propRotation, propX, propBroken);
    }

    private static boolean breakBolt(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace) {
        world.setBlockState(pos, world.getBlockState(pos).with(propBroken, true), 2);

        if (!world.isRemote) {
            ServerWorld serverWorld = (ServerWorld) world;
            LootTable table = serverWorld.getServer().getLootTableManager().getLootTableFromLocation(boltLootTable);

            LootContext context = new LootContext.Builder(serverWorld)
                    .withLuck(player.getLuck())
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .withParameter(LootParameters.BLOCK_STATE, blockState)
                    .withParameter(LootParameters.TOOL, player.getHeldItem(hand))
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .withParameter(LootParameters.POSITION, player.getPosition())
                    .build(LootParameterSets.BLOCK);

            table.generate(context).forEach(itemStack -> {
                if (!player.inventory.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false);
                }
            });

            serverWorld.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.PLAYERS, 0.4f, 0.5f);
        }

        return true;
    }

    private static boolean breakBeam(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace) {
        List<BlockPos> connectedVents = getConnectedBlocks(world, pos, new LinkedList<>(), blockState.get(propX));

        if (connectedVents.stream().anyMatch(blockPos -> !world.getBlockState(blockPos).get(propBroken))) {
            if (!world.isRemote) {
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.4f, 2);
            }
            return false;
        }

        connectedVents.forEach(blockPos -> {
            world.playEvent(null, 2001, blockPos, Block.getStateId(world.getBlockState(blockPos)));
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 2);
        });

        if (!world.isRemote) {
            ServerWorld serverWorld = (ServerWorld) world;
            LootTable table = serverWorld.getServer().getLootTableManager().getLootTableFromLocation(ventLootTable);

            LootContext context = new LootContext.Builder(serverWorld)
                    .withLuck(player.getLuck())
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .withParameter(LootParameters.BLOCK_STATE, blockState)
                    .withParameter(LootParameters.TOOL, player.getHeldItem(hand))
                    .withParameter(LootParameters.THIS_ENTITY, player)
                    .withParameter(LootParameters.POSITION, player.getPosition())
                    .build(LootParameterSets.BLOCK);

            table.generate(context).forEach(itemStack -> {
                if (!player.inventory.addItemStackToInventory(itemStack)) {
                    player.dropItem(itemStack, false);
                }
            });
        }

        return true;
    }

    private static List<BlockPos> getConnectedBlocks(World world, BlockPos pos, List<BlockPos> visited, boolean isXAxis) {
        if (!visited.contains(pos) && world.getBlockState(pos).getBlock() instanceof BlockForgedVent) {
            visited.add(pos);

            getConnectedBlocks(world, pos.up(), visited, isXAxis);
            getConnectedBlocks(world, pos.down(), visited, isXAxis);

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
    public BlockInteraction[] getPotentialInteractions(BlockState state, Direction face, Collection<Capability> capabilities) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(state, state.get(propX) ? Direction.EAST : Direction.SOUTH, face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        return BlockInteraction.attemptInteraction(world, state.getExtendedState(world, pos), pos, player, hand, rayTrace);
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.hintTooltip);
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, Direction facing, BlockState state2, IWorld world, BlockPos pos1, BlockPos pos2, Hand hand) {
        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockState = super.getStateForPlacement(context);

        Direction playerFacing = context.getPlayer() != null ? context.getPlayer().getHorizontalFacing() : Direction.NORTH;

        blockState = blockState != null ? blockState : getDefaultState();
        blockState = blockState.with(propX, Direction.Axis.X.equals(playerFacing.getAxis()));

        int rotation = 0;

        if (Direction.EAST.equals(playerFacing) || Direction.SOUTH.equals(playerFacing)) {
            rotation = 2;
        }

        if (context.getFace() != Direction.UP
                && (context.getFace() == Direction.DOWN || context.getHitVec().y - context.getPos().getY() > 0.5)) {
            rotation++;
        }

        blockState = blockState.with(propRotation, rotation);
        blockState = blockState.with(propBroken, false);

        return  blockState;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        boolean isXAxis = state.get(propX);
        if (rot.equals(Rotation.CLOCKWISE_90) || rot.equals(Rotation.COUNTERCLOCKWISE_90)) {
            state = state.with(propX, !isXAxis);
        }
        if (rot.equals(Rotation.CLOCKWISE_180)
                || (!isXAxis && rot.equals(Rotation.CLOCKWISE_90))
                || (isXAxis && rot.equals(Rotation.COUNTERCLOCKWISE_90))) {
            return state.with(propRotation, state.get(propRotation) ^ 2);
        }

        return state.with(propRotation, state.get(propRotation));
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext contex) {
        if (state.get(propX)) {
            return makeCuboidShape(0, 0, 7, 16, 16, 9);
        }
        return makeCuboidShape(7, 0, 0, 9, 16, 16);
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
        return 0;
    }
}
