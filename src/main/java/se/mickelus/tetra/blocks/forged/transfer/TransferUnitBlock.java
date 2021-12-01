package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.forged.InsulatedPlateItem;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class TransferUnitBlock extends TetraWaterloggedBlock implements IInteractiveBlock {
    public static final DirectionProperty facingProp = HorizontalBlock.FACING;
    public static final BooleanProperty plateProp = BooleanProperty.create("plate");
    public static final IntegerProperty cellProp = IntegerProperty.create("cell", 0, 2);
    public static final EnumProperty<EnumTransferConfig> configProp = EnumProperty.create("config", EnumTransferConfig.class);
    public static final EnumProperty<EnumTransferState> transferProp = EnumProperty.create("transfer", EnumTransferState.class);

    private static final ResourceLocation plateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/plate_break");

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(ToolTypes.pry, 1, Direction.SOUTH, 3, 11, 4, 6,
                    new PropertyMatcher().where(plateProp, equalTo(true)),
                    TransferUnitBlock::removePlate),
            new BlockInteraction(ToolTypes.hammer, 1, Direction.SOUTH, 4, 10, 5, 9,
                    new PropertyMatcher().where(plateProp, equalTo(false)),
                    TransferUnitBlock::reconfigure),
    };

    private static final VoxelShape eastShape =  box(16, 0, 1,  3, 12, 15);
    private static final VoxelShape northShape = box(1, 0, 0,  15, 12, 13);
    private static final VoxelShape westShape =  box(0, 0, 1,  13, 12, 15);
    private static final VoxelShape southShape = box(1, 0, 16, 15, 12, 3);

    public static final String unlocalizedName = "transfer_unit";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static TransferUnitBlock instance;

    public TransferUnitBlock() {
        super(ForgedBlockCommon.propertiesNotSolid);

        setRegistryName(unlocalizedName);

        hasItem = true;

        registerDefaultState(defaultBlockState()
                .setValue(plateProp, false)
                .setValue(cellProp, 0)
                .setValue(configProp, EnumTransferConfig.a)
                .setValue(transferProp, EnumTransferState.none));
    }

    @Override
    public void clientInit() {
        RenderTypeLookup.setRenderLayer(this, RenderType.cutout());
    }

    public static boolean removePlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace) {
        if (!world.isClientSide) {
            if (player != null) {
                BlockInteraction.dropLoot(plateLootTable, player, hand, (ServerWorld) world, blockState);
            } else {
                BlockInteraction.dropLoot(plateLootTable, (ServerWorld) world, pos, blockState);
            }
        }

        world.playSound(player, pos, SoundEvents.SHIELD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
        world.setBlock(pos, blockState.setValue(plateProp, false), 3);

        return true;
    }

    public static boolean attachPlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player) {
        world.playSound(player, pos, SoundEvents.METAL_PLACE, SoundCategory.PLAYERS, 0.5f, 1);
        world.setBlock(pos, blockState.setValue(plateProp, true), 3);

        return true;
    }

    public static boolean reconfigure(World world, BlockPos pos, BlockState blockState, @Nullable PlayerEntity player, @Nullable Hand hand, Direction hitFace) {
        EnumTransferConfig config = EnumTransferConfig.getNextConfiguration(blockState.getValue(configProp));

        world.playSound(player, pos, SoundEvents.ANVIL_HIT, SoundCategory.PLAYERS, 1, 1);
        world.setBlock(pos, blockState.setValue(configProp, config), 3);

        TileEntityOptional.from(world, pos, TransferUnitTile.class).ifPresent(TransferUnitTile::updateTransferState);

        return true;
    }

    /**
     * Returns the effect with redstone power taken into consideration, if the block is powered with redstone the REDSTONE effect will be
     * translated into SEND/RECEIVE depending on the powered side.
     * @return the effect with redstone power taken into consideration
     */
    public static EnumTransferEffect getEffectPowered(World world, BlockPos pos, BlockState blockState) {
        EnumTransferEffect effect = EnumTransferEffect.fromConfig(blockState.getValue(configProp), 0);
        if (effect.equals(EnumTransferEffect.redstone)) {
            Direction facing = blockState.getValue(facingProp);

            if (world.hasSignal(pos.relative(facing.getClockWise()), facing.getClockWise())) {
                return EnumTransferEffect.send;
            }

            if (world.hasSignal(pos.relative(facing.getCounterClockWise()), facing.getCounterClockWise())) {
                return EnumTransferEffect.receive;
            }
        }
        return effect;
    }

    public static void setReceiving(World world, BlockPos pos, BlockState blockState, boolean receiving) {
        EnumTransferState newState = receiving ? EnumTransferState.receiving : EnumTransferState.none;
    }

    public static boolean isReceiving(BlockState blockState) {
        return EnumTransferState.receiving.equals(blockState.getValue(transferProp));
    }

    public static void setSending(World world, BlockPos pos, BlockState blockState, boolean sending) {
        EnumTransferState newState = sending ? EnumTransferState.sending : EnumTransferState.none;
    }

    public static boolean isSending(BlockState blockState) {
        return EnumTransferState.sending.equals(blockState.getValue(transferProp));
    }

    public static boolean hasPlate(BlockState blockState) {
        return blockState.getValue(plateProp);
    }

    public static void updateCellProp(World world, BlockPos pos, boolean hasCell, int cellCharge) {
        BlockState blockState = world.getBlockState(pos);
        world.setBlock(pos, blockState.setValue(cellProp, hasCell ? cellCharge > 0 ? 2 : 1 : 0), 3);
    }

    public static Direction getFacing(BlockState blockState) {
        return blockState.getValue(facingProp);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(World world, BlockPos pos, BlockState blockState, Direction face, Collection<ToolType> tools) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(world, pos, blockState, blockState.getValue(facingProp), face, tools))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Direction blockFacing = state.getValue(facingProp);
        TransferUnitTile tile = TileEntityOptional.from(world, pos, TransferUnitTile.class).orElse(null);
        ItemStack heldStack = player.getItemInHand(hand);

        if (tile == null) {
            return ActionResultType.FAIL;
        }

        if (hit.getDirection().equals(Direction.UP)) {
            if (tile.hasCell()) { // remove cell
                ItemStack cell = tile.removeCell();
                if (player.inventory.add(cell)) {
                    player.playSound(SoundEvents.ITEM_PICKUP, 1, 1);
                } else {
                    popResource(world, pos.above(), cell);
                }

                world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.6f);

                world.sendBlockUpdated(pos, state, state, 3);

                if (!player.level.isClientSide) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, state, ItemStack.EMPTY);
                }

                return ActionResultType.SUCCESS;
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) { // put cell
                tile.putCell(heldStack);
                player.setItemInHand(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.5f);
                world.sendBlockUpdated(pos, state, state, 3);

                if (!player.level.isClientSide) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, state, ItemStack.EMPTY);
                }

                return ActionResultType.SUCCESS;
            }
        } else if (blockFacing.equals(hit.getDirection().getOpposite()) // attach plate
                && heldStack.getItem() instanceof InsulatedPlateItem
                && !state.getValue(plateProp)) {

            attachPlate(world, pos, state, player);
            heldStack.shrink(1);

            if (!player.level.isClientSide) {
                BlockUseCriterion.trigger((ServerPlayerEntity) player, state, ItemStack.EMPTY);
            }

            return ActionResultType.SUCCESS;
        }

        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, TransferUnitTile.class)
                    .ifPresent(tile -> {
                        if (tile.hasCell()) {
                            InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tile.getCell().copy());
                        }
                    });

            TileEntityOptional.from(world, pos, TransferUnitTile.class).ifPresent(TileEntity::setRemoved);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        if (!pos.relative(world.getBlockState(pos).getValue(facingProp)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, TransferUnitTile.class)
                    .ifPresent(TransferUnitTile::updateTransferState);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp, configProp, plateProp, cellProp, transferProp);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TransferUnitTile();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context)
                .setValue(facingProp, context.getHorizontalDirection());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
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
}
