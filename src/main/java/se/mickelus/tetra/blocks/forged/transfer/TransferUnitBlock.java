package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.block.*;
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
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.forged.ItemVentPlate;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class TransferUnitBlock extends TetraWaterloggedBlock implements IBlockCapabilityInteractive {
    public static final DirectionProperty facingProp = HorizontalBlock.HORIZONTAL_FACING;
    public static final BooleanProperty plateProp = BooleanProperty.create("plate");
    public static final IntegerProperty cellProp = IntegerProperty.create("cell", 0, 2);
    public static final EnumProperty<EnumTransferConfig> configProp = EnumProperty.create("config", EnumTransferConfig.class);
    public static final EnumProperty<EnumTransferState> transferProp = EnumProperty.create("transfer", EnumTransferState.class);

    private static final ResourceLocation plateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/plate_break");

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.pry, 1, Direction.SOUTH, 3, 11, 4, 6,
                    new PropertyMatcher().where(plateProp, equalTo(true)),
                    TransferUnitBlock::removePlate),
            new BlockInteraction(Capability.hammer, 1, Direction.SOUTH, 4, 10, 5, 9,
                    new PropertyMatcher().where(plateProp, equalTo(false)),
                    TransferUnitBlock::reconfigure),
    };

    private static final VoxelShape eastShape =  makeCuboidShape(16, 0, 1,  3, 12, 15);
    private static final VoxelShape northShape = makeCuboidShape(1, 0, 0,  15, 12, 13);
    private static final VoxelShape westShape =  makeCuboidShape(0, 0, 1,  13, 12, 15);
    private static final VoxelShape southShape = makeCuboidShape(1, 0, 16, 15, 12, 3);

    public static final String unlocalizedName = "transfer_unit";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static TransferUnitBlock instance;

    public TransferUnitBlock() {
        super(ForgedBlockCommon.properties);

        setRegistryName(unlocalizedName);

        hasItem = true;

        setDefaultState(getDefaultState()
                .with(plateProp, false)
                .with(cellProp, 0)
                .with(configProp, EnumTransferConfig.a)
                .with(transferProp, EnumTransferState.none));
    }

    public static boolean removePlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace) {
        if (!world.isRemote) {
            BlockInteraction.dropLoot(plateLootTable, player, hand, (ServerWorld) world, blockState);
        }

        world.playSound(player, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
        world.setBlockState(pos, blockState.with(plateProp, false), 3);

        return true;
    }

    public static boolean attachPlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player) {
        world.playSound(player, pos, SoundEvents.BLOCK_METAL_PLACE, SoundCategory.PLAYERS, 0.5f, 1);
        world.setBlockState(pos, blockState.with(plateProp, true), 3);

        return true;
    }

    public static boolean reconfigure(World world, BlockPos pos, BlockState blockState, PlayerEntity player, Hand hand, Direction hitFace) {
        EnumTransferConfig config = EnumTransferConfig.getNextConfiguration(blockState.get(configProp));

        world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 1);
        world.setBlockState(pos, blockState.with(configProp, config), 3);

        TileEntityOptional.from(world, pos, TransferUnitTile.class).ifPresent(TransferUnitTile::updateTransferState);

        return true;
    }

    /**
     * Returns the effect with redstone power taken into consideration, if the block is powered with redstone the REDSTONE effect will be
     * translated into SEND/RECEIVE depending on the powered side.
     * @return the effect with redstone power taken into consideration
     */
    public static EnumTransferEffect getEffectPowered(World world, BlockPos pos, BlockState blockState) {
        EnumTransferEffect effect = EnumTransferEffect.fromConfig(blockState.get(configProp), 0);
        if (effect.equals(EnumTransferEffect.redstone)) {
            Direction facing = blockState.get(facingProp);

            if (world.isSidePowered(pos.offset(facing.rotateY()), facing.rotateY())) {
                return EnumTransferEffect.send;
            }

            if (world.isSidePowered(pos.offset(facing.rotateYCCW()), facing.rotateYCCW())) {
                return EnumTransferEffect.receive;
            }
        }
        return effect;
    }

    public static void setReceiving(World world, BlockPos pos, BlockState blockState, boolean receiving) {
        if (receiving) {
            TransferUnitBlock.setSending(world, pos, blockState, false);
            world.setBlockState(pos, blockState.with(transferProp, EnumTransferState.receiving), 3);
        } else {
            world.setBlockState(pos, blockState.with(transferProp, EnumTransferState.none), 3);
        }
    }

    public static boolean isReceiving(BlockState blockState) {
        return EnumTransferState.receiving.equals(blockState.get(transferProp));
    }

    public static void setSending(World world, BlockPos pos, BlockState blockState, boolean sending) {
        if (sending) {
            TransferUnitBlock.setReceiving(world, pos, blockState, false);
            world.setBlockState(pos, blockState.with(transferProp, EnumTransferState.sending), 3);
        } else {
            world.setBlockState(pos, blockState.with(transferProp, EnumTransferState.none), 3);
        }
    }

    public static boolean isSending(BlockState blockState) {
        return EnumTransferState.sending.equals(blockState.get(transferProp));
    }

    public static boolean hasPlate(BlockState blockState) {
        return blockState.get(plateProp);
    }

    public static void updateCellProp(World world, BlockPos pos, boolean hasCell, int cellCharge) {
        BlockState blockState = world.getBlockState(pos);
        world.setBlockState(pos, blockState.with(cellProp, hasCell ? cellCharge > 0 ? 2 : 1 : 0), 3);
    }

    public static Direction getFacing(BlockState blockState) {
        return blockState.get(facingProp);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(BlockState blockState, Direction face, Collection<Capability> capabilities) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(blockState, blockState.get(facingProp), face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Direction blockFacing = state.get(facingProp);
        TransferUnitTile tile = TileEntityOptional.from(world, pos, TransferUnitTile.class).orElse(null);
        ItemStack heldStack = player.getHeldItem(hand);

        if (tile == null) {
            return false;
        }

        if (hit.getFace().equals(Direction.UP)) {
            if (tile.hasCell()) { // remove cell
                ItemStack cell = tile.removeCell();
                if (player.inventory.addItemStackToInventory(cell)) {
                    player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, 1);
                } else {
                    spawnAsEntity(world, pos.up(), cell);
                }

                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.6f);

                world.notifyBlockUpdate(pos, state, state, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, state, ItemStack.EMPTY);
                }

                return true;
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) { // put cell
                tile.putCell(heldStack);
                player.setHeldItem(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.5f);
                world.notifyBlockUpdate(pos, state, state, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, state, ItemStack.EMPTY);
                }

                return true;
            }
        } else if (blockFacing.equals(hit.getFace().getOpposite()) // attach plate
                && heldStack.getItem() instanceof ItemVentPlate
                && !state.get(plateProp)) {

            attachPlate(world, pos, state, player);
            heldStack.shrink(1);

            if (!player.world.isRemote) {
                BlockUseCriterion.trigger((ServerPlayerEntity) player, state, ItemStack.EMPTY);
            }

            return true;
        }

        return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, TransferUnitTile.class)
                    .ifPresent(tile -> {
                        if (tile.hasCell()) {
                            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tile.getCell().copy());
                        }
                    });

            TileEntityOptional.from(world, pos, TransferUnitTile.class).ifPresent(TileEntity::remove);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        if (!pos.offset(world.getBlockState(pos).get(facingProp)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, TransferUnitTile.class)
                    .ifPresent(TransferUnitTile::updateTransferState);
        }
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Direction facing = state.get(facingProp);

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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
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
                .with(facingProp, context.getPlacementHorizontalFacing());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.with(facingProp, rotation.rotate(state.get(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(facingProp)));
    }
}
