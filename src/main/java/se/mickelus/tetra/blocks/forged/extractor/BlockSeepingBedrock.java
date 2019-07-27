package se.mickelus.tetra.blocks.forged.extractor;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
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

import javax.annotation.Nullable;
import java.util.List;

public class BlockSeepingBedrock extends TetraBlock {
    public static final PropertyInteger propActive = PropertyInteger.create("active", 0, 15);

    public static final String unlocalizedName = "seeping_bedrock";

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockSeepingBedrock instance;

    public BlockSeepingBedrock() {
        super(Material.ROCK);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propActive, 15));
    }

    public static boolean isActive(IBlockAccess world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        return instance.equals(blockState.getBlock()) && blockState.getValue(propActive) > 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(ChatFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propActive);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(propActive, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propActive);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(propActive, placer.isSneaking() ? 0 : 15);
    }
}
