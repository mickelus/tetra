package se.mickelus.tetra.blocks.hammer;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraCreativeTabs;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlockHammerHead extends TetraBlock implements ITileEntityProvider {

    static final String unlocalizedName = "hammer_head";
    public static final AxisAlignedBB boundingBox = new AxisAlignedBB(0.125, 0.8125, 0.125, 0.875, 1, 0.875);

    public static BlockHammerHead instance;

    public BlockHammerHead() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityHammerHead.class, TetraMod.MOD_ID + ":" + "tile_" +unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        instance = this;

        this.setDefaultState(this.blockState.getBaseState());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public Collection<Capability> getCapabilities(World world, BlockPos pos, IBlockState blockState) {
        BlockPos basePos = pos.offset(EnumFacing.UP);
        if (world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();

            if (baseBlock.isFueled(world, basePos)) {
                return Collections.singletonList(Capability.hammer);
            }
        }
        return super.getCapabilities(world, pos, blockState);
    }

    @Override
    public int getCapabilityLevel(World world, BlockPos pos, IBlockState blockState, Capability capability) {
        BlockPos basePos = pos.offset(EnumFacing.UP);
        if (Capability.hammer.equals(capability) && world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();

            if (baseBlock.isFueled(world, basePos)) {
                return baseBlock.getHammerLevel(world, basePos);
            }
        }
        return super.getCapabilityLevel(world, pos, blockState, capability);
    }

    @Override
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, IBlockState blockState, ItemStack targetStack, EntityPlayer player, boolean consumeResources) {
        BlockPos basePos = pos.offset(EnumFacing.UP);
        if (consumeResources && world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();
            baseBlock.consumeFuel(world, basePos);

            baseBlock.applyEffects(world, basePos, targetStack, player);

            ((TileEntityHammerHead) world.getTileEntity(pos)).activate();
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3f, (float) (0.5 + Math.random() * 0.1));
        }
        return targetStack;
    }

    @Override
    public ItemStack onActionConsumeCapability(World world, BlockPos pos, IBlockState blockState, ItemStack targetStack, EntityPlayer player, boolean consumeResources) {
    BlockPos basePos = pos.offset(EnumFacing.UP);
        if (consumeResources && world.getBlockState(basePos).getBlock() instanceof BlockHammerBase) {
            BlockHammerBase baseBlock = (BlockHammerBase) world.getBlockState(basePos).getBlock();
            baseBlock.consumeFuel(world, basePos);

            ((TileEntityHammerHead) world.getTileEntity(pos)).activate();
            world.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3f, (float) (0.5 + Math.random() * 0.1));
        }
        return targetStack;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return boundingBox;
    }

    @Override
    public ExtendedBlockState createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{ Properties.StaticProperty }, new IUnlistedProperty[]{ Properties.AnimationProperty });
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHammerHead();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(Properties.StaticProperty, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }
}
