package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraCreativeTabs;

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
        setCreativeTab(TetraCreativeTabs.getInstance());

        setBlockUnbreakable();
        setResistance(22);

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, EnumFacing.UP)
                .withProperty(propPowered, false));
    }

    public static boolean isPowered(IBlockAccess world, BlockPos pos) {
        IBlockState pipeState = world.getBlockState(pos.down());
        return instance.equals(pipeState.getBlock()) && pipeState.getValue(propPowered);
    }

    private boolean shouldGetPower(IBlockAccess world, BlockPos pos, EnumFacing blockFacing) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (!facing.equals(blockFacing)) {
                IBlockState adjacent = world.getBlockState(pos.offset(facing));
                if (adjacent.getBlock().equals(this)
                        && facing.equals(adjacent.getValue(propFacing).getOpposite())
                        && adjacent.getValue(propPowered)) {
                    return true;
                }
            }
        }

        return BlockSeepingBedrock.isActive(world, pos.offset(blockFacing.getOpposite()));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.clear();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos) {
        boolean getsPowered = shouldGetPower(world, pos, state.getValue(propFacing));

        if (state.getValue(propPowered) != getsPowered) {
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
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer)
                .withProperty(propFacing, facing)
                .withProperty(propPowered, shouldGetPower(world, pos, facing));
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }
}
