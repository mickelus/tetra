package se.mickelus.tetra.blocks.forged.extractor;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.transfer.EnumTransferConfig;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCoreExtractorBase extends TetraBlock implements ITileEntityProvider {
    public static final PropertyDirection propFacing = BlockHorizontal.FACING;

    public static final String unlocalizedName = "core_extractor";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockCoreExtractorBase instance;

    public BlockCoreExtractorBase() {
        super(Material.IRON);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityCoreExtractorBase.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraCreativeTabs.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, EnumFacing.EAST));
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
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos) {
        if (!pos.offset(world.getBlockState(pos).getValue(propFacing)).equals(fromPos)) {
            TileEntityOptional.from(world, pos, TileEntityCoreExtractorBase.class)
                    .ifPresent(TileEntityCoreExtractorBase::updateTransferState);
        }
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
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propFacing);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(propFacing, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propFacing).getHorizontalIndex();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCoreExtractorBase();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState iblockstate = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);

        return iblockstate.withProperty(propFacing, placer.getHorizontalFacing());
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }
}
