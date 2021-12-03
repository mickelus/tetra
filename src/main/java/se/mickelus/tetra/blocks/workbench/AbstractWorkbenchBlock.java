package se.mickelus.tetra.blocks.workbench;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractWorkbenchBlock extends TetraBlock implements IInteractiveBlock, EntityBlock {
    public AbstractWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult interactionResult = BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
        if (interactionResult != InteractionResult.PASS || hand == InteractionHand.OFF_HAND) {
            return interactionResult;
        }

        if (!world.isClientSide) {
            TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .ifPresent(te -> NetworkHooks.openGui((ServerPlayer) player, te, pos));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                    .orElse(LazyOptional.empty())
                    .ifPresent(cap -> {
                        for (int i = 0; i < cap.getSlots(); i++) {
                            ItemStack itemStack = cap.getStackInSlot(i);
                            if (!itemStack.isEmpty()) {
                                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemStack.copy());
                            }
                        }
                    });

            TileEntityOptional.from(world, pos, WorkbenchTile.class).ifPresent(BlockEntity::setRemoved);
        }
    }

    @Override
    public Collection<ToolAction> getTools(Level world, BlockPos pos, BlockState blockState) {
        return BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canProvideTools(world, pair.getFirst(), pos))
                .map(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getTools(world, pair.getFirst(), pair.getSecond()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public int getToolLevel(Level world, BlockPos pos, BlockState blockState, ToolAction ToolAction) {
        return BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canProvideTools(world, pair.getFirst(), pos))
                .map(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getToolLevel(world, pair.getFirst(), pair.getSecond(), ToolAction))
                .max(Integer::compare)
                .orElse(-1);
    }

    private Pair<BlockPos, BlockState> getProvidingBlockstate(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack,
            ToolAction ToolAction, int level) {
        return BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canProvideTools(world, pair.getFirst(), pos))
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getToolLevel(world, pair.getFirst(), pair.getSecond(), ToolAction) >= level)
                .findFirst()
                .orElse(null);
    }

    @Override
    public ItemStack onCraftConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing, Player player,
            ToolAction requiredTool, int requiredLevel, boolean consumeResources) {
        Pair<BlockPos, BlockState> provider = getProvidingBlockstate(world, pos, blockState, targetStack, requiredTool, requiredLevel);

        if (provider != null) {
            ITetraBlock block = ((ITetraBlock) provider.getSecond().getBlock());
            return block.onCraftConsumeTool(world, provider.getFirst(), provider.getSecond(), targetStack, slot, isReplacing, player, requiredTool,
                    requiredLevel, consumeResources);
        }

        return null;
    }

    @Override
    public ItemStack onActionConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, Player player,
            ToolAction requiredTool, int requiredLevel, boolean consumeResources) {
        Pair<BlockPos, BlockState> provider = getProvidingBlockstate(world, pos, blockState, targetStack, requiredTool, requiredLevel);

        if (provider != null) {
            ITetraBlock block = ((ITetraBlock) provider.getSecond().getBlock());
            return block.onActionConsumeTool(world, provider.getFirst(), provider.getSecond(), targetStack, player, requiredTool,
                    requiredLevel, consumeResources);
        }

        return null;
    }

    @Override
    public ResourceLocation[] getSchematics(Level world, BlockPos pos, BlockState blockState) {
        return BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canUnlockSchematics(world, pair.getFirst(), pos))
                .map(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getSchematics(world, pair.getFirst(), blockState))
                .flatMap(Stream::of)
                .toArray(ResourceLocation[]::new);
    }

    @Override
    public ResourceLocation[] getCraftingEffects(Level world, BlockPos pos, BlockState blockState) {
        return BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canUnlockCraftingEffects(world, pair.getFirst(), pos))
                .map(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getCraftingEffects(world, pair.getFirst(), blockState))
                .flatMap(Stream::of)
                .toArray(ResourceLocation[]::new);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolAction> tools) {
        if (face == Direction.UP) {
            return TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .map(WorkbenchTile::getInteractions)
                    .orElse(new BlockInteraction[0]);
        }

        return new BlockInteraction[0];
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new WorkbenchTile(p_153215_, p_153216_);
    }
}
