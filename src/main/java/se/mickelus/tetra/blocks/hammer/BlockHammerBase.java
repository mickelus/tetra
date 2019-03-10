package se.mickelus.tetra.blocks.hammer;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BlockHammerBase extends TetraBlock implements ITileEntityProvider, IBlockCapabilityInteractive {
    public static final PropertyDirection propFacing = BlockHorizontal.FACING;
    public static final PropertyBool propCell1 = PropertyBool.create("cell1");
    public static final PropertyBool propCell1Charged = PropertyBool.create("cell1charged");
    public static final PropertyBool propCell2 = PropertyBool.create("cell2");
    public static final PropertyBool propCell2Charged = PropertyBool.create("cell2charged");

    private static final ResourceLocation plateLootTable = new ResourceLocation(TetraMod.MOD_ID, "forged/plate_break");

    static final String unlocalizedName = "hammer_base";
    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockHammerBase instance;

    public static final BlockInteraction[] interactions = new BlockInteraction[] {
            new BlockInteraction(Capability.pry, 1, EnumHammerPlate.EAST.face, 5, 11, 9, 11,
                    EnumHammerPlate.EAST.prop, true, (world, pos, blockState, player, hand, hitFace) ->
                    removePlate(world, pos, blockState, player, EnumHammerPlate.EAST, hitFace)),
            new BlockInteraction(Capability.pry, 1, EnumHammerPlate.WEST.face, 5, 11, 9, 11,
                    EnumHammerPlate.WEST.prop, true, (world, pos, blockState, player, hand, hitFace) ->
                    removePlate(world, pos, blockState, player, EnumHammerPlate.WEST, hitFace)),

            new BlockInteraction(Capability.hammer, 1, EnumFacing.EAST, 6, 10, 2, 9,
                    EnumHammerPlate.EAST.prop, false, (world, pos, blockState, player, hand, hitFace) ->
                    reconfigure(world, pos, blockState, player, EnumFacing.EAST)),
            new BlockInteraction(Capability.hammer, 1, EnumFacing.WEST, 6, 10, 2, 9,
                    EnumHammerPlate.WEST.prop, false, (world, pos, blockState, player, hand, hitFace) ->
                    reconfigure(world, pos, blockState, player, EnumFacing.WEST))
    };

    public BlockHammerBase() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        GameRegistry.registerTileEntity(TileEntityHammerBase.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));

        hasItem = true;

        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(propFacing, EnumFacing.EAST)
                .withProperty(propCell1, false)
                .withProperty(propCell1Charged, false)
                .withProperty(propCell2, false)
                .withProperty(propCell2Charged, false));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("ancient_description"));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityHammerBase te = getTileEntity(world, pos);
        if (te != null) {
            return state
                    .withProperty(propCell1, te.hasCellInSlot(0))
                    .withProperty(propCell1Charged, te.getCellFuel(0) > 0)
                    .withProperty(propCell2, te.hasCellInSlot(1))
                    .withProperty(propCell2Charged, te.getCellFuel(1) > 0)
                    .withProperty(EnumHammerPlate.EAST.prop, te.hasPlate(EnumHammerPlate.EAST))
                    .withProperty(EnumHammerPlate.WEST.prop, te.hasPlate(EnumHammerPlate.WEST))
                    .withProperty(EnumHammerConfig.propE, te.getConfiguration(EnumFacing.EAST))
                    .withProperty(EnumHammerConfig.propW, te.getConfiguration(EnumFacing.WEST));
        }
        return state;
    }

    public boolean isFueled(World world, BlockPos pos) {
        return Optional.ofNullable(getTileEntity(world, pos))
                .map(TileEntityHammerBase::isFueled)
                .orElse(false);
    }

    public void consumeFuel(World world, BlockPos pos) {
        Optional.ofNullable(getTileEntity(world, pos))
                .ifPresent(te -> {
                    IBlockState blockState = world.getBlockState(pos);
                    te.consumeFuel();

                    world.notifyBlockUpdate(pos, blockState, blockState, 3);
                });
    }

    public void applyEffects(World world, BlockPos pos, ItemStack itemStack, EntityPlayer player) {
        Optional.ofNullable(getTileEntity(world, pos))
                .ifPresent(te -> {
                    if (te.hasEffect(EnumHammerEffect.DAMAGING) && itemStack.getItem() instanceof ItemModular) {
                        ItemModular item = (ItemModular) itemStack.getItem();
                        int damage = (int) (itemStack.getMaxDamage() * 0.1);
                        item.applyDamage(damage, itemStack, player);
                    }
                });
    }

    public int getHammerLevel(World world, BlockPos pos) {
        return Optional.ofNullable(getTileEntity(world, pos))
                .map(TileEntityHammerBase::getHammerLevel)
                .orElse(0);
    }

    public static boolean removePlate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHammerPlate plate, EnumFacing face) {
        Optional.ofNullable(getTileEntity(world, pos))
                .ifPresent(te -> {
                    te.removePlate(plate);

                    if (!world.isRemote) {
                        WorldServer worldServer = (WorldServer) world;
                        LootTable table = worldServer.getLootTableManager().getLootTableFromLocation(plateLootTable);
                        LootContext.Builder builder = new LootContext.Builder(worldServer);
                        builder.withLuck(player.getLuck()).withPlayer(player);

                        table.generateLootForPools(player.getRNG(), builder.build())
                                .forEach(itemStack -> spawnAsEntity(worldServer, pos, itemStack));
                    }

                    world.playSound(player, pos, SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
                    world.notifyBlockUpdate(pos, state, state, 3);
        });

        return true;
    }

    public static boolean reconfigure(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing adjustedFace) {
        Optional.ofNullable(getTileEntity(world, pos))
                .ifPresent(te -> {
                    te.reconfigure(adjustedFace);
                    world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.PLAYERS, 1, 1);
                    world.notifyBlockUpdate(pos, state, state, 3);
        });

        return true;
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
                EnumHammerPlate.EAST.prop, EnumHammerPlate.WEST.prop, EnumHammerConfig.propE, EnumHammerConfig.propW);
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
