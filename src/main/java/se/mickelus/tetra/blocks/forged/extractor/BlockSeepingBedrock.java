package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class BlockSeepingBedrock extends TetraBlock {
    public static final PropertyInteger propActive = PropertyInteger.create("active", 0, 15);

    public static final String unlocalizedName = "seeping_bedrock";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockSeepingBedrock instance;

    public BlockSeepingBedrock() {
        super(Material.ROCK);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());

        setBlockUnbreakable();

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propActive, 15));
    }

    public static boolean isActive(IBlockAccess world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return instance.equals(blockState.getBlock()) && blockState.getValue(propActive) > 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propActive);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(propActive, meta);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(propActive);
    }

    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand) {
        return getDefaultState().withProperty(propActive, placer.isSneaking() ? 0 : 15);
    }
}
