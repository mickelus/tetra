package se.mickelus.tetra.blocks.workbench;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import se.mickelus.mutil.util.TileEntityOptional;
import se.mickelus.tetra.blocks.ICraftingEffectProviderBlock;
import se.mickelus.tetra.blocks.ISchematicProviderBlock;
import se.mickelus.tetra.blocks.IToolProviderBlock;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.data.DataManager;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractWorkbenchBlock extends TetraBlock implements IInteractiveBlock, EntityBlock {
    public AbstractWorkbenchBlock(Properties properties) {
        super(properties);
    }

    private InteractionResult useInternal(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult interactionResult = BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
        if (interactionResult != InteractionResult.PASS || hand == InteractionHand.OFF_HAND) {
            return interactionResult;
        }

        if (!world.isClientSide) {
            TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .ifPresent(te -> player.openMenu(te, pos));
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
            TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .map(te -> te.getItemHandler(null))
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

    /**
     * Returns a stream of block state/position pairs around the given position where each block in the stream implements IToolProviderBlock
     *
     * @param world
     * @param pos
     * @return
     */
    protected Stream<Pair<BlockPos, BlockState>> getToolProviderBlockStream(Level world, BlockPos pos) {
        return BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof IToolProviderBlock)
                .filter(pair -> ((IToolProviderBlock) pair.getSecond().getBlock()).canProvideTools(world, pair.getFirst(), pos));
    }

    public Collection<ItemAbility> getTools(Level world, BlockPos pos, BlockState blockState) {
        return getToolProviderBlockStream(world, pos)
                .map(pair -> ((IToolProviderBlock) pair.getSecond().getBlock()).getTools(world, pair.getFirst(), pair.getSecond()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public int getToolLevel(Level world, BlockPos pos, BlockState blockState, ItemAbility toolAction) {
        return getToolProviderBlockStream(world, pos)
                .map(pair -> ((IToolProviderBlock) pair.getSecond().getBlock()).getToolLevel(world, pair.getFirst(), pair.getSecond(), toolAction))
                .max(Integer::compare)
                .orElse(-1);
    }

    public Map<ItemAbility, Integer> getToolLevels(Level world, BlockPos pos, BlockState blockState) {
        return getToolProviderBlockStream(world, pos)
                .map(pair -> ((IToolProviderBlock) pair.getSecond().getBlock()).getToolLevels(world, pair.getFirst(), pair.getSecond()))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::max));
    }

    private Pair<BlockPos, BlockState> getProvidingBlockstate(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack,
            ItemAbility toolAction, int level) {
        return getToolProviderBlockStream(world, pos)
                .filter(pair -> ((IToolProviderBlock) pair.getSecond().getBlock()).getToolLevel(world, pair.getFirst(), pair.getSecond(), toolAction) >= level)
                .findFirst()
                .orElse(null);
    }

    public ItemStack onCraftConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing, Player player,
            ItemAbility requiredTool, int requiredLevel, boolean consumeResources) {
        Pair<BlockPos, BlockState> provider = getProvidingBlockstate(world, pos, blockState, targetStack, requiredTool, requiredLevel);

        if (provider != null) {
            IToolProviderBlock block = ((IToolProviderBlock) provider.getSecond().getBlock());
            return block.onCraftConsumeTool(world, provider.getFirst(), provider.getSecond(), targetStack, slot, isReplacing, player, requiredTool,
                    requiredLevel, consumeResources);
        }

        return null;
    }

    public ItemStack onActionConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, Player player,
            ItemAbility requiredTool, int requiredLevel, boolean consumeResources) {
        Pair<BlockPos, BlockState> provider = getProvidingBlockstate(world, pos, blockState, targetStack, requiredTool, requiredLevel);

        if (provider != null) {
            IToolProviderBlock block = ((IToolProviderBlock) provider.getSecond().getBlock());
            return block.onActionConsumeTool(world, provider.getFirst(), provider.getSecond(), targetStack, player, requiredTool,
                    requiredLevel, consumeResources);
        }

        return null;
    }

    public ResourceLocation[] getSchematics(Level world, BlockPos pos, BlockState blockState) {
        return Stream.concat(
                        DataManager.instance.unlockData.getData().values().stream()
                                .filter(unlock -> unlock.block != null && unlock.schematics != null && unlock.schematics.length > 0)
                                .filter(unlock -> BlockPos.betweenClosedStream(unlock.bounds.move(pos)).anyMatch(offsetPos -> unlock.block.test(world.getBlockState(offsetPos))))
                                .map(unlock -> unlock.schematics),
                        BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                                .filter(pair -> pair.getSecond().getBlock() instanceof ISchematicProviderBlock)
                                .filter(pair -> ((ISchematicProviderBlock) pair.getSecond().getBlock()).canUnlockSchematics(world, pair.getFirst(), pos))
                                .map(pair -> ((ISchematicProviderBlock) pair.getSecond().getBlock()).getSchematics(world, pair.getFirst(), blockState)))
                .flatMap(Stream::of)
                .toArray(ResourceLocation[]::new);
    }

    public ResourceLocation[] getCraftingEffects(Level world, BlockPos pos, BlockState blockState) {
        return Stream.concat(
                        DataManager.instance.unlockData.getData().values().stream()
                                .filter(unlock -> unlock.block != null && unlock.effects != null && unlock.effects.length > 0)
                                .filter(unlock -> BlockPos.betweenClosedStream(unlock.bounds.move(pos)).anyMatch(offsetPos -> unlock.block.test(world.getBlockState(offsetPos))))
                                .map(unlock -> unlock.effects), BlockPos.betweenClosedStream(pos.offset(-2, 0, -2), pos.offset(2, 4, 2))
                                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                                .filter(pair -> pair.getSecond().getBlock() instanceof ICraftingEffectProviderBlock)
                                .filter(pair -> ((ICraftingEffectProviderBlock) pair.getSecond().getBlock()).canUnlockCraftingEffects(world, pair.getFirst(), pos))
                                .map(pair -> ((ICraftingEffectProviderBlock) pair.getSecond().getBlock()).getCraftingEffects(world, pair.getFirst(), blockState)))
                .flatMap(Stream::of)
                .toArray(ResourceLocation[]::new);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ItemAbility> tools) {
        if (face == Direction.UP) {
            return TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .map(WorkbenchTile::getInteractions)
                    .orElse(new BlockInteraction[0]);
        }

        return new BlockInteraction[0];
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new WorkbenchTile(p_153215_, p_153216_);
    }
}
