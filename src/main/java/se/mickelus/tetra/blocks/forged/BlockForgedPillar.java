package se.mickelus.tetra.blocks.forged;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.state.EnumProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.Materials;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraItemGroup;

import javax.annotation.Nullable;
import java.util.List;

public class BlockForgedPillar extends TetraBlock {
    public static final EnumProperty<Direction.Axis> propAxis = EnumProperty.<Direction.Axis>create("axis", Direction.Axis.class);

    static final String unlocalizedName = "forged_pillar";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockForgedPillar instance;

    public BlockForgedPillar() {
        super(Materials.forged);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());
        setBlockUnbreakable();
        setResistance(25);

        hasItem = true;

        this.setDefaultState(this.blockState.getBaseState().withProperty(propAxis, Direction.Axis.Y));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(TextFormatting.DARK_GRAY + I18n.format("forged_description"));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        drops.clear();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propAxis);
    }

    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return this.getDefaultState().withProperty(propAxis, facing.getAxis());
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        if (meta < Direction.Axis.values().length) {
            return this.getDefaultState().withProperty(propAxis, Direction.Axis.values()[meta]);
        }
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(propAxis).ordinal();
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.getValue(propAxis)) {
                    case X:
                        return state.withProperty(propAxis, Direction.Axis.Z);
                    case Z:
                        return state.withProperty(propAxis, Direction.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }
}
