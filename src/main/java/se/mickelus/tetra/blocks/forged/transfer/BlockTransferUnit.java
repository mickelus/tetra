package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.forged.ItemVentPlate;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class BlockTransferUnit extends TetraBlock implements ITileEntityProvider, IBlockCapabilityInteractive {
    public static final PropertyDirection propFacing = HorizontalBlock.FACING;
    public static final PropertyBool propPlate = PropertyBool.create("plate");
    public static final PropertyInteger propCell = PropertyInteger.create("cell", 0, 2);
    public static final PropertyInteger propTransfer = PropertyInteger.create("transfer", 0, 2);

    private static final ResourceLocation plateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/plate_break");

    public static final BlockInteraction[] interactions = new BlockInteraction[]{
            new BlockInteraction(Capability.pry, 1, Direction.SOUTH, 5, 7, 2, 5,
                    new PropertyMatcher().where(propPlate, equalTo(true)),
                    (world, pos, blockState, player, hand, hitFace) -> removePlate(world, pos, blockState, player, hand, hitFace)),
            new BlockInteraction(Capability.hammer, 1, Direction.SOUTH, 11, 13, 2, 5,
                    new PropertyMatcher().where(propPlate, equalTo(false)),
                    (world, pos, blockState, player, hand, hitFace) -> reconfigure(world, pos, blockState, player, hand, hitFace)),
    };

    private static final AxisAlignedBB aabbEast = new AxisAlignedBB(0.1875,  0.0, 0.0625, 1, 0.75, 0.9375);
    private static final AxisAlignedBB aabbNorth = new AxisAlignedBB(0.0625,  0.0, 0.0,  0.9375, 0.75, 0.8125);
    private static final AxisAlignedBB aabbWest = new AxisAlignedBB(0.0, 0.0, 0.0625,  0.8125, 0.75, 0.9375);
    private static final AxisAlignedBB aabbSouth = new AxisAlignedBB(0.0625,  0.0, 0.1875,  0.9375, 0.75, 1);

    public static final String unlocalizedName = "transfer_unit";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockTransferUnit instance;

    public BlockTransferUnit() {
        super(Material.IRON);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityTransferUnit.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraItemGroup.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, Direction.EAST));
    }

    public static boolean removePlate(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
                                      Hand hand, Direction hitFace) {
        TileEntityOptional.from(world, pos, TileEntityTransferUnit.class)
                .ifPresent(te -> {
                    te.removePlate();

                    if (!world.isRemote) {
                        WorldServer worldServer = (WorldServer) world;
                        LootTable table = worldServer.getLootTableManager().getLootTableFromLocation(plateLootTable);
                        LootContext.Builder builder = new LootContext.Builder(worldServer);
                        builder.withLuck(player.getLuck()).withPlayer(player);

                        table.generateLootForPools(player.getRNG(), builder.build())
                                .forEach(itemStack -> spawnAsEntity(worldServer, pos, itemStack));
                    }

                    world.playSound(player, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
                    world.notifyBlockUpdate(pos, blockState, blockState, 3);
                });

        return true;
    }

    public static boolean reconfigure(World world, BlockPos pos, BlockState blockState, PlayerEntity player,
                                      Hand hand, Direction hitFace) {
        TileEntityOptional.from(world, pos, TileEntityTransferUnit.class)
                .ifPresent(te -> {
                    te.reconfigure();
                    world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 1);
                    world.notifyBlockUpdate(pos, blockState, blockState, 3);
                });

        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        drops.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(BlockState state, Direction face, Collection<Capability> capabilities) {
        return Arrays.stream(new BlockInteraction[]{
                new BlockInteraction(Capability.pry, 1, Direction.SOUTH, 3, 11, 4, 6,
                        new PropertyMatcher().where(propPlate, equalTo(true)),
                        (world, pos, blockState, player, hand, hitFace) -> removePlate(world, pos, blockState, player, hand, hitFace)),
                new BlockInteraction(Capability.hammer, 1, Direction.SOUTH, 4, 10, 5, 9,
                        new PropertyMatcher().where(propPlate, equalTo(false)),
                        (world1, pos1, blockState1, player1, hand1, hitFace1) -> reconfigure(world1, pos1, blockState1, player1, hand1, hitFace1)),
        })
                .filter(interaction -> interaction.isPotentialInteraction(state, state.getValue(propFacing), face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand,
            Direction facing, float hitX, float hitY, float hitZ) {
        Direction blockFacing = state.getValue(propFacing);
        TileEntityTransferUnit te = TileEntityOptional.from(world, pos, TileEntityTransferUnit.class).orElse(null);
        ItemStack heldStack = player.getHeldItem(hand);

        if (te == null) {
            return false;
        }

        if (facing.equals(Direction.UP)) {
            if (te.hasCell()) {
                ItemStack cell = te.removeCell();
                if (player.inventory.addItemStackToInventory(cell)) {
                    player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1, 1);
                } else {
                    spawnAsEntity(world, pos.up(), cell);
                }

                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.6f);

                world.notifyBlockUpdate(pos, state, state, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, getActualState(state, world, pos), ItemStack.EMPTY);
                }

                return true;
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) {
                te.putCell(heldStack);
                player.setHeldItem(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.5f, 0.5f);
                world.notifyBlockUpdate(pos, state, state, 3);

                if (!player.world.isRemote) {
                    BlockUseCriterion.trigger((ServerPlayerEntity) player, getActualState(state, world, pos), ItemStack.EMPTY);
                }

                return true;
            }
        } else if (blockFacing.equals(facing.getOpposite())
                && heldStack.getItem() instanceof ItemVentPlate
                && !te.hasPlate()) {
            te.attachPlate();
            world.playSound(player, pos, SoundEvents.BLOCK_METAL_PLACE, SoundCategory.PLAYERS, 0.5f, 1);
            world.notifyBlockUpdate(pos, state, state, 3);
            heldStack.shrink(1);

            if (!player.world.isRemote) {
                BlockUseCriterion.trigger((ServerPlayerEntity) player, getActualState(state, world, pos), ItemStack.EMPTY);
            }

            return true;
        }

        return BlockInteraction.attemptInteraction(world, getActualState(world.getBlockState(pos), world, pos), pos, player, hand,
                facing, hitX, hitY, hitZ);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos) {
        if (!pos.offset(world.getBlockState(pos).getValue(propFacing)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, TileEntityTransferUnit.class)
                    .ifPresent(TileEntityTransferUnit::updateTransferState);
        }
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(BlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {

        Direction facing = getActualState(state, source, pos).getValue(propFacing);

        switch (facing) {
            case NORTH:
                return aabbNorth;
            case EAST:
                return aabbEast;
            case SOUTH:
                return aabbSouth;
            case WEST:
                return aabbWest;
            default:
                return null;
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propFacing, EnumTransferConfig.prop, propPlate, propCell, propTransfer);
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos) {
        BlockState actualState = super.getExtendedState(state, world, pos);


        return TileEntityOptional.from(world, pos, TileEntityTransferUnit.class)
                .map(te -> actualState
                        .withProperty(propPlate, te.hasPlate())
                        .withProperty(propCell, te.hasCell() ? te.getCharge() > 0 ? 2 : 1 : 0)
                        .withProperty(propTransfer, te.isReceiving() ? 2 : te.isSending() ? 1 : 0)
                        .withProperty(EnumTransferConfig.prop, te.getConfiguration()))
                .orElse(actualState);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return super.getDefaultState()
                .withProperty(propFacing, Direction.getHorizontal(meta & 0b11));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(propFacing).getHorizontalIndex();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTransferUnit();
    }

    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand) {
        BlockState BlockState = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);

        return BlockState.withProperty(propFacing, placer.getHorizontalFacing());
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }
}
