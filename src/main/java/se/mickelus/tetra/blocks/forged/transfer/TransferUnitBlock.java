package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.tetra.compat.forge.registries.ObjectHolder;
import se.mickelus.mutil.util.TileEntityOptional;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraItemAbilities;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.items.cell.ThermalCellItem;
import se.mickelus.tetra.items.forged.InsulatedPlateItem;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

@ParametersAreNonnullByDefault
public class TransferUnitBlock extends TetraWaterloggedBlock implements IInteractiveBlock, EntityBlock {
    public static final String identifier = "transfer_unit";

    public static final DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty plateProp = BooleanProperty.create("plate");
    public static final IntegerProperty cellProp = IntegerProperty.create("cell", 0, 2);
    public static final EnumProperty<EnumTransferConfig> configProp = EnumProperty.create("config", EnumTransferConfig.class);
    public static final EnumProperty<EnumTransferState> transferProp = EnumProperty.create("transfer", EnumTransferState.class);
    private static final ResourceLocation plateLootTable = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "forged/plate_break");
    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(TetraItemAbilities.pry, 1, Direction.SOUTH, 3, 11, 4, 6,
                    new PropertyMatcher().where(plateProp, equalTo(true)),
                    TransferUnitBlock::removePlate),
            new BlockInteraction(TetraItemAbilities.hammer, 1, Direction.SOUTH, 4, 10, 5, 9,
                    new PropertyMatcher().where(plateProp, equalTo(false)),
                    TransferUnitBlock::reconfigure),
    };
    private static final VoxelShape eastShape = box(3, 0, 1, 16, 12, 15);
    private static final VoxelShape northShape = box(1, 0, 0, 15, 12, 13);
    private static final VoxelShape westShape = box(0, 0, 1, 13, 12, 15);
    private static final VoxelShape southShape = box(1, 0, 3, 15, 12, 16);

    @ObjectHolder(registryName = "block", value = TetraMod.MOD_ID + ":" + identifier)
    public static TransferUnitBlock instance;

    public TransferUnitBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);

        registerDefaultState(defaultBlockState()
                .setValue(plateProp, false)
                .setValue(cellProp, 0)
                .setValue(configProp, EnumTransferConfig.send)
                .setValue(transferProp, EnumTransferState.none));
    }

    public static boolean removePlate(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction hitFace) {
        if (!world.isClientSide) {
            if (player != null) {
                BlockInteraction.dropLoot(plateLootTable, player, hand, (ServerLevel) world, blockState);
            } else {
                BlockInteraction.dropLoot(plateLootTable, (ServerLevel) world, pos, blockState);
            }
        }

        world.playSound(player, pos, SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1, 0.5f);
        world.setBlock(pos, blockState.setValue(plateProp, false), 3);

        return true;
    }

    public static boolean attachPlate(Level world, BlockPos pos, BlockState blockState, Player player) {
        world.playSound(player, pos, SoundEvents.METAL_PLACE, SoundSource.PLAYERS, 0.5f, 1);
        world.setBlock(pos, blockState.setValue(plateProp, true), 3);

        return true;
    }

    public static boolean reconfigure(Level world, BlockPos pos, BlockState blockState, @Nullable Player player, @Nullable InteractionHand hand, Direction hitFace) {
        EnumTransferConfig config = EnumTransferConfig.getNextConfiguration(blockState.getValue(configProp));

        world.playSound(player, pos, SoundEvents.ANVIL_HIT, SoundSource.PLAYERS, 1, 1);
        world.setBlock(pos, blockState.setValue(configProp, config), 3);

        world.getBlockEntity(pos, TransferUnitBlockEntity.type.get()).ifPresent(TransferUnitBlockEntity::updateTransferState);

        return true;
    }

    /**
     * Returns the effect with redstone power taken into consideration, if the block is powered with redstone the REDSTONE effect will be
     * translated into SEND/RECEIVE depending on the powered side.
     *
     * @return the effect with redstone power taken into consideration
     */
    public static EnumTransferConfig getEffectPowered(Level world, BlockPos pos, BlockState blockState) {
        EnumTransferConfig effect = blockState.getValue(configProp);
        if (effect.equals(EnumTransferConfig.redstone)) {
            return world.hasNeighborSignal(pos)
                    ? EnumTransferConfig.send
                    : EnumTransferConfig.receive;
        }
        return effect;
    }

    public static void setReceiving(Level world, BlockPos pos, BlockState blockState, boolean receiving) {
        if (receiving) {
            world.setBlock(pos, blockState.setValue(transferProp, EnumTransferState.receiving), Block.UPDATE_CLIENTS);
        } else {
            world.setBlock(pos, blockState.setValue(transferProp, EnumTransferState.none), Block.UPDATE_CLIENTS);
        }
    }

    public static boolean isReceiving(BlockState blockState) {
        return EnumTransferState.receiving.equals(blockState.getValue(transferProp));
    }

    public static void setSending(Level world, BlockPos pos, BlockState blockState, boolean sending) {
        if (sending) {
            world.setBlock(pos, blockState.setValue(transferProp, EnumTransferState.sending), Block.UPDATE_CLIENTS);
        } else {
            world.setBlock(pos, blockState.setValue(transferProp, EnumTransferState.none), Block.UPDATE_CLIENTS);
        }
    }

    public static boolean isSending(BlockState blockState) {
        return EnumTransferState.sending.equals(blockState.getValue(transferProp));
    }

    public static boolean hasPlate(BlockState blockState) {
        return blockState.getValue(plateProp);
    }

    public static void updateCellProp(Level world, BlockPos pos, boolean hasCell, int cellCharge) {
        BlockState blockState = world.getBlockState(pos);
        world.setBlock(pos, blockState.setValue(cellProp, hasCell ? cellCharge > 0 ? 2 : 1 : 0), 3);
    }

    public static Direction getFacing(BlockState blockState) {
        return blockState.getValue(facingProp);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ItemAbility> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(world, pos, blockState, blockState.getValue(facingProp), face, tools))
                .toArray(BlockInteraction[]::new);
    }

    private InteractionResult useInternal(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Direction blockFacing = state.getValue(facingProp);
        TransferUnitBlockEntity tile = TileEntityOptional.from(world, pos, TransferUnitBlockEntity.class).orElse(null);
        ItemStack heldStack = player.getItemInHand(hand);

        if (tile == null) {
            return InteractionResult.FAIL;
        }

        if (hit.getDirection().equals(Direction.UP)) {
            if (tile.hasCell()) { // remove cell
                ItemStack cell = tile.removeCell();
                if (player.getInventory().add(cell)) {
                    player.playSound(SoundEvents.ITEM_PICKUP, 1, 1);
                } else {
                    popResource(world, pos.above(), cell);
                }

                world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.5f, 0.6f);

                world.sendBlockUpdated(pos, state, state, 3);

                if (!player.level().isClientSide) {
                    BlockUseCriterion.trigger((ServerPlayer) player, state, ItemStack.EMPTY);
                }

                return InteractionResult.SUCCESS;
            } else if (heldStack.getItem() instanceof ThermalCellItem) { // put cell
                tile.putCell(heldStack);
                player.setItemInHand(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.5f, 0.5f);
                world.sendBlockUpdated(pos, state, state, 3);

                if (!player.level().isClientSide) {
                    BlockUseCriterion.trigger((ServerPlayer) player, state, ItemStack.EMPTY);
                }

                return InteractionResult.SUCCESS;
            }
        } else if (blockFacing.equals(hit.getDirection().getOpposite()) // attach plate
                && heldStack.getItem() instanceof InsulatedPlateItem
                && !state.getValue(plateProp)) {

            attachPlate(world, pos, state, player);
            heldStack.shrink(1);

            if (!player.level().isClientSide) {
                BlockUseCriterion.trigger((ServerPlayer) player, state, ItemStack.EMPTY);
            }

            return InteractionResult.SUCCESS;
        }

        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
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
            TileEntityOptional.from(world, pos, TransferUnitBlockEntity.class)
                    .ifPresent(tile -> {
                        if (tile.hasCell()) {
                            Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tile.getCell().copy());
                        }
                    });

            TileEntityOptional.from(world, pos, TransferUnitBlockEntity.class).ifPresent(BlockEntity::setRemoved);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        if (!pos.relative(world.getBlockState(pos).getValue(facingProp)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, TransferUnitBlockEntity.class)
                    .ifPresent(TransferUnitBlockEntity::updateTransferState);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(facingProp);

        switch (facing) {
            case NORTH:
                return northShape;
            case EAST:
                return eastShape;
            case SOUTH:
                return southShape;
            case WEST:
                return westShape;
            default:
                return null;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp, configProp, plateProp, cellProp, transferProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(facingProp, context.getHorizontalDirection());
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }


    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(facingProp, rotation.rotate(state.getValue(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(facingProp)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransferUnitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return !level.isClientSide
                ? getTicker(entityType, TransferUnitBlockEntity.type.get(), (lvl, pos, blockState, tile) -> tile.serverTick(lvl, pos, blockState))
                : null;
    }
}
