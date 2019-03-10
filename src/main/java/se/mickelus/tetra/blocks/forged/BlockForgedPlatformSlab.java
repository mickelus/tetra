package se.mickelus.tetra.blocks.forged;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;

import javax.annotation.Nullable;
import java.util.List;


public class BlockForgedPlatformSlab extends TetraBlock {
    public static final PropertyEnum<BlockSlab.EnumBlockHalf> halfProp = PropertyEnum.<BlockSlab.EnumBlockHalf>create("half", BlockSlab.EnumBlockHalf.class);
    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

    static final String unlocalizedName = "forged_platform_slab";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedPlatformSlab instance;

    public BlockForgedPlatformSlab() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        fullBlock = false;

        this.setDefaultState(this.blockState.getBaseState().withProperty(halfProp, BlockSlab.EnumBlockHalf.BOTTOM));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("ancient_description"));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, halfProp);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(halfProp, meta == 1 ? BlockSlab.EnumBlockHalf.TOP : BlockSlab.EnumBlockHalf.BOTTOM);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP) {
            return 1;
        }

        return 0;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        if (face == EnumFacing.UP && state.getValue(halfProp) == BlockSlab.EnumBlockHalf.TOP) {
            return BlockFaceShape.SOLID;
        } else if (face == EnumFacing.DOWN && state.getValue(halfProp) == BlockSlab.EnumBlockHalf.BOTTOM) {
            return BlockFaceShape.SOLID;
        }
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        if (net.minecraftforge.common.ForgeModContainer.disableStairSlabCulling) {
            return false;
        }

        BlockSlab.EnumBlockHalf side = state.getValue(halfProp);
        return (side == BlockSlab.EnumBlockHalf.TOP && face == EnumFacing.UP) || (side == BlockSlab.EnumBlockHalf.BOTTOM && face == EnumFacing.DOWN);
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(halfProp, BlockSlab.EnumBlockHalf.BOTTOM);

        return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double)hitY <= 0.5D) ? iblockstate : iblockstate.withProperty(halfProp, BlockSlab.EnumBlockHalf.TOP);
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (super.shouldSideBeRendered(blockState, blockAccess, pos, side)) {
            return true;
        }
        return !(side != EnumFacing.UP && side != EnumFacing.DOWN);
    }
}