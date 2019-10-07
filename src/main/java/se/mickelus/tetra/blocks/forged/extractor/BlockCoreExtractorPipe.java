package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.items.TetraItemGroup;

public class BlockCoreExtractorPipe extends TetraBlock {
    public static final PropertyDirection propFacing = BlockDirectional.FACING;
    public static final PropertyBool propPowered = PropertyBool.create("powered");

    public static final String unlocalizedName = "extractor_pipe";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockCoreExtractorPipe instance;

    public BlockCoreExtractorPipe() {
        super(Material.IRON);
        setRegistryName(unlocalizedName);
        setUnlocalizedName(unlocalizedName);
        setCreativeTab(TetraItemGroup.getInstance());

        setBlockUnbreakable();
        setResistance(22);

        hasItem = true;

        setDefaultState(getBlockState().getBaseState()
                .withProperty(propFacing, Direction.UP)
                .withProperty(propPowered, false));
    }

    public static boolean isPowered(IBlockAccess world, BlockPos pos) {
        BlockState pipeState = world.getBlockState(pos.down());
        return instance.equals(pipeState.getBlock()) && pipeState.getValue(propPowered);
    }

    private boolean shouldGetPower(IBlockAccess world, BlockPos pos, Direction blockFacing) {
        for (Direction facing : Direction.values()) {
            if (!facing.equals(blockFacing)) {
                BlockState adjacent = world.getBlockState(pos.offset(facing));
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
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
        drops.clear();
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos) {
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
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(propFacing, Direction.getFront(meta & 3))
                .withProperty(propPowered, (meta >> 3 & 1) == 1);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(propFacing).getIndex()
                | (state.getValue(propPowered) ? 1 << 3 : 0);
    }

    @Override
    public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer, Hand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer)
                .withProperty(propFacing, facing)
                .withProperty(propPowered, shouldGetPower(world, pos, facing));
    }

    @Override
    public BlockState withRotation(BlockState state, Rotation rot) {
        return state.withProperty(propFacing, rot.rotate(state.getValue(propFacing)));
    }
}
