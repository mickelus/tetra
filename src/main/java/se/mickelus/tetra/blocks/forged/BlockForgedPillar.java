package se.mickelus.tetra.blocks.forged;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;

public class BlockForgedPillar extends TetraBlock {
    public static final PropertyEnum<EnumFacing.Axis> propAxis = PropertyEnum.<EnumFacing.Axis>create("axis", EnumFacing.Axis.class);

    static final String unlocalizedName = "forged_pillar";

    public static BlockForgedPillar instance;

    public BlockForgedPillar() {
        super(Material.IRON);

        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraCreativeTabs.getInstance());
        setBlockUnbreakable();

        hasItem = true;

        instance = this;
        this.setDefaultState(this.blockState.getBaseState().withProperty(propAxis, EnumFacing.Axis.Y));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, propAxis);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(propAxis, facing.getAxis());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta < EnumFacing.Axis.values().length) {
            return this.getDefaultState().withProperty(propAxis, EnumFacing.Axis.values()[meta]);
        }
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(propAxis).ordinal();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.getValue(propAxis)) {
                    case X:
                        return state.withProperty(propAxis, EnumFacing.Axis.Z);
                    case Z:
                        return state.withProperty(propAxis, EnumFacing.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }
}
