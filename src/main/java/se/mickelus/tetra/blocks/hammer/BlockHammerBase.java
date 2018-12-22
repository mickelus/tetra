package se.mickelus.tetra.blocks.hammer;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class BlockHammerBase extends TetraBlock implements ITileEntityProvider, IBlockCapabilityInteractive {
    public static final PropertyDirection propFacing = BlockHorizontal.FACING;
    public static final PropertyBool propCell1 = PropertyBool.create("cell1");
    public static final PropertyBool propCell1Charged = PropertyBool.create("cell1charged");
    public static final PropertyBool propCell2 = PropertyBool.create("cell2");
    public static final PropertyBool propCell2Charged = PropertyBool.create("cell2charged");

    static final String unlocalizedName = "hammer_base";

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.hammer, 1, EnumHammerPlate.EAST.face, 5, 11, 9, 12, EnumHammerPlate.EAST.prop,
                    true, (world, pos, blockState, player, hitFace) -> removePlate(world, pos, blockState, player, EnumHammerPlate.EAST, hitFace)),
            new BlockInteraction(Capability.hammer, 1, EnumHammerPlate.WEST.face, 5, 11, 9, 12, EnumHammerPlate.WEST.prop,
                    true, (world, pos, blockState, player, hitFace) -> removePlate(world, pos, blockState, player, EnumHammerPlate.WEST, hitFace)),

            new BlockInteraction(Capability.axe, 1, EnumHammerCable.WEST1.face, 5, 11, 1, 3, EnumHammerCable.WEST1.prop,
                    true, (world, pos, blockState, player, hitFace) -> cutCable(world, pos, blockState, player, EnumHammerCable.WEST1, hitFace)),
            new BlockInteraction(Capability.axe, 1, EnumHammerCable.WEST2.face, 5, 11, 4, 5, EnumHammerCable.WEST2.prop,
                    true, (world, pos, blockState, player, hitFace) -> cutCable(world, pos, blockState, player, EnumHammerCable.WEST2, hitFace)),
            new BlockInteraction(Capability.axe, 1, EnumHammerCable.WEST3.face, 5, 11, 6, 7, EnumHammerCable.WEST3.prop,
                    true, (world, pos, blockState, player, hitFace) -> cutCable(world, pos, blockState, player, EnumHammerCable.WEST3, hitFace)),
            new BlockInteraction(Capability.axe, 1, EnumHammerCable.WEST4.face, 5, 11, 10, 12, EnumHammerCable.WEST4.prop,
                    true, (world, pos, blockState, player, hitFace) -> cutCable(world, pos, blockState, player, EnumHammerCable.WEST4, hitFace))
    };

    public BlockHammerBase() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        GameRegistry.registerTileEntity(TileEntityHammerBase.class, unlocalizedName);

        hasItem = true;

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(propFacing, EnumFacing.EAST)
                .withProperty(propCell1, false)
                .withProperty(propCell1Charged, false)
                .withProperty(propCell2, false)
                .withProperty(propCell2Charged, false));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityHammerBase te = getTileEntity(world, pos);
        if (te != null) {
            return state
                    .withProperty(propCell1, te.hasCellInSlot(0))
                    .withProperty(propCell1Charged, te.getCellPower(0) > 0)
                    .withProperty(propCell2, te.hasCellInSlot(1))
                    .withProperty(propCell2Charged, te.getCellPower(1) > 0)
                    .withProperty(EnumHammerPlate.EAST.prop, te.hasPlate(EnumHammerPlate.EAST))
                    .withProperty(EnumHammerPlate.WEST.prop, te.hasPlate(EnumHammerPlate.WEST))
                    .withProperty(EnumHammerCable.WEST1.prop, !te.hasPlate(EnumHammerPlate.WEST) && te.hasCable(EnumHammerCable.WEST1))
                    .withProperty(EnumHammerCable.WEST2.prop, !te.hasPlate(EnumHammerPlate.WEST) && te.hasCable(EnumHammerCable.WEST2))
                    .withProperty(EnumHammerCable.WEST3.prop, !te.hasPlate(EnumHammerPlate.WEST) && te.hasCable(EnumHammerCable.WEST3))
                    .withProperty(EnumHammerCable.WEST4.prop, !te.hasPlate(EnumHammerPlate.WEST) && te.hasCable(EnumHammerCable.WEST4))
                    .withProperty(EnumHammerCable.EAST1.prop, !te.hasPlate(EnumHammerPlate.EAST) && te.hasCable(EnumHammerCable.EAST1))
                    .withProperty(EnumHammerCable.EAST2.prop, !te.hasPlate(EnumHammerPlate.EAST) && te.hasCable(EnumHammerCable.EAST2))
                    .withProperty(EnumHammerCable.EAST3.prop, !te.hasPlate(EnumHammerPlate.EAST) && te.hasCable(EnumHammerCable.EAST3))
                    .withProperty(EnumHammerCable.EAST4.prop, !te.hasPlate(EnumHammerPlate.EAST) && te.hasCable(EnumHammerCable.EAST4));
        }
        return state;
    }

    public boolean isPowered(World world, BlockPos pos) {
        return Optional.ofNullable(getTileEntity(world, pos))
                .filter(Objects::nonNull)
                .map(TileEntityHammerBase::isPowered)
                .orElse(false);
    }

    public void consumePower(World world, BlockPos pos) {
        TileEntityHammerBase te = getTileEntity(world, pos);
        if (te != null) {
            IBlockState blockState = world.getBlockState(pos);
            te.consumePower();

            world.notifyBlockUpdate(pos, blockState, blockState, 3);
        }
    }

    public static void removePlate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHammerPlate plate, EnumFacing face) {
        TileEntityHammerBase te = getTileEntity(world, pos);
        if (te != null) {
            te.removePlate(plate);
            spawnAsEntity(world, pos.offset(face), new ItemStack(Items.IRON_NUGGET, world.rand.nextInt(16) + 1));
            world.playSound(player, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    public static void cutCable(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHammerCable cable, EnumFacing face) {
        TileEntityHammerBase te = getTileEntity(world, pos);
        if (te != null) {
            te.cutCable(cable);
            // spawnAsEntity(world, pos.offset(face), new ItemStack(Items.IRON_NUGGET, world.rand.nextInt(16) + 1));
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 0.5f);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumFacing blockFacing = state.getValue(propFacing);
        TileEntityHammerBase te = getTileEntity(world, pos);

        if (te != null && blockFacing.getAxis().equals(facing.getAxis())) {
            int slotIndex = blockFacing.equals(facing)? 0 : 1;
            ItemStack heldStack = player.getHeldItem(hand);
            if (te.hasCellInSlot(slotIndex)) {
                spawnAsEntity(world, pos.offset(facing), te.removeCellFromSlot(slotIndex));
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 1, 0.6f);
                world.notifyBlockUpdate(pos, state, state, 3);
                return true;
            } else if (heldStack.getItem() instanceof ItemCellMagmatic) {
                te.putCellInSlot(heldStack, slotIndex);
                player.setHeldItem(hand, ItemStack.EMPTY);
                world.playSound(player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 1, 0.5f);
                world.notifyBlockUpdate(pos, state, state, 3);
                return true;
            }
        }

        return BlockInteraction.attemptInteraction(world, state.getActualState(world, pos), pos, player, hand, facing, hitX, hitY, hitZ);
    }

    public static void spawnAsEntity(World worldIn, BlockPos pos, ItemStack stack) {
        if (!worldIn.isRemote && !stack.isEmpty() && worldIn.getGameRules().getBoolean("doTileDrops") && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
            if (captureDrops.get()) {
                capturedDrops.get().add(stack);
                return;
            }
            EntityItem entityitem = new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            entityitem.setDefaultPickupDelay();
            worldIn.spawnEntity(entityitem);
        }
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(IBlockState state, EnumFacing face, Collection<Capability> capabilities) {
        return Arrays.stream(interactions)
                .filter(interaction -> interaction.isPotentialInteraction(state, state.getValue(propFacing), face, capabilities))
                .toArray(BlockInteraction[]::new);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHammerBase();
    }

    private static TileEntityHammerBase getTileEntity(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity;

        if (world instanceof ChunkCache) {
            tileEntity = ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        } else {
            tileEntity = world.getTileEntity(pos);
        }

        if (tileEntity instanceof TileEntityHammerBase) {
            return (TileEntityHammerBase) tileEntity;
        }
        return null;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propFacing, propCell1, propCell1Charged, propCell2 , propCell2Charged,
                EnumHammerPlate.EAST.prop, EnumHammerPlate.WEST.prop, EnumHammerCable.WEST1.prop,
                EnumHammerCable.WEST2.prop, EnumHammerCable.WEST3.prop, EnumHammerCable.WEST4.prop,
                EnumHammerCable.EAST1.prop, EnumHammerCable.EAST2.prop, EnumHammerCable.EAST3.prop,
                EnumHammerCable.EAST4.prop);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(propFacing, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(propFacing, EnumFacing.getHorizontal(meta & 0xf));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propFacing).getHorizontalIndex();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(propFacing)));
    }
}
