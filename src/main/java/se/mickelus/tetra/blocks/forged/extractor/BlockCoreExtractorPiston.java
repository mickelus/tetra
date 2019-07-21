package se.mickelus.tetra.blocks.forged.extractor;

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
import net.minecraft.util.*;
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
import se.mickelus.tetra.blocks.hammer.BlockHammerBase;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraCreativeTabs;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockCoreExtractorPiston extends TetraBlock implements ITileEntityProvider {

    static final String unlocalizedName = "extractor_piston";
    public static final AxisAlignedBB boundingBox = new AxisAlignedBB(0.3125, 0, 0.3125, 0.6875, 1, 0.6875);

    @GameRegistry.ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockCoreExtractorPiston instance;

    public BlockCoreExtractorPiston() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        GameRegistry.registerTileEntity(TileEntityCoreExtractorPiston.class, TetraMod.MOD_ID + ":" + "tile_" +unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        setTickRandomly(true);

        this.setDefaultState(this.blockState.getBaseState());
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return TileEntityOptional.from(worldIn, pos, TileEntityCoreExtractorPiston.class)
                .map(te -> {
                    te.activate();
                    return true;
                }).orElse(false);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        TileEntityOptional.from(worldIn, pos, TileEntityCoreExtractorPiston.class)
                .ifPresent(te -> {
                    if (te.isActive()) {
                        float random = rand.nextFloat();

                        if (random < 0.6f) {
                            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                                    pos.getX() + 0.4 + rand.nextGaussian() * 0.2,
                                    pos.getY() + rand.nextGaussian(),
                                    pos.getZ() + 0.4 + rand.nextGaussian() * 0.2,
                                    0.0D, 0.0D, 0.0D);
                        }
                    }
                });
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
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return boundingBox;
    }

    @Override
    public ExtendedBlockState createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{ Properties.StaticProperty }, new IUnlistedProperty[]{ Properties.AnimationProperty });
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

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCoreExtractorPiston();
    }
}
