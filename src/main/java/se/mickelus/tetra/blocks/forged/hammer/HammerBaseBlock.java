package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.salvage.TileBlockInteraction;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;
import static net.minecraft.world.level.material.Fluids.WATER;
import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;
@ParametersAreNonnullByDefault
public class HammerBaseBlock extends TetraBlock implements IInteractiveBlock, EntityBlock {
    public static final DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;

    public static final String qualityImprovementKey = "quality";

    public static final String unlocalizedName = "hammer_base";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static HammerBaseBlock instance;

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new TileBlockInteraction<>(ToolTypes.pry, 1, Direction.EAST, 5, 11, 10, 12,
                    HammerBaseTile.class, tile -> tile.getEffect(true) != null,
                    (world, pos, blockState, player, hand, hitFace) -> removeModule(world, pos, blockState, player, hand, hitFace, true)),
            new TileBlockInteraction<>(ToolTypes.pry, 1, Direction.WEST, 5, 11, 10, 12,
                    HammerBaseTile.class, tile -> tile.getEffect(false) != null,
                    (world, pos, blockState, player, hand, hitFace) -> removeModule(world, pos, blockState, player, hand, hitFace, false))
    };

    public HammerBaseBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);

        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(facingProp);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientInit() {
        BlockEntityRenderers.register(HammerBaseTile.type, HammerBaseRenderer::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final BlockGetter world, final List<Component> tooltip, final TooltipFlag advanced) {
        tooltip.add(locationTooltip);
        tooltip.add(new TextComponent(" "));
        tooltip.add(new TranslatableComponent("block.multiblock_hint.1x2x1")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public boolean isFunctional(Level world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::isFunctional)
                .orElse(false);
    }

    public void consumeFuel(Level world, BlockPos pos) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .ifPresent(HammerBaseTile::consumeFuel);
    }

    public int getHammerLevel(Level world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(HammerBaseTile::getHammerLevel)
                .orElse(0);
    }

    public static boolean removeModule(Level world, BlockPos pos, BlockState blockState, @Nullable Player player, @Nullable InteractionHand hand, Direction hitFace, boolean isA) {
        ItemStack moduleStack = TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(te -> te.removeModule(isA))
                .map(ItemStack::new)
                .orElse(null);

        if (moduleStack != null && !world.isClientSide) {
            if (player != null && player.getInventory().add(moduleStack)) {
                player.playSound(SoundEvents.ITEM_PICKUP, 1, 1);
            } else {
                popResource(world, pos.relative(hitFace), moduleStack);
            }
        }

        world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.5f, 0.6f);

        return true;
    }

    public ItemStack applyCraftEffects(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing,
            Player player, ToolAction requiredTool, int requiredLevel, boolean consumeResources) {
        if (consumeResources) {
            consumeFuel(world, pos);
        }

        if (isReplacing) {
            int preciseLevel = TileEntityOptional.from(world, pos, HammerBaseTile.class)
                    .map(te -> te.getEffectLevel(HammerEffect.precise))
                    .orElse(0);

            if (preciseLevel > 0) {
                ItemStack upgradedStack = targetStack.copy();

                ItemModuleMajor.addImprovement(upgradedStack, slot, qualityImprovementKey, preciseLevel);
                return upgradedStack;
            }
        }

        return targetStack;
    }

    public ItemStack applyActionEffects(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, Player player,
            ToolAction requiredTool, int requiredLevel, boolean consumeResources) {
        if (consumeResources) {
            consumeFuel(world, pos);
        }
        return targetStack;
    }

    private Map<String, String> getAdvancementData(Level world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, HammerBaseTile.class)
                .map(tile -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("functional", String.valueOf(tile.isFunctional()));

                    Optional.ofNullable(tile.getEffect(true))
                            .ifPresent(module -> result.put("moduleA", module.toString()));
                    Optional.ofNullable(tile.getEffect(false))
                            .ifPresent(module -> result.put("moduleB", module.toString()));

                    return result;
                })
                .orElseGet(Collections::emptyMap);
    }

    @Override
    public InteractionResult use(final BlockState blockState, final Level world, final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult rayTraceResult) {
        Direction blockFacing = blockState.getValue(facingProp);
        HammerBaseTile te = TileEntityOptional.from(world, pos, HammerBaseTile.class).orElse(null);
        ItemStack heldStack = player.getItemInHand(hand);
        Direction facing = rayTraceResult.getDirection();

        if (te == null) {
            return InteractionResult.FAIL;
        }

        if (blockFacing.getAxis().equals(facing.getAxis())) {
            int slotIndex = blockFacing.equals(facing)? 0 : 1;
            if (te.hasCellInSlot(slotIndex)) {
                ItemStack cell = te.removeCellFromSlot(slotIndex);
                if (player.getInventory().add(cell)) {
                    player.playSound(SoundEvents.ITEM_PICKUP, 1, 1);
                } else {
                    popResource(world, pos.relative(facing), cell);
                }

                world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.5f, 0.6f);

                if (!player.level.isClientSide) {
                    BlockUseCriterion.trigger((ServerPlayer) player, world.getBlockState(pos), ItemStack.EMPTY, getAdvancementData(world, pos));
                }

                return InteractionResult.sidedSuccess(player.level.isClientSide);
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) {
                te.putCellInSlot(heldStack, slotIndex);
                player.setItemInHand(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.5f, 0.5f);

                if (!player.level.isClientSide) {
                    BlockUseCriterion.trigger((ServerPlayer) player, world.getBlockState(pos), heldStack, getAdvancementData(world, pos));
                }

                return InteractionResult.sidedSuccess(player.level.isClientSide);
            }
        } else {
            boolean isA = Rotation.CLOCKWISE_90.rotate(blockFacing).equals(facing);

            if (te.getEffect(isA) == null) {
                boolean success = te.setModule(isA, heldStack.getItem());
                if (success) {
                    if (!player.level.isClientSide) {
                        BlockUseCriterion.trigger((ServerPlayer) player, world.getBlockState(pos), heldStack, getAdvancementData(world, pos));
                    }

                    world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.5f, 0.5f);
                    heldStack.shrink(1);

                    if (world.isClientSide) {
                        InteractiveBlockOverlay.markDirty();
                    }

                    return InteractionResult.sidedSuccess(player.level.isClientSide);
                }
            }
        }

        return BlockInteraction.attemptInteraction(world, world.getBlockState(pos), pos, player, hand, rayTraceResult);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, HammerBaseTile.class)
                    .ifPresent(tile -> {
                        for (int i = 0; i < 2; i++) {
                            if (tile.hasCellInSlot(i)) {
                                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tile.getStackInSlot(i).copy());
                            }
                        }

                        Stream.of(tile.getEffect(true), tile.getEffect(false))
                                .filter(Objects::nonNull)
                                .map(HammerEffect::getItem)
                                .map(ItemStack::new)
                                .forEach(stack -> Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack));
                    });

            TileEntityOptional.from(world, pos, HammerBaseTile.class).ifPresent(BlockEntity::setRemoved);
        }
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, final BlockState state, final Direction face, final Collection<ToolAction> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(world, pos, state, state.getValue(facingProp), face, tools))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        TileEntityOptional.from(world, currentPos, HammerBaseTile.class).ifPresent(HammerBaseTile::updateRedstonePower);
        if (Direction.DOWN.equals(facing) && !HammerHeadBlock.instance.equals(facingState.getBlock())) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    // based on same method implementation in BedBlock
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockState headState = HammerHeadBlock.instance.defaultBlockState()
                .setValue(WATERLOGGED, world.getFluidState(pos.below()).getType() == WATER);
        world.setBlock(pos.below(), headState, 3);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos().below()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(facingProp, context.getHorizontalDirection().getOpposite());
        }

        // returning null here stops the block from being placed
        return null;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        TileEntityOptional.from(world, pos, HammerBaseTile.class).ifPresent(HammerBaseTile::updateRedstonePower);
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(facingProp, rotation.rotate(state.getValue(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(facingProp)));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new HammerBaseTile(p_153215_, p_153216_);
    }
}
