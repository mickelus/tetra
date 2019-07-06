package se.mickelus.tetra.blocks.forged.transfer;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.items.forged.ItemVentPlate;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Predicates.equalTo;

public class BlockTransferUnit extends TetraBlock implements ITileEntityProvider, IBlockCapabilityInteractive {
    public static final PropertyDirection propFacing = BlockHorizontal.FACING;
    public static final PropertyBool propPlate = PropertyBool.create("plate");
    public static final PropertyInteger propCell = PropertyInteger.create("cell", 0, 2);

    private static final ResourceLocation plateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/plate_break");

    public static final BlockInteraction[] interactions = new BlockInteraction[]{
            new BlockInteraction(Capability.pry, 1, EnumFacing.SOUTH, 5, 7, 2, 5,
                    new PropertyMatcher().where(propPlate, equalTo(true)),
                    BlockTransferUnit::removePlate),
            new BlockInteraction(Capability.hammer, 1, EnumFacing.SOUTH, 11, 13, 2, 5,
                    new PropertyMatcher().where(propPlate, equalTo(false)),
                    BlockTransferUnit::reconfigure),
    };

    private static final AxisAlignedBB aabbEast = new AxisAlignedBB(0.1875,  0.0, 0.0625, 1, 0.75, 0.9375);
    private static final AxisAlignedBB aabbNorth = new AxisAlignedBB(0.0625,  0.0, 0.0,  0.9375, 0.75, 0.8125);
    private static final AxisAlignedBB aabbWest = new AxisAlignedBB(0.0, 0.0, 0.0625,  0.8125, 0.75, 0.9375);
    private static final AxisAlignedBB aabbSouth = new AxisAlignedBB(0.0625,  0.0, 0.1875,  0.9375, 0.75, 1);

    public static final String unlocalizedName = "transfer_unit";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockTransferUnit instance;

    public BlockTransferUnit() {
        super(Material.IRON);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityTransferUnit.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraCreativeTabs.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, EnumFacing.EAST));
    }

    public static boolean removePlate(World world, BlockPos pos, IBlockState blockState, EntityPlayer player,
                                      EnumHand hand, EnumFacing hitFace) {
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

    public static boolean reconfigure(World world, BlockPos pos, IBlockState blockState, EntityPlayer player,
                                      EnumHand hand, EnumFacing hitFace) {
        TileEntityOptional.from(world, pos, TileEntityTransferUnit.class)
                .ifPresent(te -> {
                    te.reconfigure();
                    world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 1);
                    world.notifyBlockUpdate(pos, blockState, blockState, 3);
                });

        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.clear();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(IBlockState state, EnumFacing face, Collection<Capability> capabilities) {
        return Arrays.stream(new BlockInteraction[]{
                new BlockInteraction(Capability.pry, 1, EnumFacing.SOUTH, 3, 11, 4, 6,
                        new PropertyMatcher().where(propPlate, equalTo(true)),
                        BlockTransferUnit::removePlate),
                new BlockInteraction(Capability.hammer, 1, EnumFacing.SOUTH, 4, 10, 5, 9,
                        new PropertyMatcher().where(propPlate, equalTo(false)),
                        BlockTransferUnit::reconfigure),
        })
                .filter(interaction -> interaction.isPotentialInteraction(state, state.getValue(propFacing), face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumFacing blockFacing = state.getValue(propFacing);
        TileEntityTransferUnit te = TileEntityOptional.from(world, pos, TileEntityTransferUnit.class).orElse(null);
        ItemStack heldStack = player.getHeldItem(hand);

        if (te == null) {
            return false;
        }

        if (facing.equals(EnumFacing.UP)) {
            if (te.hasCell()) {
                spawnAsEntity(world, pos, te.removeCell());
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 1, 0.6f);
                world.notifyBlockUpdate(pos, state, state, 3);
                return true;
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) {
                te.putCell(heldStack);
                player.setHeldItem(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 1, 0.5f);
                world.notifyBlockUpdate(pos, state, state, 3);
                return true;
            }
        } else if (blockFacing.equals(facing.getOpposite())
                && heldStack.getItem() instanceof ItemVentPlate
                && !te.hasPlate()) {
            te.attachPlate();
            world.notifyBlockUpdate(pos, state, state, 3);
            heldStack.shrink(1);
            return true;
        }

        return BlockInteraction.attemptInteraction(world, getActualState(world.getBlockState(pos), world, pos), pos, player, hand,
                facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

        EnumFacing facing = getActualState(state, source, pos).getValue(propFacing);

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
        return new BlockStateContainer(this, propFacing, EnumTransferConfig.prop, propPlate, propCell);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState actualState = super.getExtendedState(state, world, pos);


        return TileEntityOptional.from(world, pos, TileEntityTransferUnit.class)
                .map(te -> actualState
                        .withProperty(propPlate, te.hasPlate())
                        .withProperty(propCell, te.hasCell() ? te.getCellFuel() > 0 ? 2 : 1 : 0)
                        .withProperty(EnumTransferConfig.prop, te.getConfiguration()))
                .orElse(actualState);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getDefaultState()
                .withProperty(propFacing, EnumFacing.getHorizontal(meta & 0b11));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propFacing).getHorizontalIndex();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTransferUnit();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState iblockstate = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);

        return iblockstate.withProperty(propFacing, placer.getHorizontalFacing());
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return TileEntityOptional.from(world, pos, TileEntityTransferUnit.class)
                .map(te -> te.getCellFuel() > 0 ? 1 : 0)
                .orElse(0);
    }
}
