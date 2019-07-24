package se.mickelus.tetra.blocks.forged.extractor;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCoreExtractorPipe extends TetraBlock {
    public static final PropertyDirection propFacing = BlockDirectional.FACING;
    public static final PropertyBool propPowered = PropertyBool.create("powered");

    public static final String unlocalizedName = "extractor_pipe";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockCoreExtractorPipe instance;

    public BlockCoreExtractorPipe() {
        super(Material.IRON);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityCoreExtractorBase.class, new ResourceLocation(TetraMod.MOD_ID, unlocalizedName));
        setCreativeTab(TetraCreativeTabs.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, EnumFacing.UP));
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
        EnumFacing blockFacing = state.getValue(propFacing);
        boolean isPowered = state.getValue(propPowered);

        boolean getsPowered = false;
        for (EnumFacing facing : EnumFacing.values()) {
            if (!facing.equals(blockFacing)) {
                IBlockState adjacent = world.getBlockState(pos.offset(facing));
                if (adjacent.getBlock().equals(this)
                        && facing.equals(adjacent.getValue(propFacing).getOpposite())
                        && adjacent.getValue(propPowered)) {
                    getsPowered = true;
                }
            }
        }

        if (isPowered != getsPowered) {
            world.setBlockState(pos, state.withProperty(propPowered, getsPowered));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propFacing, propPowered);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(propFacing, EnumFacing.getFront(meta & 3))
                .withProperty(propPowered, (meta >> 3 & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propFacing).getIndex()
                | (state.getValue(propPowered) ? 1 << 3 : 0);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState iblockstate = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);

        return iblockstate.withProperty(propFacing, facing.getOpposite());
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }
}
